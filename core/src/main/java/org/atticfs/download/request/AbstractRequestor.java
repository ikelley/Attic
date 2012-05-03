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

import org.atticfs.download.Downloader;
import org.atticfs.download.table.DownloadTable;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * abstract class for download callable implementations.
 * A requestor gets SegmentRequests from a DownloadTable and connects to the remote host and
 * downloads. It then writes the contents to file locally.
 *
 * 
 */

public abstract class AbstractRequestor implements Callable<List<Downloader.FetchResult>> {

    static Logger log = Logger.getLogger("org.atticfs.download.request.AbstractRequestor");

    protected DownloadTable table;
    protected Downloader downloader;

    public AbstractRequestor(DownloadTable table, Downloader downloader) {
        this.table = table;
        this.downloader = downloader;
    }

    public abstract List<Downloader.FetchResult> call() throws Exception;
}
