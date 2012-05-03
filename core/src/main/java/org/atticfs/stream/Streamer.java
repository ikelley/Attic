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

package org.atticfs.stream;

import org.atticfs.Attic;
import org.atticfs.download.DownloadException;
import org.atticfs.download.request.RequestCollection;
import org.atticfs.download.request.SegmentRequest;
import org.atticfs.download.table.DownloadTable;
import org.atticfs.download.table.DownloadTableCreator;
import org.atticfs.download.table.RoundRobinTableCreator;
import org.atticfs.stats.DownloadStats;
import org.atticfs.types.DataDescription;
import org.atticfs.types.FileSegmentHash;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * 
 */

public class Streamer implements Runnable, StreamSource {

    static Logger log = Logger.getLogger("org.atticfs.stream.Streamer");

    private StreamSink sink;
    private RequestCollection collection;
    private DownloadTableCreator downloadTableCreator;
    private CompletionService<StreamFetchResult> ecs;

    private DownloadStats stats;
    private DownloadTable table;

    private Attic attic;

    private volatile boolean done = false;

    private boolean attemptVerification = false;
    private int maxBuffer = 0;

    private List<Future<StreamFetchResult>> futures = new ArrayList<Future<StreamFetchResult>>();

    public Streamer(RequestCollection collection, Attic attic) {
        this.collection = collection;
        this.attic = attic;
        this.ecs = new ExecutorCompletionService<StreamFetchResult>(this.attic.getExecutor());
        this.attemptVerification = attic.getStreamConfig().isAttemptVerification();
        this.maxBuffer = attic.getStreamConfig().getMaxBufferSize();
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

    public void stream() {
        attic.execute(this);
    }

    public void run() {
        DataDescription dd = collection.getDataDescription();
        stats = new DownloadStats(dd.getId());
        if (dd == null) {
            notifyStreamArrived(new StreamEvent(this, dd, "No description found in pointer", false, null, -1, -1, stats));
        }
        fetchData(dd, collection);
    }

    private void fetchData(DataDescription dd, RequestCollection pointers) {
        try {
            stats.setPreStartTime(System.currentTimeMillis());
            if (downloadTableCreator == null) {
                downloadTableCreator = new RoundRobinTableCreator(attic.getDownloadConfig());
            }
            table = downloadTableCreator.createTable(pointers);
            log.fine("Downloader.fetchData download table:\n");
            log.fine(table.toString());
            stats.setPreEndTime(System.currentTimeMillis());
            submit(dd);
        } catch (DownloadException e) {
            e.printStackTrace();
            stats.setPreEndTime(System.currentTimeMillis());
            notifyStreamArrived(new StreamEvent(this, dd, e, stats));
        }

    }

    private void submit(final DataDescription dd) throws DownloadException {
        int numThreads = table.getDownloadConfig().getMaxFileConnections();
        stats.setNumThreads(numThreads);
        stats.setNumChunks(table.getTemplate().getBlockNumber());
        stats.setInitialStatus(table.getTemplate().getStatus().toString());
        stats.setStartTime(System.currentTimeMillis());

        for (int i = 0; i < numThreads; i++) {
            futures.add(ecs.submit(new StreamRequestor(table, stats, attemptVerification, maxBuffer)));
        }

        for (int count = 0; count < numThreads; count++) {
            try {
                Future<StreamFetchResult> f = ecs.take();
                futures.remove(f);
                StreamFetchResult sfr = f.get();
                if (sfr != null) {
                    boolean b = sfr.isSuccess();
                    if (!b) {
                        table.onFailure(sfr.getRequest());
                        futures.add(ecs.submit(new StreamRequestor(table, stats, attemptVerification, maxBuffer)));
                    } else {
                        StreamEvent event;
                        if (sfr.getRequest().getFileSegmentHash() == null) {
                            event = new StreamEvent(this, dd, "got a stream", true, sfr.getStream(), 0L, -1L, stats);
                        } else {
                            FileSegmentHash fsh = sfr.getRequest().getFileSegmentHash();
                            event = new StreamEvent(this, dd, "got a stream", true, sfr.getStream(), fsh.getStartOffset(), fsh.getEndOffset(), stats);
                        }
                        table.onSuccess(sfr.getRequest());
                        notifyStreamArrived(event);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                log.warning("Execution exception thrown:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void finish() {
        if (done) {
            return;
        }
        done = true;
        stats.setEndTime(System.currentTimeMillis());
        log.fine("Downloader.submit done getting results");
        log.fine("Downloader.submit about to post process...");
        stats.setFinalStatus(table.getResult().getStatus().toString());
        stats.setPostStartTime(System.currentTimeMillis());
        stats.setPostEndTime(System.currentTimeMillis());
        notifyStreamFinished(new StreamEvent(this, collection.getDataDescription(), "EOF", true, null, -1, -1, stats));
        for (Future<StreamFetchResult> future : futures) {
            log.fine("Downloader.submit canelling future...");
            future.cancel(true);
        }

    }

    protected void notifyStreamArrived(StreamEvent event) {
        if (sink != null) {
            sink.streamArrived(event);
        }
    }

    protected void notifyStreamFinished(StreamEvent event) {
        if (sink != null) {
            sink.streamsFinished(event);
        }
    }

    public void setSink(StreamSink sink) {
        this.sink = sink;
    }

    public synchronized void streamExhaused(StreamEvent event) {
        log.fine("Streamer.streamExhaused called");
        if (done) {
            log.fine("Streamer.streamExhaused I'm done");
            return;
        }
        if (table.isComplete()) {
            log.fine("Streamer.streamExhaused table is complete");
            finish();
        }
        log.fine("Streamer.streamExhaused adding future");

        futures.add(ecs.submit(new StreamRequestor(table, stats, attemptVerification, maxBuffer)));
        try {
            Future<StreamFetchResult> f = ecs.take();
            futures.remove(f);
            StreamFetchResult sfr = f.get();
            if (sfr != null) {
                boolean b = sfr.isSuccess();
                if (!b) {
                    table.onFailure(sfr.getRequest());
                    futures.add(ecs.submit(new StreamRequestor(table, stats, attemptVerification, maxBuffer)));
                } else {
                    if (sfr.getRequest().getFileSegmentHash() == null) {
                        event = new StreamEvent(this, collection.getDataDescription(), "got a stream", true, sfr.getStream(), 0L, -1L, stats);
                    } else {
                        FileSegmentHash fsh = sfr.getRequest().getFileSegmentHash();
                        event = new StreamEvent(this, collection.getDataDescription(), "got a stream", true, sfr.getStream(), fsh.getStartOffset(), fsh.getEndOffset(), stats);
                    }
                    table.onSuccess(sfr.getRequest());
                    notifyStreamArrived(event);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            log.warning("Execution exception thrown:" + e.getMessage());
            e.printStackTrace();
        }

    }

    public void streamsClosed() {
        finish();
    }

    public void nextStream() {
        StreamEvent evt = new StreamEvent(this, -1, null, "", false, null, -1, -1, null);
        streamExhaused(evt);
    }

    public static class StreamFetchResult {
        private SegmentRequest request;
        private InputStream stream;
        private boolean success;

        public StreamFetchResult(SegmentRequest request, InputStream stream, boolean success) {
            this.request = request;
            this.stream = stream;
            this.success = success;
        }

        public SegmentRequest getRequest() {
            return request;
        }

        public InputStream getStream() {
            return stream;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}