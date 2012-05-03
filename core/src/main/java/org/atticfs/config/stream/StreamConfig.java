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

package org.atticfs.config.stream;

import org.atticfs.config.Config;

/**
 * configuration for the streaming (URLConnection) interface
 *
 * 
 */

public class StreamConfig extends Config {

    /**
     * maximum value to buffer up to if attempting verification.
     * defaults to 1MB
     */
    private int maxBufferSize = 1024 * 1024;
    /**
     * attempts to buffer a data chunk and verify it before passing it to the stream.
     * If the chunk size (and hence the data referenced by the hash) is bigger than the
     * bufferSize, then the chunk will not be verified.
     */
    private boolean attemptVerification = true;

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        setterCalled("setMaxBufferSize");
        this.maxBufferSize = maxBufferSize;
    }

    public boolean isAttemptVerification() {
        return attemptVerification;
    }

    public void setAttemptVerification(boolean attemptVerification) {
        setterCalled("setAttemptVerification");
        this.attemptVerification = attemptVerification;
    }
}
