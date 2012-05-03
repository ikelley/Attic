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
import org.atticfs.roles.AdvertProcessor;
import org.atticfs.store.DataPointerStore;
import org.atticfs.types.*;
import org.atticfs.util.StringConstants;
import org.atticfs.util.UriUtils;

import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */

public class ProxyRequestHandler extends AbstractRequestHandler implements Authenticating {

    private String path;
    private DataPointerStore store;
    private AdvertProcessor advertProcessor;


    public ProxyRequestHandler(String path, DataPointerStore store, AdvertProcessor advertProcessor) {
        this.path = path;
        this.store = store;
        this.advertProcessor = advertProcessor;
    }

    public ProxyRequestHandler(DataPointerStore store, AdvertProcessor advertProcessor) {
        this(StringConstants.META_KEY, store, advertProcessor);
    }

    protected ChannelData handleGet(ChannelData context) {
        String targetPath = context.getRequestPath();

        String id = UriUtils.extractId(targetPath, StringConstants.POINTER_KEY);
        log.fine(" got id for data to get from URI " + id);
        if (id != null) {
            DataPointer dp = store.getDataPointer(id);
            if (dp != null) {
                context.setResponseData(dp);
                context.setOutcome(ChannelData.Outcome.OK);
            } else {
                Object resource = loadResource(StringConstants.POINTER_KEY, targetPath);
                if (resource != null) {
                    context.setResponseData(resource);
                    context.setOutcome(ChannelData.Outcome.OK);
                } else {
                    context.setOutcome(ChannelData.Outcome.NOT_FOUND);
                }
            }
        } else {
            if (advertProcessor.getAttic().getSecurityConfig().isTestMode()) {
                List<DataPointer> all = store.getDataPointers();
                PointerCollection pc = new PointerCollection();
                for (DataPointer dataPointer : all) {
                    DataPointer copy = new DataPointer(dataPointer.getDataDescription().metadataCopy());
                    copy.setEndpoints(dataPointer.getEndpointSet());
                    pc.addDataPointer(copy);
                }
                context.setResponseData(pc);
                context.setOutcome(ChannelData.Outcome.OK);
            } else {
                context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            }
        }
        return context;
    }

    protected ChannelData handleCreate(ChannelData context) {
        return handleCreateOrMessage(context);
    }

    protected ChannelData handleMessage(ChannelData context) {
        return handleCreateOrMessage(context);
    }

    protected ChannelData handleDelete(ChannelData context) {
        String targetPath = context.getRequestPath();
        String id = UriUtils.extractId(targetPath, StringConstants.POINTER_KEY);
        if (id == null) {
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
        } else {
            advertProcessor.processDelete(id, context);
        }
        return context;
    }

    private ChannelData handleCreateOrMessage(ChannelData context) {
        Object msg = context.getRequestData();
        if (msg == null || !(msg instanceof WireType)) {
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
        } else {
            WireType wt = (WireType) msg;
            if (wt instanceof DataQuery) {
                DataQuery query = (DataQuery) wt;
                advertProcessor.processDataQuery(query, context);
            } else if (wt instanceof DataAdvert) {
                DataAdvert advert = (DataAdvert) wt;
                if (advert.getEndpoint() == null || advert.getDataDescription() == null) {
                    context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
                } else {
                    advertProcessor.processDataAdvert(advert, context);
                }
            } else {
                context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            }
        }
        return context;
    }


    public String getPath() {
        return path;
    }

    public String getAuthenticationKey(ChannelData context) {
        if (context.getAction() == ChannelData.Action.GET) {
            return null;
        }
        String targetPath = context.getRequestPath();
        String id = UriUtils.extractId(targetPath, StringConstants.POINTER_KEY);
        Object msg = context.getRequestData();
        if (msg != null) {
            if (msg instanceof DataQuery) {
                // a query is always a request to cache
                return StringConstants.CACHE_KEY;
            } else if (msg instanceof DataAdvert) {
                if (id != null) {
                    // an advert pointing to a particular id is a cache update
                    return StringConstants.CACHE_KEY;
                } else {
                    // and advert with no id is a request to publish new data
                    return StringConstants.PUBLISH_KEY;
                }
            }
        }
        return StringConstants.PUBLISH_KEY;
    }
}