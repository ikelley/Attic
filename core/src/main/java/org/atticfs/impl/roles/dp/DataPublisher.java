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

package org.atticfs.impl.roles.dp;

import java.io.File;
import java.io.IOException;

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelProperties;
import org.atticfs.channel.InChannel;
import org.atticfs.channel.OutChannel;
import org.atticfs.impl.store.FileDescriptionStore;
import org.atticfs.impl.store.MemoryPointerLocationStore;
import org.atticfs.roles.AbstractServiceRole;
import org.atticfs.roles.DescriptionStorage;
import org.atticfs.roles.Publisher;
import org.atticfs.roles.handlers.DataRequestHandler;
import org.atticfs.roles.handlers.DescriptionRequestHandler;
import org.atticfs.store.DataPointerLocationStore;
import org.atticfs.store.DescriptionStore;
import org.atticfs.store.index.DescriptionListener;
import org.atticfs.store.index.DescriptionMaker;
import org.atticfs.store.index.Index;
import org.atticfs.types.DataAdvert;
import org.atticfs.types.DataDescription;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;
import org.atticfs.util.StringConstants;

/**
 * Class Description Here...
 *
 * 
 */

public class DataPublisher extends AbstractServiceRole implements DescriptionListener, DescriptionStorage, Publisher {

    private Endpoint publishControl;

    private DescriptionStore descriptionStore;
    private DataPointerLocationStore dataPointerLocationStore;

    public DataPublisher(Endpoint publishControl) {
        this.publishControl = publishControl;
    }

    public DataPublisher() {
    }

    @Override
    public void init(Attic attic) throws IOException {
        super.init(attic);
        if (publishControl == null && attic.getBootstrapEndpoint() != null) {
            publishControl = new Endpoint(attic.getBootstrapEndpoint());
        }

        if (dataPointerLocationStore == null) {
            dataPointerLocationStore = new MemoryPointerLocationStore();
        }

        if (descriptionStore == null) {
            descriptionStore = new FileDescriptionStore(getAttic(), getDataHome(), getAttic().getDPDesc(), this);
        }
        addChannelRequestHandler(StringConstants.DATA_KEY, new DataRequestHandler(getDescriptionStore()));
        addChannelRequestHandler(StringConstants.DESCRIPTION_KEY, new DescriptionRequestHandler(getDescriptionStore()));

        dataPointerLocationStore.init();
        descriptionStore.init();

    }

    public void shutdown() throws IOException {
        super.shutdown();
        descriptionStore.shutdown();
        dataPointerLocationStore.shutdown();
    }

    public void index(DataDescription template, File file) {
        Index index = new Index(getAttic(), new DescriptionMaker(template, this, getAttic().getDataConfig().isMoveIndexedFile()));
        index.index(file);
    }

    protected InChannel initInChannel() throws IOException {
        ChannelProperties props = new ChannelProperties();
        props.setLocalPort(getAttic().getPort());
        props.setOutputDirectory(getAttic().getDPDataHome());
        return getAttic().getChannelFactory().createInChannel(this, props);
    }

    public DataAdvert createAdvert(DataDescription description) {
        DataAdvert ad = new DataAdvert(description);
        log.fine("DataAdvert ID (datapublisher)/createAdvert=" + ad.getDataDescription().getName());

        Endpoint ep = createEndpoint(ad);
        ad.setEndpoint(ep);
        return ad;
    }

    public DataPointer publish(DataAdvert advert) throws Exception {
        if (publishControl == null) {
            throw new IOException("No publish endpoint provided in either the constructor or the attic bootstrap endpoint!");
        }
        log.fine("DataAdvert ID (datapublisher)/publish=" + advert.getDataDescription().getName());


        OutChannel out = getAttic().getChannelFactory().createOutChannel(null);
        ChannelData cd = new ChannelData(ChannelData.Action.CREATE, publishControl);
        cd.setRequestData(advert);
        cd.setResponseType(DataPointer.class);

        cd.setCloseOnFinish(true);
        ChannelData response = out.send(cd);
        log.fine("outcome to publish:" + response.getOutcome());
        if (response.getOutcome() == ChannelData.Outcome.OK) {

            Object ret = response.getResponseData();
            log.fine("return value:" + ret);
            if (ret != null && ret instanceof DataPointer) {
                DataPointer dp = (DataPointer) ret;
                if (response.getLocation() != null) {
                    Endpoint location = new Endpoint(response.getLocation());
                    dataPointerLocationStore.add(location, dp);
                }
                return dp;
            }
        }
        return null;
    }

    public DataPointer unpublish(DataAdvert advert) throws Exception {
        if (publishControl == null) {
            throw new IOException("No publish endpoint provided in either the constructor or the attic bootstrap endpoint!");
        }
        OutChannel out = getAttic().getChannelFactory().createOutChannel(null);
        Endpoint deleteEndpoint = publishControl.appendToPath(advert.getDataDescription().getId());
        ChannelData cd = new ChannelData(ChannelData.Action.DELETE, deleteEndpoint);

        cd.setCloseOnFinish(true);
        ChannelData response = out.send(cd);
        log.fine("outcome to unublish:" + response.getOutcome());
        if (response.getOutcome() == ChannelData.Outcome.OK) {
            Object ret = response.getResponseData();
            log.fine("return value:" + ret);
            if (ret != null && ret instanceof DataPointer) {
                DataPointer dp = (DataPointer) ret;
                return dp;
            }
        }
        return null;
    }

    public Endpoint getEndpoint(DataPointer pointer) {
        return dataPointerLocationStore.get(pointer);
    }

    public Endpoint createEndpoint(DataAdvert advert) {
        DataDescription dd = advert.getDataDescription();
        if (dd == null || dd.getId() == null) {
            return null;
        }
        Endpoint ep = getHandlerEndpoint(getHandlerForType(StringConstants.DATA_KEY))
                .appendToPath(dd.getId());
        ep.setMetaEndpoint(getHandlerEndpoint(getHandlerForType(StringConstants.DESCRIPTION_KEY)).toString());
        return ep;
    }

    public String getPath() {
        return StringConstants.DATA_PUBLISHER;
    }

    public File getDataHome() {
        return getAttic().getDPDataHome();
    }

    public DescriptionStore getDescriptionStore() {
        return descriptionStore;
    }

    public void setDescriptionStore(DescriptionStore descriptionStore) {
        this.descriptionStore = descriptionStore;
    }

    public DataPointerLocationStore getDataPointerLocationStore() {
        return dataPointerLocationStore;
    }

    public void setDataPointerLocationStore(DataPointerLocationStore dataPointerLocationStore) {
        this.dataPointerLocationStore = dataPointerLocationStore;
    }

    public void descriptionMapped(DataDescription dd, File f) {
        System.out.println("DataPublisher.descriptionMapped with file:" + f.getAbsolutePath());
        try {
            DataAdvert advert = createAdvert(dd);
            DataPointer dp = publish(advert);
            if (dp != null) {
                if (dp.getDataDescription() != null) {
                    getDescriptionStore().put(f, dd);
                    log.info("More beer please!");
                }
            }
        } catch (Exception e1) {
            log.warning("could not publish data with id:" + dd.getId());
        }
    }

    public void indexComplete() {
        log.fine("Done indexing local files.");
    }

}
