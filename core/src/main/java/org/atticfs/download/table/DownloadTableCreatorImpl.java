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
import org.atticfs.download.request.RequestCollection;
import org.atticfs.types.DataDescription;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileHash;
import org.atticfs.types.FileSegmentHash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * takes a MappingCollection and returns FetchRequests by constructing
 * segment requests that attempt to rebuild a whole file.
 *
 * 
 */

public class DownloadTableCreatorImpl implements DownloadTableCreator {

    static Logger log = Logger.getLogger("org.atticfs.download.table.DownloadTableCreatorImpl");

    protected Map<Endpoint, EndpointRequest> primary = new HashMap<Endpoint, EndpointRequest>();
    protected Map<Endpoint, EndpointRequest> secondary = new HashMap<Endpoint, EndpointRequest>();

    protected DownloadConfig config;
    protected DownloadTable table;


    public DownloadTableCreatorImpl(DownloadConfig config) {
        this.config = config;
    }

    public DownloadTable createTable(RequestCollection collection) throws DownloadException {

        FileHash fileHash = collection.getDataDescription().getHash();
        if (fileHash == null) {
            throw new DownloadException("No hash of data defined. I need the length from that.");
        }
        long length = fileHash.getSize();
        if (length <= 0) {
            throw new DownloadException("No length of data defined. It can't be zero - surely.");
        }
        List<EndpointRequest> mappings = collection.getMappings();
        if (mappings.size() == 0) {
            mappings = collection.getReserveMappings();
            if (mappings.size() == 0) {
                throw new DownloadException("No mappings defined. no-one to request from.");
            }
        }


        int totalSegs = fileHash.getNumChunks();
        log.fine("totalSegments:" + totalSegs);
        SegmentedData data = new SegmentedData(length);
        table = createTable(collection.getDataDescription(), data);

        if (mappings.size() == 1) {
            log.fine("DownloadTableCreatorImpl.getRequests only one hash mapping. Extracting from that.");
            return oneRequest(mappings.get(0));
        }

        while (mappings.size() > 0) {
            System.out.println("DownloadTableCreatorImpl.createTable doing mappings");
            mappings = matchMappings(totalSegs, mappings, table);
        }
        mappings = collection.getReserveMappings();
        if (mappings.size() > 0) {
            mappings = matchMappings(totalSegs, mappings, table);
        }
        return onComplete(collection);

    }

    protected DownloadTable createTable(DataDescription dd, SegmentedData data) {
        return new SimpleDownloadTable(dd, data, config);
    }

