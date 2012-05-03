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
import org.atticfs.types.WireType;
import org.atticfs.util.StringConstants;
import org.atticfs.util.UriUtils;


/**
 * Class Description Here...
 *
 * 
 */

public class DescriptionRequestHandler extends AbstractRequestHandler {

    private String path;
    private DescriptionStore store;

    public DescriptionRequestHandler(String path, DescriptionStore store) {
        this.path = path;
        this.store = store;
    }

    public DescriptionRequestHandler(DescriptionStore store) {
        this(StringConstants.META_KEY, store);
    }

    protected ChannelData handleGet(ChannelData context) {
        log.fine("DescriptionRequestHandler.handleRequest ENTER");
        String targetPath = context.getRequestPath();
        String id = UriUtils.extractId(targetPath, StringConstants.DESCRIPTION_KEY);

        WireType wt = null;
        if (id != null) {
            log.fine("DescriptionRequestHandler.handleRequest got description id:" + id);
            wt = store.getDataDescription(id);
            log.fine("DescriptionRequestHandler.handleRequest got wire type:" + wt);

        } else {
            id = UriUtils.extractId(targetPath, StringConstants.FILE_HASH_KEY);
            log.fine("DescriptionRequestHandler.handleGet getting filehash id:" + id);
            if (id != null) {
                wt = store.getFileHash(id);
            }
        }
        if (wt != null) {
            context.setResponseData(wt);
            context.setOutcome(ChannelData.Outcome.OK);
        } else {
            context.setOutcome(ChannelData.Outcome.NOT_FOUND);
        }

        return context;
    }

    public String getPath() {
        return path;
    }
}
