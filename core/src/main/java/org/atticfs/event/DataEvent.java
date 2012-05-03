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

package org.atticfs.event;

import org.atticfs.stats.DownloadStats;
import org.atticfs.types.DataDescription;

import java.io.File;

/**
 * Class Description Here...
 *
 * 
 */

public class DataEvent extends MessageEvent {

    private File file;
    private DownloadStats stats;

    public DataEvent(Object o, int status, DataDescription description, String detail, boolean successful, File file, DownloadStats stats) {
        super(o, status, description, detail, successful);
        this.file = file;
        this.stats = stats;
    }

    public DataEvent(Object o, int status, DataDescription description, Throwable throwable, DownloadStats stats) {
        super(o, status, description, throwable);
        this.stats = stats;
    }

    public DataEvent(Object o, DataDescription description, String detail, boolean successful, File file, DownloadStats stats) {
        super(o, description, detail, successful);
        this.file = file;
        this.stats = stats;
    }

    public DataEvent(Object o, DataDescription description, Throwable throwable, DownloadStats stats) {
        super(o, description, throwable);
        this.stats = stats;
    }

    public File getFile() {
        return file;
    }

    public DownloadStats getStats() {
        return stats;
    }

    public DataDescription getDataDescription() {
        return (DataDescription) super.getType();
    }
}
