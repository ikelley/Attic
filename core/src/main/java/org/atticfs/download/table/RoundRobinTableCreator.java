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
import org.atticfs.types.DataDescription;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileSegmentHash;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * takes a MappingCollection and returns FetchRequests by constructing
 * segment requests that attempt to rebuild a whole file.
 *
 * 
 */

public class RoundRobinTableCreator extends DownloadTableCreatorImpl {

    static Logger log = Logger.getLogger("org.atticfs.download.table.RoundRobinTableCreator");

    public RoundRobinTableCreator(DownloadConfig config) {
        super(config);
    }

    protected DownloadTable createTable(DataDescription dd, SegmentedData data) {
        return new SequentialDownloadTable(dd, data, config);
    }

    protected List<EndpointRequest> matchMappings(int totalSegs, List<EndpointRequest> mappings, DownloadTable table) throws DownloadException {

        SegmentedData data = table.getTemplate();
        List<EndpointRequest> currMappings = new ArrayList<EndpointRequest>(mappings);

        int count = 0;
        while (currMappings.size() > 0) {
            log.fine(" current mappings size is " + currMappings.size());
            if (count >= currMappings.size()) {
                count = 0;
            }
            log.fine(" count is " + count);

            EndpointRequest hash = currMappings.get(count);
            log.fine(" current mappings size after removal is " + currMappings.size());
            Endpoint ep = hash.getEndpoint();

            List<FileSegmentHash> chunks = hash.getChunks();
            if (chunks.size() == 0) {
                currMappings.remove(count);
            } else {
                FileSegmentHash chunk = chunks.remove(0);
                hash.removeChunk(chunk);

                if (data.addChunk(chunk)) {
                    addPrimary(ep, chunk);
                    for (EndpointRequest currMapping : currMappings) {
                        if (currMapping.getId() != hash.getId()) {
                            FileSegmentHash currChunk = currMapping.getChunk(chunk.getHash());
                            if (currChunk != null) {
                                currMapping.removeChunk(currChunk);
                                addSecondary(currMapping.getEndpoint(), currChunk);
                            }
                        }
                    }
                } else {
                    addSecondary(ep, chunk);
                }
            }
            count++;
            DownloadTable.Status status = data.getStatus();
            log.fine("DownloadTableCreatorImpl.getRequests status:" + status);
        }
        return currMappings;
    }

    private void addPrimary(Endpoint ep, FileSegmentHash chunk) {
        EndpointRequest req = primary.get(ep);
        if (req == null) {
            List<FileSegmentHash> list = new ArrayList<FileSegmentHash>();
            list.add(chunk);
            primary.put(ep, new EndpointRequest(DownloadTable.Priority.PRIMARY, list, ep)); // we've found some chunks that are needed.
            log.fine(" added request to " + ep);
        } else {
            req.addChunk(chunk);
            primary.put(ep, req);
            log.fine(" added request to exisiting " + ep);
        }
        log.fine(" amount of requests in primary list:" + primary.size());
    }

    private void addSecondary(Endpoint ep, FileSegmentHash chunk) {
        EndpointRequest req = secondary.get(ep);
        if (req == null) {
            List<FileSegmentHash> list = new ArrayList<FileSegmentHash>();
            list.add(chunk);
            secondary.put(ep, new EndpointRequest(DownloadTable.Priority.SECONDARY, list, ep)); // we've found some chunks that are needed.
            log.fine(" added request to " + ep);
        } else {
            req.addChunk(chunk);
            secondary.put(ep, req);
            log.fine(" added request to " + ep);
        }
    }


}