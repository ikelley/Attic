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
import org.atticfs.download.request.EndpointRequest;
import org.atticfs.download.request.SegmentRequest;
import org.atticfs.types.DataDescription;
import org.atticfs.types.FileSegmentHash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Class Description Here...
 *
 * 
 */

public class EndpointAwareDownloadTable extends AbstractDownloadTable {

    private PriorityBlockingQueue<SegmentRequest>[] requests;
    private SegmentRequestComparator segComp = new SegmentRequestComparatorImpl();

    private Map<String, FileSegmentHash> processing = new ConcurrentHashMap<String, FileSegmentHash>();
    // request endpoint ids mapped to queues in the array
    private Map<Long, Integer> queueEndpoints = new HashMap<Long, Integer>();
    private int numThreads;
    private volatile int nextQueue;

    @SuppressWarnings("unchecked")
    public EndpointAwareDownloadTable(DataDescription description, SegmentedData template, DownloadConfig config) {
        super(description, template, config);
        this.numThreads = config.getMaxFileConnections();
        nextQueue = numThreads - 1;
        requests = new PriorityBlockingQueue[numThreads];
        for (int i = 0; i < requests.length; i++) {
            requests[i] = new PriorityBlockingQueue<SegmentRequest>(50, segComp);
        }
    }

    @SuppressWarnings("unchecked")
    public EndpointAwareDownloadTable(DataDescription description, SegmentedData template, DownloadConfig config, SegmentRequestComparator comparator) {
        super(description, template, config);
        this.numThreads = config.getMaxFileConnections();
        nextQueue = numThreads - 1;
        requests = new PriorityBlockingQueue[numThreads];
        this.segComp = comparator;
        for (int i = 0; i < requests.length; i++) {
            requests[i] = new PriorityBlockingQueue<SegmentRequest>(50, segComp);
        }
    }

    private int getNextQueue() {
        nextQueue++;
        if (nextQueue == numThreads) {
            nextQueue = 0;
        }
        return nextQueue;
    }

    protected void addPrimaryRequest(EndpointRequest request) {
        if (queueEndpoints.get(request.getId()) != null) {
            return; // this request has already been processed
        }
        int next = getNextQueue();
        if (request.getChunks().size() > 0) {
            List<FileSegmentHash> chunks = request.getChunks();
            for (FileSegmentHash chunk : chunks) {
                SegmentRequest sr = new SegmentRequest(request.getEndpoint(), chunk, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                sr.setPriority(request.getPriority());
                requests[next].add(sr);
            }
        } else {
            SegmentRequest sr = new SegmentRequest(request.getEndpoint(), null, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
            requests[next].add(sr);
        }
        queueEndpoints.put(request.getId(), next);

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
            Integer queueNum;
            if (endpointRequestId < 0) {
                queueNum = getNextQueue();
            } else {
                queueNum = queueEndpoints.get(endpointRequestId);
                if (queueNum == null || queueNum >= numThreads) {
                    return DownloadTable.poison;
                }
            }
            SegmentRequest req = requests[queueNum].take();
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
                Integer queueNum = queueEndpoints.get(request.getId());
                if (queueNum == null) {
                    queueNum = getNextQueue();
                    queueEndpoints.put(request.getId(), queueNum);
                }
                requests[queueNum].add(sr);
            }
        } else {
            boolean found = false;
            for (EndpointRequest request : secondary) {
                FileSegmentHash other = request.getChunk(hash.getHash());
                if (other != null) {
                    SegmentRequest sr = new SegmentRequest(request.getEndpoint(), other, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                    found = true;
                    Integer queueNum = queueEndpoints.get(request.getId());
                    if (queueNum == null) {
                        queueNum = getNextQueue();
                        queueEndpoints.put(request.getId(), queueNum);
                    }
                    requests[queueNum].add(sr);
                }
            }
            if (!found) {
                for (EndpointRequest request : tertiary) {
                    FileSegmentHash other = request.getChunk(hash.getHash());
                    if (other != null) {
                        SegmentRequest sr = new SegmentRequest(request.getEndpoint(), other, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                        found = true;
                        Integer queueNum = queueEndpoints.get(request.getId());
                        if (queueNum == null) {
                            queueNum = getNextQueue();
                            queueEndpoints.put(request.getId(), queueNum);
                        }
                        requests[queueNum].add(sr);
                    }
                }
            }
            if (!found) {
                if (tertiary.size() > 0) {
                    EndpointRequest request = tertiary.remove();
                    SegmentRequest sr = new SegmentRequest(request.getEndpoint(), hash, false, request.getPriority(), request.getId(), config.getDownloadChunkSize());
                    found = true;
                    Integer queueNum = queueEndpoints.get(request.getId());
                    if (queueNum == null) {
                        queueNum = getNextQueue();
                        queueEndpoints.put(request.getId(), queueNum);
                    }
                    requests[queueNum].add(sr);
                }
            }
        }
    }

    private void checkFinished() {
        if (complete || template.equals(result) || (secondary.size() == 0 && tertiary.size() == 0 && queuesAreEmpty())) {
            log.fine("=============EndpointAwareDownloadTable.checkFinished I AM FINISHED============");
            for (PriorityBlockingQueue<SegmentRequest> request : requests) {
                for (int i = 0; i < numThreads * 2; i++) {
                    request.put(DownloadTable.poison);
                }
            }

        }
    }

    private boolean queuesAreEmpty() {
        for (PriorityBlockingQueue<SegmentRequest> request : requests) {
            if (request.size() > 0) {
                return false;
            }
        }
        return true;
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
                    if (result.getStatus() == Status.COMPLETE || result.equals(template)) {
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
        findDupicate(request.getFileSegmentHash());
        if (request.getFileSegmentHash() != null) {
            processing.remove(request.getHash());
        }
        checkFinished();

    }

    @Override
    protected String primaryRequests() {
        StringBuilder sb = new StringBuilder();
        for (PriorityBlockingQueue<SegmentRequest> request : requests) {
            for (SegmentRequest sr : request) {
                sb.append("\n").append(sr);
            }
        }

        return sb.toString();

    }


}