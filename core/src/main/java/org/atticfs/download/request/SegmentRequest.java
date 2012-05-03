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

import org.atticfs.channel.ByteRange;
import org.atticfs.download.table.DownloadTable;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileSegmentHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class SegmentRequest {

    static Logger log = Logger.getLogger("org.atticfs.download.request.SegmentRequest");


    private static Random r = new Random();

    private Endpoint endpoint;
    private FileSegmentHash fileSegmentHash;
    private boolean close;
    private DownloadTable.Priority priority;
    private long id;
    private long endpointRequestId;
    private List<Chunk> downloadChunks = new ArrayList<Chunk>();
    private int retries = 0;

    public SegmentRequest(Endpoint endpoint, FileSegmentHash fileSegmentHash, boolean close, DownloadTable.Priority priority, long endpointRequestId, int downloadChunkSize) {
        this.endpoint = endpoint;
        this.fileSegmentHash = fileSegmentHash;
        this.close = close;
        this.priority = priority;
        this.endpointRequestId = endpointRequestId;
        this.id = r.nextLong();
        if (fileSegmentHash != null) {
            createDownloadChunks(downloadChunkSize);
        }

    }

    private void createDownloadChunks(int downloadChunkSize) {
        long size = (fileSegmentHash.getEndOffset() - fileSegmentHash.getStartOffset());
        if (size <= downloadChunkSize) {
            downloadChunks.add(new Chunk(endpoint, fileSegmentHash.getStartOffset(), fileSegmentHash.getEndOffset()));
        } else {
            long currOffset = fileSegmentHash.getStartOffset();
            while (currOffset < fileSegmentHash.getEndOffset()) {
                long left = fileSegmentHash.getEndOffset() - currOffset;
                long i = Math.min(downloadChunkSize, left);
                if (left < (downloadChunkSize * 1.5)) {
                    downloadChunks.add(new Chunk(endpoint, currOffset, fileSegmentHash.getEndOffset()));
                    currOffset = fileSegmentHash.getEndOffset();
                } else {
                    downloadChunks.add(new Chunk(endpoint, currOffset, currOffset + i));
                    currOffset += i + 1;
                }
            }
        }
        log.fine("SegmentRequest.createDownloadChunks start:" + fileSegmentHash.getStartOffset());
        log.fine("SegmentRequest.createDownloadChunks   end:" + fileSegmentHash.getEndOffset());
        for (Chunk downloadChunk : downloadChunks) {
            log.fine("SegmentRequest.createDownloadChunks:" + downloadChunk);
        }
    }


    public Endpoint getEndpoint() {
        return endpoint;
    }

    public String getHash() {
        return fileSegmentHash.getHash();
    }

    public ByteRange getByteRange() {
        return new ByteRange(fileSegmentHash.getStartOffset(), fileSegmentHash.getEndOffset());
    }

    public FileSegmentHash getFileSegmentHash() {
        return fileSegmentHash;
    }

    public List<Chunk> getDownloadChunks() {
        return downloadChunks;
    }

    public boolean isClose() {
        return close;
    }

    public DownloadTable.Priority getPriority() {
        return priority;
    }

    public void setPriority(DownloadTable.Priority priority) {
        this.priority = priority;
    }

    public long getId() {
        return id;
    }

    public long getEndpointRequestId() {
        return endpointRequestId;
    }

    public int getRetries() {
        return retries;
    }

    public void incRetries() {
        this.retries++;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SegmentRequest: endpoint:").append(getEndpoint())
                .append(", priority:").append(getPriority())
                .append(", id:").append(getId())
                .append(", Endpoint Id:").append(getEndpointRequestId())
                .append("\n    FileSegmentHash:").append(fileSegmentHash);

        for (Chunk downloadChunk : downloadChunks) {
            sb.append("\n    Chunk:").append(downloadChunk);
        }
        return sb.toString();
    }
}
