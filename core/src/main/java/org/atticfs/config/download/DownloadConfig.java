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

package org.atticfs.config.download;

import org.atticfs.config.Config;

/**
 * Class Description Here...
 *
 * 
 */

public class DownloadConfig extends Config {

    private int bufferSize = 8192;
    // maximum number of concurrent connections total (per client)
    private int maxTotalConnections = 10;
    // maximum number of concurrent connections per file (per client)
    private int maxFileConnections = 5;
    // timeout for chunk downloads
    private int connectionIdleTime = 1000 * 60 * 3;

    // should intermediate files be created, or should results be written directly to the end result?
    private boolean streamToTargetFile = false;

    // size of chunks to download. These are typically smaller than the hashed file segment.
    private int downloadChunkSize = 262144; // 256Kb

    // number of times a request for a segment to a particular endpoint will be attempted again.
    // attempts may not necessarily happen immediately. The request may be queued as a possible
    // option later, and even never get used again.
    private int retryCount = 2;

    // whether data requests should request that data is compressed (gzipped)
    private boolean compress = true;

    private int socketTimeout = 0;

    private int connectionRetryCount = 0;

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        setterCalled("setSocketTimeout");
        this.socketTimeout = socketTimeout;
    }

    public boolean isStreamToTargetFile() {
        return streamToTargetFile;
    }

    public void setStreamToTargetFile(boolean streamToTargetFile) {
        setterCalled("setStreamToTargetFile");
        this.streamToTargetFile = streamToTargetFile;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        setterCalled("setBufferSize");
        this.bufferSize = bufferSize;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        setterCalled("setMaxTotalConnections");
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getMaxFileConnections() {
        return maxFileConnections;
    }

    public void setMaxFileConnections(int maxFileConnections) {
        setterCalled("setMaxFileConnections");
        this.maxFileConnections = maxFileConnections;
    }

    public int getConnectionIdleTime() {
        return connectionIdleTime;
    }

    public void setConnectionIdleTime(int connectionIdleTime) {
        setterCalled("setConnectionIdleTime");
        this.connectionIdleTime = connectionIdleTime;
    }

    public int getDownloadChunkSize() {
        return downloadChunkSize;
    }

    public void setDownloadChunkSize(int downloadChunkSize) {
        setterCalled("setDownloadChunkSize");
        this.downloadChunkSize = downloadChunkSize;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        setterCalled("setRetryCount");
        this.retryCount = retryCount;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        setterCalled("setCompress");
        this.compress = compress;
    }

    public int getConnectionRetryCount() {
        return connectionRetryCount;
    }

    public void setConnectionRetryCount(int connectionRetryCount) {
        setterCalled("setConnectionRetryCount");
        this.connectionRetryCount = connectionRetryCount;
    }
}