    protected List<EndpointRequest> matchMappings(int totalSegs, List<EndpointRequest> mappings, DownloadTable table) throws DownloadException {

        SegmentedData data = table.getTemplate();
        List<EndpointRequest> currMappings = new ArrayList<EndpointRequest>(mappings);
        mappings = new ArrayList<EndpointRequest>();

        int cap = totalSegs / currMappings.size();
        int leftOver = totalSegs % currMappings.size();
        log.info(" total segs:" + totalSegs);
        log.info(" cap:" + cap);
        log.info(" left over:" + leftOver);
        log.info(" total:" + ((cap * currMappings.size()) + leftOver));
        int currIndex = 0;
        while (currMappings.size() > 0 && currIndex < currMappings.size()) {
            log.fine(" current mappings size is " + currMappings.size());
            EndpointRequest hash = currMappings.remove(currIndex);
            log.fine(" current mappings size after removal is " + currMappings.size());
            Endpoint ep = hash.getEndpoint();
            List<FileSegmentHash> chunks = hash.getChunks();
            List<FileSegmentHash> needed = new ArrayList<FileSegmentHash>();
            List<FileSegmentHash> duplicate = new ArrayList<FileSegmentHash>();
            int currCap = cap;
            int added = 0;
            if (leftOver > 0) {
                currCap++;
                leftOver--;
            }

            for (int i = 0; i < chunks.size(); i++) {
                FileSegmentHash chunk = chunks.get(i);
                //log.fine(" getting next chunk:" + chunk);
                if (data.addChunk(chunk)) {
                    needed.add(chunk);
                    added++;
                    //log.fine(" added chunk:" + chunk);
                    if (added == currCap) {
                        break;
                    }
                } else {
                    duplicate.add(chunk);
                }

            }
            for (FileSegmentHash fileSegmentHash : needed) {
                hash.removeChunk(fileSegmentHash); // remove those chunks we added. Keep the other chunks as backup
            }
            for (FileSegmentHash fileSegmentHash : duplicate) {
                hash.removeChunk(fileSegmentHash); // remove those chunks we added. Keep the other chunks as backup
            }
            chunks = hash.getChunks();
            if (chunks.size() > 0) {
                log.fine(" there are some chunks left in current hash:" + chunks.size() + " so these are being added to be reprocessed");
                mappings.add(currIndex, hash); // save the mapping if it still has chunks
            }
            if (needed.size() > 0) {
                log.fine(" we have got some chunks to add");
                EndpointRequest req = primary.get(ep);
                if (req == null) {
                    primary.put(ep, new EndpointRequest(DownloadTable.Priority.PRIMARY, needed, ep)); // we've found some chunks that are needed.
                    log.fine(" added request to " + ep);
                } else {
                    for (FileSegmentHash fileSegmentHash : needed) {
                        req.addChunk(fileSegmentHash);
                    }
                    primary.put(ep, req);
                    log.fine(" added request to " + ep);
                }
            }
            if (duplicate.size() > 0) {
                log.fine(" we have got some chunks to add");
                EndpointRequest req = secondary.get(ep);
                if (req == null) {
                    secondary.put(ep, new EndpointRequest(DownloadTable.Priority.SECONDARY, duplicate, ep)); // we've found some chunks that are needed.
                    log.fine(" added request");
                } else {
                    for (FileSegmentHash fileSegmentHash : duplicate) {
                        req.addChunk(fileSegmentHash);
                    }
                    secondary.put(ep, req);
                    log.fine(" added request");
                }
            }
            DownloadTable.Status status = data.getStatus();
            log.info("DownloadTableCreatorImpl.getRequests status:" + status);

            currIndex++;
        }
        if (currMappings.size() > 0) {
            log.fine(" There are some mappings we didn't look at. Adding these to be re processed. there are: " + currMappings.size() + " left");
            for (int i = currMappings.size() - 1; i >= 0; i--) {
                EndpointRequest hm = currMappings.get(i);
                mappings.add(0, hm); // now save all those we never even looked at (hopefully) at the correct index :-)
            }
        }
        return mappings;
    }

    protected DownloadTable onComplete(RequestCollection collection) {

        for (EndpointRequest endpointRequest : primary.values()) {
            table.addRequest(endpointRequest);
        }
        for (EndpointRequest endpointRequest : secondary.values()) {
            table.addRequest(endpointRequest);
        }
        List<EndpointRequest> reserve = collection.getReserveMappings();
        for (EndpointRequest req : reserve) {
            req.setPriority(DownloadTable.Priority.TERTIARY);
            table.addRequest(req);
        }
        return table;
    }


    protected DownloadTable oneRequest(EndpointRequest request) throws DownloadException {
        SegmentedData data = table.getTemplate();
        log.fine("request:" + request);
        log.fine("segments data status:" + data.getStatus());
        log.fine("chunks:" + request.getChunks().size());
        data.addChunks(request.getChunks());
        if (data.getStatus() == DownloadTable.Status.COMPLETE) {
            table.addRequest(new EndpointRequest(DownloadTable.Priority.PRIMARY, new ArrayList<FileSegmentHash>(), request.getEndpoint()));
        } else {
            table.addRequest(request);
        }
        return table;
    }


}
