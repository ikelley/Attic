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
import org.atticfs.types.Endpoint;

import java.util.Comparator;

/**
 * Class Description Here...
 *
 * 
 */

public interface DownloadTable {


    static enum Priority {
        POISON,             // top priority. This kills off the threads waiting on the queue.
        PRIMARY,
        SECONDARY,
        TERTIARY,
        UNKNOWN
    }

    static enum Status {
        EMPTY,              // nothing has been added yet
        DISCONTINUOUS,      // there are gaps between the blocks
        CONTINUOUS,         // the blocks are continuous but they do not reach to the end, or the end is not known
        COMPLETE,           // the blocks are continuous and reach the known end.
    }

    SegmentRequest poison = new SegmentRequest(new Endpoint("poison"), null, true, Priority.POISON, -1, -1);

    public void addRequest(EndpointRequest request);

    /**
     * get the next SegmentRequest for processing
     *
     * @param endpointRequestId this is the id of the EndpointRequest from which the segmentRequest is created.
     *                          Clients should use this to inform the table what endpoint request has just been processed.
     *                          Clients should use -1 to indicate no Endpoint request id is known.
     * @return
     */
    public SegmentRequest next(long endpointRequestId);

    public void onSuccess(SegmentRequest request);

    public void onFailure(SegmentRequest request);

    public SegmentedData getTemplate();

    public SegmentedData getResult();

    public boolean isComplete();

    public DataDescription getDescription();

    public DownloadConfig getDownloadConfig();

    static class EndpointRequestComparator implements Comparator<EndpointRequest> {

        public int compare(EndpointRequest request, EndpointRequest request1) {
            Priority p = request.getPriority();
            Priority p1 = request1.getPriority();
            if (p.ordinal() < p1.ordinal()) {
                return -1;
            } else if (p.ordinal() > p1.ordinal()) {
                return 1;
            } else {
                if (request.getReceiveTime() < request1.getReceiveTime()) {
                    return -1;
                } else if (request.getReceiveTime() > request1.getReceiveTime()) {
                    return 1;
                } else {
                    if (request.getId() < request1.getId()) {
                        return -1;
                    } else if (request.getId() > request1.getId()) {
                        return 1;
                    }
                    return 0;
                }

            }

        }
    }

    static interface SegmentRequestComparator extends Comparator<SegmentRequest> {

    }

    static class SegmentRequestComparatorImpl implements SegmentRequestComparator {

        public int compare(SegmentRequest request, SegmentRequest request1) {
            Priority p = request.getPriority();
            Priority p1 = request1.getPriority();
            if (p.ordinal() < p1.ordinal()) {
                return -1;
            } else if (p.ordinal() > p1.ordinal()) {
                return 1;
            } else {
                if (request.getRetries() < request1.getRetries()) {
                    return -1;
                } else if (request.getRetries() > request1.getRetries()) {
                    return 1;
                } else {
                    if (request.getId() < request1.getId()) {
                        return -1;
                    } else if (request.getId() > request1.getId()) {
                        return 1;
                    }
                    return 0;
                }
            }

        }
    }
}