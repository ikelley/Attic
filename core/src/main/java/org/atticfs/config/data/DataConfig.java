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

package org.atticfs.config.data;

import org.atticfs.config.Config;

/**
 * Class Description Here...
 *
 * 
 */


public class DataConfig extends Config {

    private long maxLocalData = 1024 * 1024 * 1024 * 100L;  // 100GB!
    private int fileSegmentHashSize = 524288;  // half an MB????
    private long dataQueryInterval = 60 * 60; // DC query interval in seconds.
    private boolean moveIndexedFile = false;


    public long getMaxLocalData() {
        return maxLocalData;
    }

    public void setMaxLocalData(long maxLocalData) {
        setterCalled("setMaxLocalData");
        this.maxLocalData = maxLocalData;
    }

    public int getFileSegmentHashSize() {
        return fileSegmentHashSize;
    }

    public void setFileSegmentHashSize(int fileSegmentHashSize) {
        setterCalled("setFileSegmentHashSize");
        this.fileSegmentHashSize = fileSegmentHashSize;
    }

    public long getDataQueryInterval() {
        return dataQueryInterval;
    }

    public void setDataQueryInterval(long dataQueryInterval) {
        setterCalled("setDataQueryInterval");
        this.dataQueryInterval = dataQueryInterval;
    }

    public boolean isMoveIndexedFile() {
        return moveIndexedFile;
    }

    public void setMoveIndexedFile(boolean moveIndexedFile) {
        setterCalled("setMoveIndexedFile");
        this.moveIndexedFile = moveIndexedFile;
    }
}
