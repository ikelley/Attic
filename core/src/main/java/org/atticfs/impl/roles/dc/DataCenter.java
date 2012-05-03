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

package org.atticfs.impl.roles.dc;

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelProperties;
import org.atticfs.channel.InChannel;
import org.atticfs.channel.OutChannel;
import org.atticfs.download.Downloader;
import org.atticfs.download.request.RequestCollection;
import org.atticfs.download.request.RequestResolver;
import org.atticfs.event.DataReceiver;
import org.atticfs.impl.store.FileDescriptionStore;
import org.atticfs.impl.store.MemoryPointerLocationStore;
import org.atticfs.roles.AbstractServiceRole;
import org.atticfs.roles.DescriptionStorage;
import org.atticfs.roles.handlers.DataRequestHandler;
import org.atticfs.roles.handlers.DescriptionRequestHandler;
import org.atticfs.store.DataPointerLocationStore;
import org.atticfs.store.DescriptionStore;
import org.atticfs.store.index.DescriptionListener;
import org.atticfs.types.*;
import org.atticfs.util.FileUtils;
import org.atticfs.util.StringConstants;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class DataCenter extends AbstractServiceRole implements DescriptionListener, DescriptionStorage {

    private DescriptionStore descriptionStore;
    /**
     * NOTE: this is not used yet!
     */
    private DataPointerLocationStore dataPointerLocationStore;

    private Endpoint dls;
    private Timer queryTimer = new Timer();
    private List<String> current = new Vector<String>();

    public DataCenter(Endpoint dls) {
        this.dls = dls;
    }

    public DataCenter() {
    }

    public String getPath() {
        return StringConstants.DATA_CENTER;
    }

    @Override
    public void init(Attic attic) throws IOException {
        super.init(attic);
        if (dls == null && attic.getBootstrapEndpoint() != null) {
            dls = new Endpoint(attic.getBootstrapEndpoint());
        }
        if (descriptionStore == null) {
            descriptionStore = new FileDescriptionStore(attic, getDataHome(), attic.getDCDesc(), this);
        }
        if (dataPointerLocationStore == null) {
            dataPointerLocationStore = new MemoryPointerLocationStore();
        }
        addChannelRequestHandler(StringConstants.DATA_KEY, new DataRequestHandler(getDescriptionStore()));
        addChannelRequestHandler(StringConstants.DESCRIPTION_KEY, new DescriptionRequestHandler(getDescriptionStore()));

        log.fine("Data query interval is:" + attic.getDataConfig().getDataQueryInterval());
        queryTimer.schedule(new QueryTask(this, null), 1000, attic.getDataConfig().getDataQueryInterval() * 1000);

        descriptionStore.init();
        dataPointerLocationStore.init();

    }

    public void shutdown() throws IOException {
        super.shutdown();
        descriptionStore.shutdown();
        dataPointerLocationStore.shutdown();
    }

    protected InChannel initInChannel() throws IOException {
        ChannelProperties props = new ChannelProperties();
        props.setLocalPort(getAttic().getPort());
        props.setOutputDirectory(getAttic().getDCDataHome());
        return getAttic().getChannelFactory().createInChannel(this, props);
    }

    public void query() throws Exception {
        query(null);
    }

    public void query(Constraints constraints) throws Exception {
        if (dls == null) {
            log.warning("trying to query for cacheable data, but no endpoint to query to was specified");
            return;
        }
        DataQuery query = new DataQuery();
        query.setConstraints(constraints);
        OutChannel out = getAttic().getChannelFactory().createOutChannel(null);
        ChannelData data = new ChannelData(ChannelData.Action.MESSAGE, dls);
        data.setResponseType(PointerCollection.class);
        data.setRequestData(query);
        data.setCloseOnFinish(true);
        ChannelData resp = out.send(data);
        Object o = resp.getResponseData();
        if (o != null && o instanceof PointerCollection) {
            log.fine("Got a PointerCollection");
            List<DataPointer> possible = processPointerCollection((PointerCollection) o);
            Endpoint mine = createDataEndpoint();
            if (possible.size() > 0) {
                // creates a data endpoint without the id appended and a meta endpoint

                DataCenterReceiver receiver = new DataCenterReceiver(mine, dls, this);
                Downloads downloads = new Downloads(possible, getAttic().getDCDataHome(), getAttic(), receiver);
                getAttic().execute(downloads);
            }
            possible = updatePointerCollection((PointerCollection) o);
            // these are pointers which reference data that is already cached.
            if (possible.size() > 0) {
                DataCenterReceiver receiver = new DataCenterReceiver(mine, dls, this);
                for (DataPointer pointer : possible) {
                    DataDescription dd = pointer.getDataDescription();
                    receiver.notifyDls(dd, getDescriptionStore().getFile(dd.getId()));
                }
            }
        }
    }

    private Endpoint createDataEndpoint() {
        Endpoint ret = getHandlerEndpoint(getHandlerForType(StringConstants.DATA_KEY));
        ret.setMetaEndpoint(getHandlerEndpoint(getHandlerForType(StringConstants.DESCRIPTION_KEY)).toString());
        return ret;
    }

    private List<DataPointer> updatePointerCollection(PointerCollection coll) {
        List<DataPointer> possible = new ArrayList<DataPointer>();

        List<DataPointer> pointers = coll.getDataPointers();
        for (DataPointer pointer : pointers) {
            DataDescription dd = pointer.getDataDescription();
            if (dd == null) {
                continue;
            }
            if (dd.getId() == null) {
                continue;
            }

            if (current.contains(dd.getId())) {
                return null;
            }
            FileHash fh = dd.getHash();
            if (fh == null) {
                continue;
            }
            if (getDescriptionStore().getDataDescription(dd.getId()) != null) {
                File f = getDescriptionStore().getFile(dd.getId());
                if (f != null) {
                    List<Endpoint> eps = pointer.getEndpoints();
                    Endpoint mine = createDataEndpoint().appendToPath(dd.getId());
                    for (Endpoint ep : eps) {
                        if (ep.equals(mine)) {
                            continue;
                        }
                    }
                    current.add(dd.getId());
                }
            }
            possible.add(pointer);
        }
        return possible;
    }

    private List<DataPointer> processPointerCollection(PointerCollection coll) {
        List<DataPointer> possible = new ArrayList<DataPointer>();
        long available = FileUtils.spaceAvailable(getAttic().getDCDataHome(), getAttic().getDataConfig().getMaxLocalData());
        if (available <= 0) {
            log.fine("cannot cache anything - no space left");
            return possible;
        }
        List<DataPointer> pointers = coll.getDataPointers();
        for (DataPointer pointer : pointers) {
            DataDescription dd = pointer.getDataDescription();
            if (dd == null) {
                continue;
            }
            if (dd.getId() == null) {
                continue;
            }
            if (getDescriptionStore().getDataDescription(dd.getId()) != null) {
                continue;
            }
            if (current.contains(dd.getId())) {
                return null;
            }
            FileHash fh = dd.getHash();
            if (fh == null) {
                continue;
            }
            long length = fh.getSize();
            if (length <= 0) {
                continue;
            }
            if (length <= available) {
                possible.add(pointer);
            }
            current.add(dd.getId());
        }
        return possible;
    }

    public void setDescriptionStore(DescriptionStore descriptionStore) {
        this.descriptionStore = descriptionStore;
    }

    public File getDataHome() {
        return getAttic().getDCDataHome();
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

    public void descriptionMapped(DataDescription description, File file) {
    }

    private static class Downloads implements Runnable {

        private List<DataPointer> pointers;
        private File target;
        private Attic attic;
        private DataReceiver receiver;

        private Downloads(List<DataPointer> pointers, File target, Attic attic, DataReceiver receiver) {
            this.pointers = pointers;
            this.target = target;
            this.attic = attic;
            this.receiver = receiver;
        }

        public void run() {
            for (DataPointer pointer : pointers) {
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

    protected void updateStores(File f, DataDescription dd) {
        getDescriptionStore().put(f, dd);
        current.remove(dd.getId());
    }

    private static class QueryTask extends TimerTask {

        static Logger log = Logger.getLogger("org.atticfs.impl.roles.dc.DataCenter.QueryTask");

        private DataCenter dc;
        private Constraints constrains;

        private QueryTask(DataCenter dc, Constraints constrains) {
            this.dc = dc;
            this.constrains = constrains;
        }

        public void run() {
            log.fine("Query timer running....");
            boolean running = true;
           // boolean startservice = true;

          //  while (running) {
                try {
            //        if (startservice) {
                        dc.query(constrains);
                     //   startservice = false;
              //      }
                } catch (IOException e) {
                    log.warning("error during query:" + e.getMessage() + "\n" + FileUtils.formatThrowable(e));
                  //  startservice = true;
                } catch (Exception e) {
                    //log.warning(e.getMessage());
                //    startservice = true;
                }

          //  }

        }
    }


}
