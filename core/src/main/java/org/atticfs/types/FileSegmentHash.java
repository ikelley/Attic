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


/**
 * Created by IntelliJ IDEA.
 * User: ikelley
 * Date: Jan 19, 2009
 * Time: 1:00:47 PM
 */
public class FileSegmentHash extends WireType {
    String hash;
    long startOffset;
    long endOffset;

    public FileSegmentHash() {
        this(null, -1, -1);
    }

    // we do not necessarily need to have the end, if we know the chunkSize
    public FileSegmentHash(String hash, long startOffset) {
        this(hash, startOffset, -1);
    }

    public FileSegmentHash(String hash, long startOffset, long endOffset) {
        super(WireType.Type.Segment);
        setHash(hash);
        setStartOffset(startOffset);
        setEndOffset(endOffset);
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }

    public long getSize() {
        return endOffset - startOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileSegmentHash that = (FileSegmentHash) o;

        if (endOffset != that.endOffset) return false;
        if (startOffset != that.startOffset) return false;
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * result + (int) (startOffset ^ (startOffset >>> 32));
        result = 31 * result + (int) (endOffset ^ (endOffset >>> 32));
        return result;
    }

    public String toString() {
        return "FileSegmentHash:[hash=" + hash + ",startOffset=" + startOffset + ",endOffset=" + endOffset + "]";
    }
}