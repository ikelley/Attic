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

package org.atticfs.stream;

import org.atticfs.event.MessageEvent;
import org.atticfs.stats.DownloadStats;
import org.atticfs.types.DataDescription;

import java.io.InputStream;

/**
 * Class Description Here...
 *
 * 
 */

public class StreamEvent extends MessageEvent {

    private InputStream stream;
    private long startOffset = -1;
    private long endOffset = -1;
    private DownloadStats stats;

    public StreamEvent(Object o, int status, DataDescription description, String detail, boolean successful, InputStream stream, long startOffset, long endOffset, DownloadStats stats) {
        super(o, status, description, detail, successful);
        this.stream = stream;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.stats = stats;
    }

    public StreamEvent(Object o, int status, DataDescription description, Throwable throwable, DownloadStats stats) {
        super(o, status, description, throwable);
        this.stats = stats;
    }

    public StreamEvent(Object o, DataDescription description, String detail, boolean successful, InputStream stream, long startOffset, long endOffset, DownloadStats stats) {
        super(o, description, detail, successful);
        this.stream = stream;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.stats = stats;
    }

    public StreamEvent(Object o, DataDescription description, Throwable throwable, DownloadStats stats) {
        super(o, description, throwable);

        this.stats = stats;
    }

    public InputStream getStream() {
        return stream;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public DownloadStats getStats() {
        return stats;
    }

    public DataDescription getDataDescription() {
        return (DataDescription) super.getType();
    }
}
