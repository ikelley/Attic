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

package org.atticfs.download;

import org.atticfs.types.FileSegmentHash;

/**
 * Class Description Here...
 *
 * 
 */

public class DownloadException extends Exception {

    private FileSegmentHash chunk;
    private long total;

    public DownloadException(String s, FileSegmentHash chunk, long total) {
        super(s);
        this.chunk = chunk;
        this.total = total;
    }

    public DownloadException(FileSegmentHash chunk, long total) {
        this.chunk = chunk;
        this.total = total;
    }

    public DownloadException() {
    }

    public DownloadException(String s) {
        super(s);
    }

    public DownloadException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DownloadException(Throwable throwable) {
        super(throwable);
    }

    public FileSegmentHash getChunk() {
        return chunk;
    }

    public long getTotal() {
        return total;
    }
}
