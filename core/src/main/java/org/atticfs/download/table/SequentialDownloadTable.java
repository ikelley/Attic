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

package org.atticfs.download.table;

import org.atticfs.config.download.DownloadConfig;
import org.atticfs.download.request.SegmentRequest;
import org.atticfs.types.DataDescription;

/**
 * Class Description Here...
 *
 * 
 */

public class SequentialDownloadTable extends SimpleDownloadTable {

    public SequentialDownloadTable(DataDescription description, SegmentedData template, DownloadConfig config) {
        super(description, template, config, new SequentialSegmentRequestComparator());
    }

    static class SequentialSegmentRequestComparator implements SegmentRequestComparator {

        public int compare(SegmentRequest request, SegmentRequest request1) {
            Priority p = request.getPriority();
            Priority p1 = request1.getPriority();
            if (p.ordinal() < p1.ordinal()) {
                return -1;
            } else if (p.ordinal() > p1.ordinal()) {
                return 1;
            } else {
                if (request.getFileSegmentHash() == null) {
                    return 1;
                } else if (request1.getFileSegmentHash() == null) {
                    return -1;
                } else if (request.getFileSegmentHash() == null && request1.getFileSegmentHash() == null) {
                    return 0;
                } else {
                    if (request.getFileSegmentHash().getStartOffset() < request1.getFileSegmentHash().getStartOffset()) {
                        return -1;
                    } else if (request.getFileSegmentHash().getStartOffset() > request1.getFileSegmentHash().getStartOffset()) {
                        return 1;
                    } else {
                        if (request.getRetries() < request1.getRetries()) {
                            return -1;
                        } else if (request.getRetries() > request1.getRetries()) {
                            return 1;
                        } else {
                            if (request.getId() < request1.getId()) {
                                return -1;
                            } else if (request.getId() > request1.getId()) {
                                return 1;
                            }
                            return 0;
                        }
                    }
                }
            }

        }
    }


}