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

package org.atticfs.util;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class FileUtils {

    static Logger log = Logger.getLogger("org.atticfs.util.FileUtils");


    /**
     * deletes files recursively. can optionally delete the parent file as well. So, if the parent file
     * is not a directory, and incParent is false, then nothing will be deleted.
     *
     * @param parent    file to delete. If this is a directory then any children are deleted.
     * @param incParent boolean that determines if the parent file is also deleted.
     * @throws java.io.FileNotFoundException
     */
    public static void deleteFiles(File parent, boolean incParent) throws FileNotFoundException {
        if (!parent.exists()) {
            throw new FileNotFoundException("File does not exist.");
        }
        if (parent.isDirectory() && !(parent.listFiles() == null)) {
            File[] files = parent.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFiles(files[i], true);
            }
        }
        if (incParent) {
            parent.delete();
        }
    }

    public static long spaceAvailable(File dataStore, long max) {
        return max - spaceUsed(dataStore);
    }

    public static long spaceUsed(File dataStore) {
        long total = 0;
        if (!dataStore.exists()) {
            return 0;
        }
        if (dataStore.isDirectory()) {
            File[] children = dataStore.listFiles();
            if (children == null || children.length == 0) {
                return 0;
            }
            for (File child : children) {
                total += spaceUsed(child);
            }
        } else {
            total += dataStore.length();
        }
        return total;
    }

    public static boolean verify(File f, String hash) {
        try {
            FileChannel channel = new FileInputStream(f).getChannel();
            MappedByteBuffer in = channel.map(FileChannel.MapMode.READ_ONLY, 0, f.length());
            MessageDigest md = MessageDigest.getInstance("MD5");
            int len = 8192;
            byte[] bytes = new byte[len];
            while (in.remaining() > 0) {
                int c = Math.min(len, in.remaining());
                in.get(bytes, 0, c);
                md.update(bytes, 0, c);
            }
            byte[] digest = md.digest();
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                buf.append(Integer.toHexString((int) digest[i] & 0x00FF));
            }
            boolean b = buf.toString().equalsIgnoreCase(hash);
            return b;

        } catch (Exception e) {
            log.warning("error reading file:" + e.getMessage());
            return false;
        }
    }

    public static ByteArrayOutputStream verify(InputStream in, String hash) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            MessageDigest md = MessageDigest.getInstance("MD5");
            int len = 8192;
            byte[] bytes = new byte[len];
            int c;
            while ((c = in.read(bytes)) != -1) {
                md.update(bytes, 0, c);
                bout.write(bytes, 0, c);
            }
            bout.flush();
            bout.close();
            byte[] digest = md.digest();
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                buf.append(Integer.toHexString((int) digest[i] & 0x00FF));
            }
            boolean b = buf.toString().equalsIgnoreCase(hash);
            if (b) {
                return bout;
            }
            return null;

        } catch (Exception e) {
            log.warning("error reading file:" + e.getMessage());
            return null;
        }
    }

    public static String formatThrowable(Throwable t) {
        StringBuilder sb = new StringBuilder(t.getClass().getName());
        t.fillInStackTrace();
        StackTraceElement[] trace = t.getStackTrace();
        for (StackTraceElement stackTraceElement : trace) {
            sb.append("\t\n").append(stackTraceElement);
        }
        return sb.toString();
    }

    public static boolean rename(File src, File dest) {
        if (src.getAbsolutePath().equals(dest.getAbsolutePath())) {
            return true;
        }
        boolean rename = src.renameTo(dest);
        if (!rename) {
            try {
                copyFilesRecursive(src, dest);
            } catch (IOException e) {
                log.warning("Exception thrown while renaming:" + FileUtils.formatThrowable(e));
                return false;
            }
        }
        return true;
    }


    /**
     * Copies files and directories recursively. The destination does not
     * have to exist yet.
     *
     * @param src  the source file to copy. Can be a file or a directory.
     * @param dest the destination to copy to. Can be a file or directory.
     * @return a List of the newly created Files
     * @throws java.io.FileNotFoundException if src does not exist
     * @throws java.io.IOException           if src is a directory and dest exists
     *                                       and is not a directory, or an IO error occurs.
     */
    public static List<File> copyFilesRecursive(File src, File dest) throws IOException {
        ArrayList<File> list = new ArrayList<File>();
        if (!src.exists()) {
            throw new FileNotFoundException("Input file does not exist.");
        }
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdirs();
            } else if (!dest.isDirectory()) {
                throw new IOException("cannot write a directory to a file.");
            }
            list.add(dest);
            File[] srcFiles = src.listFiles();
            for (int i = 0; i < srcFiles.length; i++) {
                list.addAll(copyFilesRecursive(srcFiles[i], new File(dest, srcFiles[i].getName())));
            }
        } else {
            if (dest.exists() && dest.isDirectory()) {
                dest = new File(dest, src.getName());
            }
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] bytes = new byte[8192];
            int c;
            while ((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
            }
            out.flush();
            out.close();
            in.close();
            list.add(dest);
        }
        return list;
    }

}
