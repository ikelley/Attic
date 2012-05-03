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

package org.atticfs.impl.roles.dw;

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.OutChannel;
import org.atticfs.download.Downloader;
import org.atticfs.download.request.RequestCollection;
import org.atticfs.download.request.RequestResolver;
import org.atticfs.event.DataEvent;
import org.atticfs.event.DataReceiver;
import org.atticfs.impl.store.FileDescriptionStore;
import org.atticfs.impl.store.MemoryPointerLocationStore;
import org.atticfs.roles.AbstractRole;
import org.atticfs.roles.DescriptionStorage;
import org.atticfs.store.DataPointerLocationStore;
import org.atticfs.store.DescriptionStore;
import org.atticfs.store.index.DescriptionListener;
import org.atticfs.types.DataDescription;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileHash;
import org.atticfs.util.FileUtils;
import org.atticfs.util.StringConstants;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class DataWorker extends AbstractRole implements DataReceiver, DescriptionListener, DescriptionStorage {

    static Logger log = Logger.getLogger("org.atticfs.impl.roles.dc.DataWorker");

    private DescriptionStore descriptionStore;
    private DataPointerLocationStore dataPointerLocationStore;


    public DataWorker() {
    }

    public String getPath() {
        return StringConstants.DATA_CENTER;
    }

    public void init(Attic attic) throws IOException {
        super.init(attic);
        if (descriptionStore == null) {
            descriptionStore = new FileDescriptionStore(getAttic(), getDataHome(), getAttic().getDWDesc(), this);
        }
        if (dataPointerLocationStore == null) {
            dataPointerLocationStore = new MemoryPointerLocationStore();
        }
        descriptionStore.init();
        dataPointerLocationStore.init();
    }

    public void shutdown() throws IOException {
        descriptionStore.shutdown();
        dataPointerLocationStore.shutdown();
    }

    public void getPointer(Endpoint endpoint) throws Exception {
        if (endpoint == null) {
            log.warning("trying to query for data, but no endpoint to query to was specified");
            return;
        }

        OutChannel out = getAttic().getChannelFactory().createOutChannel(null);
        ChannelData data = new ChannelData(ChannelData.Action.GET, endpoint);
        data.setResponseType(DataPointer.class);
        ChannelData resp = out.send(data);
        Object o = resp.getResponseData();
        if (o != null && o instanceof DataPointer && resp.getOutcome() == ChannelData.Outcome.OK) {
            if (processPointer((DataPointer) o)) {
                log.fine("Got a DataPointer");
                Download downloads = new Download((DataPointer) o, getAttic().getDWDataHome(), getAttic(), this);
                dataPointerLocationStore.add(endpoint, (DataPointer) o);
                getAttic().execute(downloads);
            }
        }
    }

    private boolean processPointer(DataPointer pointer) {
        long available = FileUtils.spaceAvailable(getAttic().getDWDataHome(), getAttic().getDataConfig().getMaxLocalData());
        if (available <= 0) {
            log.fine("cannot cache anything - no space left");
            return false;
        }

        DataDescription dd = pointer.getDataDescription();
        if (dd == null) {
            return false;
        }
        FileHash fh = dd.getHash();
        if (fh == null) {
            return false;
        }
        long length = fh.getSize();
        if (length <= 0) {
            return false;
        }
        if (length <= available) {
            return true;
        }

        return false;
    }

    public void setDescriptionStore(DescriptionStore descriptionStore) {
        this.descriptionStore = descriptionStore;
    }

    public File getDataHome() {
        return getAttic().getDWDataHome();
    }

    public DescriptionStore getDescriptionStore() {
        return descriptionStore;
    }

    public DataPointerLocationStore getDataPointerLocationStore() {
        return dataPointerLocationStore;
    }

    public void setDataPointerLocationStore(DataPointerLocationStore dataPointerLocationStore) {
        this.dataPointerLocationStore = dataPointerLocationStore;
    }

    public void dataArrived(DataEvent event) {
        log.info(event.getStats().display());
        if (event.isSuccessful()) {
            log.fine("HOORAY! GOT SOME DATA");
            DataDescription dd = event.getDataDescription();
            getDescriptionStore().put(event.getFile(), dd);
        }
    }

    public void descriptionMapped(DataDescription description, File file) {
    }

    private static class Download implements Runnable {

        private DataPointer pointer;
        private File target;
        private Attic attic;
        private DataReceiver receiver;

        private Download(DataPointer pointer, File target, Attic attic, DataReceiver receiver) {
            this.pointer = pointer;
            this.target = target;
            this.attic = attic;
            this.receiver = receiver;
        }

        public void run() {
            try {
                RequestCollection mc = RequestResolver.createRequestCollection(pointer, attic);
                Downloader downloader = new Downloader(receiver, mc, target, attic);

                downloader.download();
            } catch (IOException e) {
                log.info("exception thrown during download:" + e.getMessage());
            }
        }
    }
}