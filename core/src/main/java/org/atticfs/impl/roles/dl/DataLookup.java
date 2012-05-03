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

package org.atticfs.impl.roles.dl;

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelProperties;
import org.atticfs.channel.InChannel;
import org.atticfs.identity.Identity;
import org.atticfs.impl.store.MemoryDataAdvertStore;
import org.atticfs.impl.store.MemoryDataPointerStore;
import org.atticfs.impl.store.MemoryDataQueryStore;
import org.atticfs.roles.AbstractServiceRole;
import org.atticfs.roles.AdvertProcessor;
import org.atticfs.roles.AdvertStorage;
import org.atticfs.roles.handlers.PointerRequestHandler;
import org.atticfs.roles.handlers.ProxyRequestHandler;
import org.atticfs.store.DataAdvertStore;
import org.atticfs.store.DataPointerStore;
import org.atticfs.store.DataQueryStore;
import org.atticfs.types.DataAdvert;
import org.atticfs.types.DataPointer;
import org.atticfs.types.DataQuery;
import org.atticfs.types.PointerCollection;
import org.atticfs.util.StringConstants;
import org.atticfs.util.UriUtils;

import java.io.IOException;
import java.util.List;

/**
 * Lookup service. This serves up DataPointers.
 * It keeps a mapping between DataAdverts (possibly associated with an Identity) and their associated DataPointers.
 *
 * 
 */

public class DataLookup extends AbstractServiceRole implements AdvertStorage, AdvertProcessor {


    private DataAdvertStore dataAdvertStore;
    private DataQueryStore dataQueryStore;
    private DataPointerStore dataPointerStore;

    public void init(Attic attic) throws IOException {
        super.init(attic);
        if (dataAdvertStore == null) {
            dataAdvertStore = new MemoryDataAdvertStore();
        }
        if (dataQueryStore == null) {
            dataQueryStore = new MemoryDataQueryStore();
        }
        if (dataPointerStore == null) {
            dataPointerStore = new MemoryDataPointerStore();
        }
        addChannelRequestHandler(StringConstants.POINTER_KEY, new PointerRequestHandler(dataPointerStore, this));
        addChannelRequestHandler(StringConstants.POINTER_KEY, new ProxyRequestHandler(dataPointerStore, this));

        dataAdvertStore.init();
        dataQueryStore.init();
        dataPointerStore.init();
    }

    public void shutdown() throws IOException {
        super.shutdown();
        dataAdvertStore.shutdown();
        dataQueryStore.shutdown();
        dataPointerStore.shutdown();
    }

    protected InChannel initInChannel() throws IOException {
        ChannelProperties props = new ChannelProperties();
        props.setLocalPort(getAttic().getPort());
        props.setServerContext(getPath());
        return getAttic().getChannelFactory().createInChannel(this, props);
    }


    public String getPath() {
        return StringConstants.DATA_LOOKUP;
    }

    /**
     * process:
     * 1. if the id does not exist, check if we are authenticating, and check that the action is not a Caching action.
     * caching can only occur on data that already exists. If this fails, send back an Action not allowed.
     * 2. if publishing is allowed and the action is publish, set the id, and store it.
     * 3. otherwise - this is a caching notification. Add the endpoint in the advert to the data pointer table pointer.
     * <p/>
     * Return the DataPointer - referenced or created - to the client
     *
     * @param advert
     * @param context
     */
    public void processDataAdvert(DataAdvert advert, ChannelData context) {
        log.fine("client action is " + context.getAuthenticationAction());
        String targetPath = context.getRequestPath();
        String id = UriUtils.extractId(targetPath, StringConstants.POINTER_KEY);
        String ddId = advert.getDataDescription().getId();
        DataAdvert existing = dataAdvertStore.getDataAdvert(ddId);
        if (id != null && !id.equals(ddId)) {
            // the id in the URI must match the id in the advert
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            return;
        }
        if (id != null && existing == null) {
            // if there is an id in the URI, then an existing advert (previously published) must exist
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            return;
        }
        DataPointer ret = null;
        if (existing == null) {
            log.fine("data advert does not exist yet with id:" + advert.getDataDescription().getId());
            if (context.getAuthenticationAction().equals(StringConstants.PUBLISH_KEY)) {
                // we'll create a new data pointer
                dataAdvertStore.addDataAdvert(context.getRemoteIdentity(), advert);
                ret = dataPointerStore.createDataPointer(advert);
                if (ret != null) {
                    context.setOutcome(ChannelData.Outcome.CREATED);
                    context.setResponseData(ret);
                } else {
                    context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
                }
            } else {
                context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
            }
        } else {
            log.fine("data advert already exists with id:" + advert.getDataDescription().getId());
            if (context.getAuthenticationAction().equals(StringConstants.CACHE_KEY)) {
                // key was in URL
                // update the pointer only with the endpoint

                if (advert.getEndpoint() != null) {
                    boolean dereference = isDereference(advert);
                    if (dereference) {
                        ret = dataPointerStore.removeEndpointFromDataPointer(advert);
                        if (ret != null) {
                            dataAdvertStore.dataUncached(advert);
                        }
                    } else {
                        ret = dataPointerStore.addEndpointToDataPointer(advert);
                        if (ret != null) {
                            dataAdvertStore.dataCached(advert);
                        }
                    }
                    if (ret != null) {
                        context.setOutcome(ChannelData.Outcome.OK);
                        context.setResponseData(ret);
                    } else {
                        // the advert exists, so if there is not pointer it's our fault
                        context.setOutcome(ChannelData.Outcome.SERVER_ERROR);
                    }
                } else {
                    context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
                }

            } else if (context.getAuthenticationAction().equals(StringConstants.PUBLISH_KEY)) {
                // key was not in URL
                if (getAttic().getSecurityConfig().isTestMode() || context.isAuthorized()) {
                    Identity i = dataAdvertStore.getIdentity(advert);
                    dataAdvertStore.addDataAdvert(i, advert);
                    ret = dataPointerStore.updateDataPointer(advert);
                    if (ret != null) {
                        context.setOutcome(ChannelData.Outcome.OK);
                        context.setResponseData(ret);
                    } else {
                        context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
                    }
                } else {
                    context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
                }
            } else {
                context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
            }

        }
        if (ret != null) {
            String ddid = ret.getDataDescription().getId();
            // return the endpoint of the, either created or updated DataPointer in the header
            context.setLocation(getHandlerEndpoint(getHandlerForType(StringConstants.POINTER_KEY))
                    .appendToPath(StringConstants.POINTER_KEY).appendToPath(ddid).toString());
        }
    }

