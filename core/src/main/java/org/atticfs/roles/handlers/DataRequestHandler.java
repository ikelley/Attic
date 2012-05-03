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

package org.atticfs.roles.handlers;

import org.atticfs.channel.ChannelData;
import org.atticfs.store.DescriptionStore;
import org.atticfs.util.StringConstants;
import org.atticfs.util.UriUtils;

import java.io.File;

/**
 * Class Description Here...
 *
 * 
 */

public class DataRequestHandler extends AbstractRequestHandler {

    private String path;
    private DescriptionStore store;

    public DataRequestHandler(String path, DescriptionStore store) {
        this.path = path;
        this.store = store;
    }

    public DataRequestHandler(DescriptionStore store) {
        this(StringConstants.DATA_KEY, store);
    }

    protected ChannelData handleGet(ChannelData context) {
        String targetPath = context.getRequestPath();
        String id = UriUtils.extractId(targetPath, StringConstants.DATA_KEY);
        log.fine(" got id for data to get from URI " + id);
        if (id != null) {
            File f = store.getFile(id);
            if (f != null && f.exists() && f.length() > 0) {
                context.setResponseData(f);
                context.setMimeType("application/octet-stream");
                context.setOutcome(ChannelData.Outcome.OK);
            } else {
                context.setOutcome(ChannelData.Outcome.NOT_FOUND);
            }
        } else {
            context.setOutcome(ChannelData.Outcome.NOT_FOUND);

        }
        return context;

    }


    public String getPath() {
        return path;
    }
}
