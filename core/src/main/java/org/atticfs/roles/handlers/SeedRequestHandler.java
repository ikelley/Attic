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
import org.atticfs.identity.Identity;
import org.atticfs.protocol.AtticProtocol;
import org.atticfs.roles.Publisher;
import org.atticfs.store.DataAdvertStore;
import org.atticfs.store.DescriptionStore;
import org.atticfs.types.DataAdvert;
import org.atticfs.types.DataDescription;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;
import org.atticfs.util.FileUtils;
import org.atticfs.util.MonitoredMap;
import org.atticfs.util.StringConstants;
import org.atticfs.util.UriUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Receives posted DataAdverts
 *
 * 
 */

public class SeedRequestHandler extends AbstractRequestHandler implements Authenticating {

    private String path;
    private Publisher service;
    private DataAdvertStore dataAdvertStore;
    private DescriptionStore dataStore;
    private File outputDir;

    private Map<String, DataAdvert> pendingData = new MonitoredMap<String, DataAdvert>(1000 * 60 * 5);

    public SeedRequestHandler(String path, DataAdvertStore dataAdvertStore, DescriptionStore dataStore, File outputDir, Publisher service) {
        this.path = path;
        this.dataAdvertStore = dataAdvertStore;
        this.dataStore = dataStore;
        this.outputDir = outputDir;
        this.service = service;
    }

    public SeedRequestHandler(DataAdvertStore dataAdvertStore, DescriptionStore dataStore, File outputDir, Publisher service) {
        this(StringConstants.SEED_KEY, dataAdvertStore, dataStore, outputDir, service);
    }

    protected ChannelData handleDelete(ChannelData context) {
        String targetPath = context.getRequestPath();
        log.fine(" target path: " + targetPath);
        String id = UriUtils.extractId(targetPath, StringConstants.SEED_KEY);
        if (id == null) {
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
        } else {
            if (service.getAttic().getSecurityConfig().isTestMode()) {
                DataAdvert advert = dataAdvertStore.getDataAdvert(id);
                if (advert != null) {
                    unpublish(context, advert);
                    if (context.getOutcome() == ChannelData.Outcome.OK) {
                        advert = dataAdvertStore.deleteDataAdvert(null, id);
                        dataStore.removeFile(id);
                    } else {
                        context.setOutcome(ChannelData.Outcome.SERVER_ERROR);
                    }
                } else {
                    context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
                }
            } else {
                if (context.isAuthorized()) {
                    DataAdvert advert = dataAdvertStore.getDataAdvert(id);
                    Identity i = dataAdvertStore.getIdentity(advert);
                    unpublish(context, advert);
                    if (context.getOutcome() == ChannelData.Outcome.OK) {
                        dataAdvertStore.deleteDataAdvert(i, id);
                        dataStore.removeFile(id);
                    } else {
                        context.setOutcome(ChannelData.Outcome.SERVER_ERROR);
                    }
                } else {
                    context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
                }
            }
        }
        return context;
    }

    private void unpublish(ChannelData context, DataAdvert ad) {
        try {
            DataPointer pointer = service.unpublish(ad);
            context.setResponseData(pointer);
            context.setOutcome(ChannelData.Outcome.OK);
        } catch (Exception e) {
            context.setOutcome(ChannelData.Outcome.SERVER_ERROR);
        }
    }

    protected ChannelData handleCreate(ChannelData context) {
        try {
        log.fine(" ENTER");
        Object msg = null;
        if (context != null) {
            msg = context.getRequestData();
        }
        if (msg == null) {
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            return context;
        }
        String targetPath = context.getRequestPath();
        log.fine(" target path: " + targetPath);
        String id = UriUtils.extractId(targetPath, StringConstants.SEED_KEY);
        log.fine(" got id for data to get from URI " + id);
        if (id == null) {
            log.fine("ID is null. message object is " + msg);
            if (msg instanceof DataAdvert) {
                DataAdvert ad = (DataAdvert) msg;
                DataDescription dd = ad.getDataDescription();
                log.fine("DataAdvert ID (id==null)=" + ad.getDataDescription().getName());

                if (dd == null) {
                    context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
                } else {
                    if (dd.getId() == null) {
                        dd.setId(dd.createId());
                    }
                    Endpoint ep = service.getHandlerEndpoint(this);
                    context.setLocation(ep.appendToPath(dd.getId()).toString());
                    context.setOutcome(ChannelData.Outcome.CREATED);
                    pendingData.put(dd.getId(), ad);
                }
            } else {
                context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            }
        } else {
            DataAdvert ad = pendingData.get(id);
            final DataDescription dd = ad.getDataDescription();
            if (dd != null) {
                pendingData.remove(id);
                File file = null;
                if (msg instanceof File) {
                    file = (File) msg;
                } else {
                    file = writeData(msg, id);
                }
                if (file != null) {
                    final File f = file;
                    Endpoint ep = service.createEndpoint(ad);
                    ad.setEndpoint(ep);
                    try {
                        DataPointer dp = service.publish(ad);
                        log.fine("DataAdvert ID=" + ad.getDataDescription().getName());
                        if (dp != null) {
                            Endpoint loc = service.getEndpoint(dp);
                            if (loc != null) {
                                new Thread() {
                                    public void run() {
                                        service.index(dd, f);
                                    }
                                }.start();

                                Endpoint endpoint = new Endpoint(loc.toString());
                                if (context.isAuthorized()) {
                                    endpoint.setScheme(AtticProtocol.SCHEME_ATTICS);
                                } else {
                                    endpoint.setScheme(AtticProtocol.SCHEME_ATTIC);
                                }
                                context.setLocation(endpoint.toString());
                                context.setOutcome(ChannelData.Outcome.CREATED);
                                context.setResponseData(dp);
                                // store it permanently
                                dataAdvertStore.addDataAdvert(context.getRemoteIdentity(), ad);
                            }
                        }
                    } catch (IOException e) {
                        log.fine(" exception thrown " + e.getMessage());
                        context.setOutcome(ChannelData.Outcome.SERVER_ERROR); // can we do better?
                    } catch (Exception e) {
                        log.fine(" exception thrown " + e.getMessage());
                        context.setOutcome(ChannelData.Outcome.SERVER_ERROR); // can we do better?
                    }
                } else {
                    context.setOutcome(ChannelData.Outcome.SERVER_ERROR); // can we do better?
                }
            } else {
                context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            }
        }
        } catch (Exception e) {
            log.fine(" exception thrown " + e.getMessage());
            context.setOutcome(ChannelData.Outcome.SERVER_ERROR); // can we do better?
        }
        return context;

    }

    public String getPath() {
        return path;
    }

    public String getAuthenticationKey(ChannelData data) {
        return StringConstants.SEED_KEY;
    }

    private File writeData(Object data, String id) {
        try {
            File f = new File(outputDir, id + ".dat");
            FileOutputStream fout = new FileOutputStream(f);
            if (data instanceof byte[]) {
                fout.write((byte[]) data);
            } else if (data instanceof String) {
                fout.write(((String) data).getBytes());
            } else {
                fout.close();
                return null;
            }
            fout.flush();
            fout.close();
            return f;
        } catch (IOException e) {
            log.fine("exception thrown writing to file:" + FileUtils.formatThrowable(e));
        }
        return null;

    }
}