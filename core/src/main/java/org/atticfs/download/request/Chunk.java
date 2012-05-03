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
import org.atticfs.types.Endpoint;

/**
 * A chunk is a non-verifiable segment which is a sub segment of a FileSegmentHash.
 * It has an endpoint associated with it and can be in one of 4 states:
 * <p/>
 * untried
 * pending
 * complete
 * failed
 *
 * 
 */

public class Chunk {

    public static enum State {
        UNTRIED, // this chunk has not been tried before
        UNVERIFIED, // the chunk has been successfully downloaded but has not undergone verification
        VERIFIED, // this chunk has been downloaded and the full file segment has been verified
        FAILED // the chunk is a source of verification failure or download failed
    }

    private Endpoint endpoint;
    private long startOffset;
    private long endOffset;
    private State state = State.UNTRIED;


    public Chunk(Endpoint endpoint, long startOffset, long endOffset) {
        this.endpoint = endpoint;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public ByteRange getByteRange() {
        return new ByteRange(getStartOffset(), getEndOffset());
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String toString() {
        return "Endpoint: " + endpoint + " start:" + getStartOffset() + ",end:" + getEndOffset();
    }
}
