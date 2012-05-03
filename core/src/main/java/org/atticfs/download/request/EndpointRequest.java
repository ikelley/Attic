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

package org.atticfs.download.request;

import org.atticfs.download.table.DownloadTable;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileSegmentHash;

import java.util.*;


/**
 * Represents a series of chunks available at a certain endpoint
 *
 * 
 */
public class EndpointRequest {


    private Map<String, FileSegmentHash> chunks = new HashMap<String, FileSegmentHash>();
    private Map<String, FileSegmentHash> completed = new HashMap<String, FileSegmentHash>();

    private static Random r = new Random();

    private Endpoint endpoint = null;
    private boolean failed = false;
    private DownloadTable.Priority priority = DownloadTable.Priority.UNKNOWN;
    private long id;
    private long receiveTime = Long.MAX_VALUE;


    public EndpointRequest(DownloadTable.Priority priority, List<FileSegmentHash> chunks, Endpoint endpoint, long receiveTime) {
        this.priority = priority;
        for (FileSegmentHash chunk : chunks) {
            if (chunk.getHash() != null) {
                this.chunks.put(chunk.getHash(), chunk);
            }
        }
        this.endpoint = endpoint;
        this.receiveTime = receiveTime;
        this.id = r.nextLong();
    }


    public EndpointRequest(DownloadTable.Priority priority, List<FileSegmentHash> chunks, Endpoint endpoint) {
        this(priority, chunks, endpoint, Long.MAX_VALUE);
    }

    public EndpointRequest(List<FileSegmentHash> chunks, Endpoint endpoint) {
        this(DownloadTable.Priority.UNKNOWN, chunks, endpoint, Long.MAX_VALUE);
    }

    public EndpointRequest(List<FileSegmentHash> chunks, Endpoint endpoint, long receiveTime) {
        this(DownloadTable.Priority.UNKNOWN, chunks, endpoint, receiveTime);
    }

    public EndpointRequest(Endpoint endpoint) {
        this(DownloadTable.Priority.UNKNOWN, new ArrayList<FileSegmentHash>(), endpoint, Long.MAX_VALUE);
    }

    public EndpointRequest(Endpoint endpoint, long receiveTime) {
        this(DownloadTable.Priority.UNKNOWN, new ArrayList<FileSegmentHash>(), endpoint, receiveTime);
    }

    public long getId() {
        return id;
    }

    public List<FileSegmentHash> getChunks() {
        return new ArrayList<FileSegmentHash>(chunks.values());
    }

    public void setChunks(List<FileSegmentHash> chunks) {
        for (FileSegmentHash chunk : chunks) {
            if (chunk.getHash() != null) {
                this.chunks.put(chunk.getHash(), chunk);
            }
        }
    }

    public DownloadTable.Priority getPriority() {
        return priority;
    }

    public void setPriority(DownloadTable.Priority priority) {
        this.priority = priority;
    }

    public void addChunk(FileSegmentHash hash) {
        chunks.put(hash.getHash(), hash);
    }

    public FileSegmentHash removeChunk(FileSegmentHash hash) {
        return chunks.remove(hash.getHash());
    }

    public FileSegmentHash removeChunk(String hash) {
        return chunks.remove(hash);
    }

    public void completed(FileSegmentHash hash) {
        chunks.remove(hash.getHash());
        completed.put(hash.getHash(), hash);
    }

    public boolean isEmpty() {
        return chunks.size() == 0;
    }

    public List<FileSegmentHash> getCompleted() {
        return new ArrayList<FileSegmentHash>(completed.values());
    }

    public FileSegmentHash getChunk(String hash) {
        return chunks.get(hash);
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName() + " id:" + id + " priority:" + priority + " endpoint:" + endpoint + "\nsegments:\n");
        for (FileSegmentHash fsh : chunks.values()) {
            sb.append(fsh).append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndpointRequest that = (EndpointRequest) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
