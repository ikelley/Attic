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

package org.atticfs.download.table;

import org.atticfs.config.download.DownloadConfig;
import org.atticfs.download.DownloadException;
import org.atticfs.download.request.Chunk;
import org.atticfs.download.request.EndpointRequest;
import org.atticfs.download.request.SegmentRequest;
import org.atticfs.types.DataDescription;
import org.atticfs.types.FileSegmentHash;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Class Description Here...
 *
 * 
 */

public class SimpleDownloadTable extends AbstractDownloadTable {

    protected PriorityBlockingQueue<SegmentRequest> requests;

    protected Map<String, FileSegmentHash> processing = new ConcurrentHashMap<String, FileSegmentHash>();

    public SimpleDownloadTable(DataDescription description, SegmentedData template, DownloadConfig config) {
        super(description, template, config);
        requests = new PriorityBlockingQueue<SegmentRequest>(50, new SegmentRequestComparatorImpl());

    }

    public SimpleDownloadTable(DataDescription description, SegmentedData template, DownloadConfig config, SegmentRequestComparator comparator) {
        super(description, template, config);
        requests = new PriorityBlockingQueue<SegmentRequest>(50, comparator);
    }


    protected void addPrimaryRequest(EndpointRequest request) {

        if (request.getChunks().size() > 0) {
            log.info(" adding primary request with endpoint:" + request.getEndpoint());
            List<FileSegmentHash> chunks = request.getChunks();
            for (FileSegmentHash chunk : chunks) {
                SegmentRequest sr = new SegmentRequest(request.getEndpoint(), chunk, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                requests.add(sr);
            }
        } else {
            SegmentRequest sr = new SegmentRequest(request.getEndpoint(), null, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
            requests.add(sr);
        }
    }

    protected void addSecondaryRequest(EndpointRequest request) {

        if (request.getChunks().size() > 0) {
            AbstractDownloadTable.log.fine(" adding request:" + request);
            secondary.add(request);
        }
    }

    protected void addTertiaryRequest(EndpointRequest request) {
        List<FileSegmentHash> segs = request.getChunks();
        for (FileSegmentHash seg : segs) {
            if (result.containsChunk(seg)) {
                AbstractDownloadTable.log.fine("removing completed chunk from request:" + seg);
                request.completed(seg);
            }
        }
        tertiary.add(request);
    }

    public SegmentRequest next(long endpointRequestId) {
        if (complete) {
            return DownloadTable.poison;
        }
        try {
            SegmentRequest req = requests.take();
            synchronized (this) {
                if (req.getFileSegmentHash() != null) {
                    processing.put(req.getHash(), req.getFileSegmentHash());
                }
            }
            return req;
        } catch (InterruptedException e) {

        }
        return DownloadTable.poison;
    }

    private void clearTable(FileSegmentHash hash) {
        for (EndpointRequest request : secondary) {
            request.completed(hash);
        }
    }

    private void findDupicate(FileSegmentHash hash) {
        if (hash == null) {
            if (tertiary.size() > 0) {
                EndpointRequest request = tertiary.remove();
                SegmentRequest sr = new SegmentRequest(request.getEndpoint(), null, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                requests.add(sr);
            }
        } else {
            boolean found = false;
            Iterator<EndpointRequest> it = secondary.iterator();
            while (it.hasNext()) {
                EndpointRequest request = it.next();
                if (request.isEmpty()) {
                    it.remove();
                } else {
                    FileSegmentHash other = request.removeChunk(hash.getHash());
                    if (other != null) {
                        SegmentRequest sr = new SegmentRequest(request.getEndpoint(), other, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                        found = true;
                        requests.add(sr);
                    }
                }
            }
            if (!found) {
                it = tertiary.iterator();
                while (it.hasNext()) {
                    EndpointRequest request = it.next();
                    if (request.isEmpty()) {
                        it.remove();
                    } else {
                        FileSegmentHash other = request.removeChunk(hash.getHash());
                        if (other != null) {
                            SegmentRequest sr = new SegmentRequest(request.getEndpoint(), other, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                            found = true;
                            requests.add(sr);
                        }
                    }
                }
            }
            if (!found) {
                if (tertiary.size() > 0) {
                    EndpointRequest request = tertiary.remove();
                    SegmentRequest sr = new SegmentRequest(request.getEndpoint(), hash, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                    found = true;
                    requests.add(sr);
                }
            }
        }
    }

    private void checkFinished() {
        if (complete || template.equals(result) || (secondary.size() == 0 && tertiary.size() == 0 && requests.size() == 0)) {
            log.fine("=============SimpleDownloadTable.checkFinished I AM FINISHED============");
            for (int i = 0; i < config.getMaxFileConnections() * 2; i++) {
                requests.put(DownloadTable.poison);
            }
        }
    }


    public synchronized void onSuccess(SegmentRequest request) {
        if (request.getFileSegmentHash() == null) {
            complete = true;
            try {
                result.addChunks(template.getChunks());
            } catch (DownloadException e) {

            }
        } else {
            try {
                if (result.addChunk(request.getFileSegmentHash())) {
                    AbstractDownloadTable.log.fine(" yipeee! added segment:" + request.getFileSegmentHash());
                    if (result.getStatus() == DownloadTable.Status.COMPLETE || result.equals(template)) {
                        AbstractDownloadTable.log.fine("result is complete!!!!!");
                        this.complete = true;
                    }
                    clearTable(request.getFileSegmentHash());
                }
                processing.remove(request.getHash());

            } catch (DownloadException e) {
                e.printStackTrace();
            }
        }
        checkFinished();
    }

    public synchronized void onFailure(SegmentRequest request) {
        if (request.getRetries() < config.getRetryCount()) {
            List<Chunk> chunks = request.getDownloadChunks();
            for (Chunk chunk : chunks) {
                // clear the failed state from the chunk if we are trying again later
                if (chunk.getState() == Chunk.State.FAILED) {
                    chunk.setState(Chunk.State.UNTRIED);
                }
            }
            request.incRetries();
            requests.add(request);
        }
        findDupicate(request.getFileSegmentHash());
        if (request.getFileSegmentHash() != null) {
            processing.remove(request.getHash());

        }
        checkFinished();

    }

    @Override
    protected String primaryRequests() {
        StringBuilder sb = new StringBuilder();
        for (SegmentRequest request : requests) {
            sb.append("\n").append(request);
        }
        return sb.toString();

    }


}
