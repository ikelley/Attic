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
import org.atticfs.download.request.EndpointRequest;
import org.atticfs.download.request.SegmentRequest;
import org.atticfs.types.DataDescription;
import org.atticfs.types.FileSegmentHash;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public abstract class AbstractDownloadTable implements DownloadTable {

    static Logger log = Logger.getLogger("org.atticfs.download.table.AbstractDownloadTable");


    // the segmented data to compare results to.
    protected SegmentedData template;
    // the segmented data being constructed
    protected SegmentedData result;

    protected volatile boolean complete = false;

    protected PriorityQueue<EndpointRequest> secondary = new PriorityQueue<EndpointRequest>(50, new DownloadTable.EndpointRequestComparator());
    protected PriorityQueue<EndpointRequest> tertiary = new PriorityQueue<EndpointRequest>(50, new DownloadTable.EndpointRequestComparator());

    protected DataDescription description;
    protected DownloadConfig config;

    public AbstractDownloadTable(DataDescription description, SegmentedData template, DownloadConfig config) {
        this.description = description;
        this.template = template;
        log.fine("received table has a status of " + template.getStatus());
        this.result = new SegmentedData(template.getLength());
        this.config = config;
    }


    public synchronized void addRequest(EndpointRequest request) {
        List<FileSegmentHash> segs = request.getChunks();
        for (FileSegmentHash seg : segs) {
            if (result.containsChunk(seg)) {
                log.fine("removing completed chunk from request:" + seg);
                request.completed(seg);
            }
        }
        switch (request.getPriority()) {
            case PRIMARY:
                addPrimaryRequest(request);
                break;
            case SECONDARY:
                addSecondaryRequest(request);
                break;
            case TERTIARY:
                addTertiaryRequest(request);
                break;
            default:
                break;
        }
    }


    protected abstract void addPrimaryRequest(EndpointRequest request);

    protected abstract void addSecondaryRequest(EndpointRequest request);

    protected abstract void addTertiaryRequest(EndpointRequest request);

    public abstract SegmentRequest next(long endpointRequestId);


    public abstract void onSuccess(SegmentRequest request);

    public abstract void onFailure(SegmentRequest request);

    public SegmentedData getTemplate() {
        return template;
    }

    public SegmentedData getResult() {
        return result;
    }

    public boolean isComplete() {
        return complete;
    }


    public DataDescription getDescription() {
        return description;
    }

    public DownloadConfig getDownloadConfig() {
        return config;
    }

    protected abstract String primaryRequests();


    public String toString() {
        StringBuilder sb = new StringBuilder("DownloadTable status:");
        sb.append(getResult().getStatus()).append("\nprimary queue:\n");
        sb.append(primaryRequests());
        sb.append("\nsecondary queue:\n");
        EndpointRequest[] arr = secondary.toArray(new EndpointRequest[secondary.size()]);
        Arrays.sort(arr, new DownloadTable.EndpointRequestComparator());
        for (EndpointRequest endpointRequest : arr) {
            sb.append(endpointRequest).append("\n");
        }
        sb.append("data:\n");
        List<FileSegmentHash> hashes = result.getChunks();
        for (FileSegmentHash hash : hashes) {
            sb.append(hash).append("\n");
        }
        return sb.toString();

    }


}