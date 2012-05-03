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

package org.atticfs.types;


import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ikelley
 * Date: Jan 17, 2009
 * Time: 12:56:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileHash extends WireType {

    private long defaultChunkSize = -1;
    private String hash;
    private long size;
    private List<FileSegmentHash> chunks = new Vector<FileSegmentHash>();

    public FileHash() {
        super(WireType.Type.FileHash);
    }

    public int getNumChunks() {
        return chunks.size();
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Long getDefaultChunkSize() {
        return defaultChunkSize;
    }

    public void setDefaultChunkSize(Long defaultChunkSize) {
        this.defaultChunkSize = defaultChunkSize;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void addSegment(String hash, long start, long end) {
        FileSegmentHash fsh = getChunkByRange(start, end);
        if (fsh != null) {
            fsh.setHash(hash);
        } else {
            chunks.add(new FileSegmentHash(hash, start, end));
        }

    }

    public void addSegment(FileSegmentHash hash) {
        FileSegmentHash fsh = getChunkByRange(hash.getStartOffset(), hash.getEndOffset());
        if (fsh != null) {
            fsh.setHash(hash.getHash());
        } else {
            chunks.add(hash);
        }
    }

    public FileSegmentHash getChunk(int number) {
        if (number >= chunks.size() || number < 0 || chunks.size() == 0) {
            return null;
        }
        return chunks.get(number);
    }

    public List<FileSegmentHash> getChunks() {
        return Collections.unmodifiableList(chunks);
    }

    public FileSegmentHash getChunkByRange(long start, long finish) {
        for (FileSegmentHash fsh : chunks) {
            if ((fsh.getStartOffset() == start) && (fsh.getEndOffset() == finish)) {
                return fsh;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileHash fileHash = (FileHash) o;

        if (defaultChunkSize != fileHash.defaultChunkSize) return false;
        if (size != fileHash.size) return false;
        if (chunks != null ? !chunks.equals(fileHash.chunks) : fileHash.chunks != null) return false;
        if (hash != null ? !hash.equals(fileHash.hash) : fileHash.hash != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (defaultChunkSize ^ (defaultChunkSize >>> 32));
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (chunks != null ? chunks.hashCode() : 0);
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("hash:" + hash + ", chunks:");
        for (FileSegmentHash fsh : chunks) {
            sb.append("\n\t" + fsh.getHash() + "(" + fsh.getStartOffset() + " - " + fsh.getEndOffset() + ") ");
        }
        return sb.toString();
    }
}