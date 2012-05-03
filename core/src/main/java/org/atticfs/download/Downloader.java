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

package org.atticfs.download;

import org.atticfs.Attic;
import org.atticfs.channel.ByteRange;
import org.atticfs.download.request.AbstractRequestor;
import org.atticfs.download.request.MultipleFileRequestor;
import org.atticfs.download.request.RequestCollection;
import org.atticfs.download.request.SingleFileRequestor;
import org.atticfs.download.table.DownloadTable;
import org.atticfs.download.table.DownloadTableCreator;
import org.atticfs.download.table.DownloadTableCreatorImpl;
import org.atticfs.event.DataEvent;
import org.atticfs.event.DataReceiver;
import org.atticfs.stats.DownloadStats;
import org.atticfs.types.DataDescription;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileSegmentHash;
import org.atticfs.util.FileUtils;
import org.atticfs.util.StringConstants;
import org.wspeer.streamable.RebuiltStreamable;
import org.wspeer.streamable.StreamableFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * A Downloader brings together a number of components involved n a download.
 *
 * 
 */

public class Downloader implements Runnable {

    static Logger log = Logger.getLogger("org.atticfs.download.Downloader");


    private DataReceiver receiver;
    private RequestCollection collection;
    private DownloadTableCreator downloadTableCreator;
    private Class<? extends AbstractRequestor> requestorClass = MultipleFileRequestor.class;

    private DownloadStats stats;

    private File downloadDir;
    private File targetFile;
    private Attic attic;

    private List<Future<List<FetchResult>>> futures = new ArrayList<Future<List<FetchResult>>>();


    public Downloader(DataReceiver receiver, RequestCollection collection, File downloadDir, Attic attic) {
        if (downloadDir.exists() && !downloadDir.isDirectory()) {
            throw new IllegalArgumentException("file must either not exist or be a directory");
        }
        this.receiver = receiver;
        this.collection = collection;
        downloadDir.mkdirs();
        this.downloadDir = new File(downloadDir, createDirectoryNameFromName(collection.getDataDescription().getId()));
        this.downloadDir.mkdirs();
        this.targetFile = new File(downloadDir, createFileNameFromName(collection.getDataDescription().getId()));
        this.attic = attic;
    }

    public Class<? extends AbstractRequestor> getRequestorClass() {
        return requestorClass;
    }

    protected void setRequestorClass(Class<? extends AbstractRequestor> requestorClass) {
        this.requestorClass = requestorClass;
    }

    private AbstractRequestor createRequestor(DownloadTable table) throws DownloadException {
        try {
            Constructor c = requestorClass.getConstructor(DownloadTable.class, Downloader.class);
            return (AbstractRequestor) c.newInstance(table, this);
        } catch (Exception e) {
            throw new DownloadException("Could not create callable or type:" + requestorClass.getName());
        }
    }

    public DownloadTableCreator getTableCreator() {
        return downloadTableCreator;
    }

    public void setTableCreator(DownloadTableCreator downloadTableCreator) {
        this.downloadTableCreator = downloadTableCreator;
    }

    public DownloadStats getStats() {
        return stats;
    }