    private boolean isDereference(DataAdvert advert) {
        return advert.getConstraint(DataAdvert.DEREFERENCE) != null &&
                advert.getConstraint(DataAdvert.DEREFERENCE).getBooleanValue() == true;
    }


    /**
     * query for a pointer collection.
     *
     * @param query
     * @param context
     */

    public void processDataQuery(DataQuery query, ChannelData context) {
        String targetPath = context.getRequestPath();
        String id = UriUtils.extractId(targetPath, StringConstants.POINTER_KEY);
        if (id != null) {
            // a query for data should not contain an id - it is a query for any data
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            return;
        }
        dataQueryStore.addDataQuery(query);
        List<DataAdvert> matching = dataAdvertStore.getDataAdverts(query.getConstraints());
        PointerCollection pc = new PointerCollection();
        if (matching.size() > 0) {
            for (DataAdvert dataAdvert : matching) {
                DataPointer dp = dataPointerStore.getDataPointer(dataAdvert.getDataDescription().getId());
                if (dp != null) {
                    log.fine(" found matching data pointer:");
                    pc.addDataPointer(dp);
                }
            }
        }
        context.setResponseData(pc);
        context.setOutcome(ChannelData.Outcome.OK);
    }

    public void processDelete(String id, ChannelData context) {

        DataPointer dp = dataPointerStore.getDataPointer(id);
        if (dp != null) {
            if (getAttic().getSecurityConfig().isTestMode()) {
                dataPointerStore.deleteDataPointer(id);
                dataAdvertStore.deleteDataAdvert(null, id);
                context.setResponseData(dp);
                context.setOutcome(ChannelData.Outcome.OK);
            } else {
                if (context.isAuthorized()) {
                    DataAdvert advert = dataAdvertStore.getDataAdvert(id);
                    Identity i = dataAdvertStore.getIdentity(advert);
                    dataAdvertStore.deleteDataAdvert(i, id);
                    dataPointerStore.deleteDataPointer(id);
                    context.setResponseData(dp);
                    context.setOutcome(ChannelData.Outcome.OK);
                } else {
                    context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
                }
            }
        } else {
            context.setOutcome(ChannelData.Outcome.NOT_FOUND);
        }
    }

    public DataAdvertStore getDataAdvertStore() {
        return dataAdvertStore;
    }

    public void setDataAdvertStore(DataAdvertStore dataAdvertStore) {
        this.dataAdvertStore = dataAdvertStore;
    }

    public DataQueryStore getDataQueryStore() {
        return dataQueryStore;
    }

    public void setDataQueryStore(DataQueryStore dataQueryStore) {
        this.dataQueryStore = dataQueryStore;
    }

    public DataPointerStore getDataPointerStore() {
        return dataPointerStore;
    }

    public void setDataPointerStore(DataPointerStore dataPointerStore) {
        this.dataPointerStore = dataPointerStore;
    }
}
