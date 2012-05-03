/*
 * Copyright 2004 - 2012 Cardiff University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atticfs.store.index;

import org.atticfs.types.FileHash;
import org.atticfs.types.FileSegmentHash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Creates DataDescriptions from Files.
 *
 * 
 */

public class Indexer implements Runnable {

    static Logger log = Logger.getLogger("org.atticfs.store.index.Indexer");


    private File file;
    private int chunkSize;
    private BlockingQueue<FileMapping> queue;
    private Pattern p = null;

    private static String[] excludes = {".", "CVS"};

    public Indexer(File file, int chunkSize, BlockingQueue<FileMapping> queue) {
        this.file = file;
        this.chunkSize = chunkSize;
        this.queue = queue;
    }

    public Indexer(File file, int chunkSize, BlockingQueue<FileMapping> queue, String filePattern) {
        this(file, chunkSize, queue);
        p = Pattern.compile(filePattern);
    }

    public void run() {
        if (file == null || queue == null) {
            log.warning("attempting to call index without setting the file or the queue. Returning...");
            return;
        }
        try {
            crawl(file);
            queue.put(new FileMapping(null, null)); // signal to finish - no file hash in data description
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File[] getFiles(File dir) {
        if (p != null) {
            return dir.listFiles(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    if (p.matcher(s).matches()) {
                        return true;
                    }
                    return false;
                }
            });
        } else {
            return dir.listFiles(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    for (String exclude : excludes) {
                        if (s.startsWith(exclude)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
        }
    }

    public void crawl(File file) throws InterruptedException, IOException {
        if (file.isDirectory()) {
            File[] children = getFiles(file);
            if (children == null) {
                return;
            }
            for (File child : children) {
                crawl(child);
            }
        } else {
            FileHash fileHash = createFileHash(file, chunkSize);
            queue.put(new FileMapping(file, fileHash));
        }
    }

    public FileHash createFileHash(File f, int chunkSize) throws IOException {

        FileHash fh = new FileHash();
        long length = f.length();
        fh.setSize(length);

        boolean chunk = false;
        if (chunkSize != -1 && length >= chunkSize * 2) {
            chunk = true;
        }
        createChunks(chunkSize, fh, f, chunk);
        return fh;
    }

    private void createChunks(int chunkSize, FileHash fileHash, File f, boolean chunk) throws IOException {
        MessageDigest fullMd = null;
        MessageDigest chunkMd = null;
        try {
            fullMd = MessageDigest.getInstance("MD5");
            chunkMd = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            assert false;
        }
        FileInputStream in = new FileInputStream(f);
        byte[] buff = new byte[chunkSize];
        int c;
        long i = 0;

        while ((c = in.read(buff, 0, buff.length)) != -1) {
            while (c < chunkSize) { // incase there was some blocking for some reason
                int curr = in.read(buff, c, buff.length - c);
                if (curr == -1) {
                    break;
                }
                c += curr;
            }
            fullMd.update(buff, 0, c);
            //if (chunk) {
            String hash = hashChunk(buff, 0, c, chunkMd);
            fileHash.addSegment(new FileSegmentHash(hash, i, i + c - 1));
            i += c;
            //}
        }
        in.close();
        fileHash.setHash(toHex(fullMd.digest()));
    }

    private String hashChunk(byte[] bytes, int s, int l, MessageDigest md) {
        md.update(bytes, s, l);
        String ret = toHex(md.digest());
        md.reset();
        return ret;
    }

    private String toHex(byte[] digest) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            buf.append(Integer.toHexString((int) digest[i] & 0x00FF));
        }
        return buf.toString();
    }

}