    public File getDownloadDir() {
        return downloadDir;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void download() {
        attic.execute(this);
    }

    public void run() {
        DataDescription dd = collection.getDataDescription();
        stats = new DownloadStats(dd.getId());
        if (dd == null) {
            receiver.dataArrived(new DataEvent(this, dd, "No description found in pointer", false, null, stats));
        }
        fetchData(dd, collection);
    }

    private void fetchData(DataDescription dd, RequestCollection pointers) {
        CompletionService<List<FetchResult>> ecs = new ExecutorCompletionService<List<FetchResult>>(attic.getExecutor());
        try {
            stats.setPreStartTime(System.currentTimeMillis());
            if (downloadTableCreator == null) {
                downloadTableCreator = new DownloadTableCreatorImpl(attic.getDownloadConfig());
            }
            if (attic.getDownloadConfig().isStreamToTargetFile()) {
                setRequestorClass(SingleFileRequestor.class);
            }
            DownloadTable table = downloadTableCreator.createTable(pointers);
            log.fine("Downloader.fetchData download table:\n");
            log.fine(table.toString());
            stats.setPreEndTime(System.currentTimeMillis());
            submit(ecs, table, dd);
        } catch (DownloadException e) {
            e.printStackTrace();
            stats.setPreEndTime(System.currentTimeMillis());
            receiver.dataArrived(new DataEvent(this, dd, e, stats));
        }

    }

    private void submit(CompletionService<List<FetchResult>> ecs, final DownloadTable table, final DataDescription dd) throws DownloadException {
        List<FetchResult> results = new ArrayList<FetchResult>();
        int numThreads = table.getDownloadConfig().getMaxFileConnections();
        stats.setNumThreads(numThreads);
        stats.setNumChunks(table.getTemplate().getBlockNumber());
        stats.setInitialStatus(table.getTemplate().getStatus().toString());
        for (int i = 0; i < numThreads; i++) {
            AbstractRequestor ar = createRequestor(table);
            futures.add(ecs.submit(ar));
        }
        stats.setStartTime(System.currentTimeMillis());
        try {
            for (int count = 0; count < numThreads; count++) {
                try {
                    Future<List<FetchResult>> f = ecs.take();
                    List<FetchResult> reses = f.get();
                    if (reses != null && reses.size() > 0) {
                        for (FetchResult res : reses) {
                            if (res != null) {
                                results.add(res);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    log.warning("Execution exception thrown:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } finally {
            for (Future<List<FetchResult>> future : futures) {
                log.fine("Downloader.submit canelling future...");
                future.cancel(true);
            }
        }
        stats.setEndTime(System.currentTimeMillis());
        log.fine("Downloader.submit done getting results");
        log.fine("Downloader.submit results size:" + results.size());
        log.fine("Downloader.submit about to post process...");
        stats.setFinalStatus(table.getResult().getStatus().toString());
        stats.setPostStartTime(System.currentTimeMillis());
        DataEvent evt = postProcess(results, dd, table);
        stats.setPostEndTime(System.currentTimeMillis());
        receiver.dataArrived(evt);
        try {
            FileUtils.deleteFiles(downloadDir, true);
        } catch (FileNotFoundException e) {
            log.fine(" could not remove working files.");
        }
    }

    private DataEvent postProcess(List<FetchResult> results, DataDescription dd, DownloadTable table) {

        int templNum = table.getTemplate().getBlockNumber();
        int resNum = table.getResult().getBlockNumber();
        if (templNum != resNum) {
            log.fine(" not enough chunks arrived back. Should be:" + templNum + " but is:" + resNum);
            return new DataEvent(this, dd, "not enough chunks", false, null, stats);
        }
        if (results.size() == 1) {
            Object ret = results.get(0).getResource();
            if (ret != null) {
                if (ret instanceof File) {
                    File f = (File) ret;
                    return new DataEvent(this, dd, "Get me a beer.", true, f, stats);
                } else {
                    log.fine(" GOT SOMETHING BACK WHICH IS NOT A FILE:" + ret);
                    return new DataEvent(this, dd, "GOT SOMETHING BACK WHICH IS NOT A FILE:" + ret, false, null, stats);
                }
            } else {
                log.fine(" GOT NOTHING BACK");
                return new DataEvent(this, dd, "GOT NOTHING BACK", false, null, stats);
            }
        } else {
            if (!attic.getDownloadConfig().isStreamToTargetFile()) {
                RebuiltStreamable s = new RebuiltStreamable(targetFile, "application/octet-stream");
                for (FetchResult result : results) {
                    Object ret = result.getResource();
                    if (ret != null) {
                        if (ret instanceof File) {
                            org.atticfs.channel.ByteRange range = result.getByteRange();
                            if (range != null) {
                                StreamableFile sf = new StreamableFile((File) ret);
                                try {
                                    s.addFragment(sf, range.getStartOffset(), s.getLength());
                                } catch (IOException e) {
                                    return new DataEvent(this, result.getDescription(), e, stats);
                                }
                            } else {
                                return new DataEvent(this, dd, "No range specified. Is this the whole file? ", false, (File) ret, stats);
                            }
                        } else {
                            log.fine(" GOT SOMETHING BACK WHICH IS NOT A FILE:" + ret);
                            return new DataEvent(this, dd, "GOT SOMETHING BACK WHICH IS NOT A FILE:" + ret, false, null, stats);
                        }
                    } else {
                        log.fine(" GOT NOTHING BACK");
                        return new DataEvent(this, dd, "GOT NOTHING BACK", false, null, stats);
                    }
                }
                log.fine("Downloader.postProcess status of data: " + s.getRebuildStatus());
                if (s.getRebuildStatus() != RebuiltStreamable.RebuildStatus.COMPLETE) {
                    return new DataEvent(this, dd, "File is not complete", false, targetFile, stats);
                }
            }
            long len = dd.getHash().getSize();
            if (targetFile.length() != len) {
                return new DataEvent(this, dd, "File is not complete", false, targetFile, stats);
            }
            try {
                FileChannel channel = new FileInputStream(targetFile).getChannel();
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, targetFile.length());
                Boolean b = verify(buffer, dd.getHash().getHash());
                if (b == null) {
                    return new DataEvent(this, dd, "Get me a hot coco.", false, targetFile, stats);
                } else if (b) {
                    return new DataEvent(this, dd, "Get me a beer.", true, targetFile, stats);
                } else {
                    // umm this could be anyone, but we don't know who!
                    //EndpointStats.getStats().addBadHashEndpoint();
                    return new DataEvent(this, dd, "Get me a hot coco.", false, targetFile, stats);
                }
            } catch (IOException e) {
                return new DataEvent(this, dd, "Dig me a hole.", false, targetFile, stats);
            }
        }


    }

    public String createFileNameFromName(String name) {
        return createDirectoryNameFromName(name) + StringConstants.EXT_DATA;
    }

    public String createDirectoryNameFromName(String name) {
        String better = name.replaceAll("[\\\\/:;\\{\\}?<>*&%$£@!+=|\"]", "_").toLowerCase();
        better = better.replaceAll("[\' ]", "");
        better = better.replaceAll("[_]+", "_");
        return better;
    }


    public Boolean verify(ByteBuffer in, String hash) {
        if (hash == null) {
            return true;
        }
        stats.onVerify(hash);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        int len = 8192;
        try {
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

            log.fine("Downloader.verify returning " + b);
            return b;

        } catch (Exception e) {
            return null;
        }
    }


    public static class FetchResult {
        private FileSegmentHash segment;
        private Object resource;
        private DataDescription dd;
        private Endpoint endpoint;
        private org.atticfs.channel.ByteRange byteRange;

        public FetchResult(Object resource, DataDescription dd, Endpoint endpoint) {
            this.resource = resource;
            this.dd = dd;
            this.endpoint = endpoint;
        }

        public FetchResult(Object resource, DataDescription dd, Endpoint endpoint, FileSegmentHash segment) {
            this.resource = resource;
            this.dd = dd;
            this.endpoint = endpoint;
            this.segment = segment;
        }

        public FetchResult(DataDescription dd, Endpoint endpoint) {
            this.dd = dd;
            this.endpoint = endpoint;
        }

        public FileSegmentHash getSegment() {
            return segment;
        }

        public void setSegment(FileSegmentHash segment) {
            this.segment = segment;
        }

        public Object getResource() {
            return resource;
        }

        public DataDescription getDescription() {
            return dd;
        }

        public Endpoint getHost() {
            return endpoint;
        }

        public ByteRange getByteRange() {
            return byteRange;
        }

        public void setByteRange(ByteRange byteRange) {
            this.byteRange = byteRange;
        }
    }


}