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

import org.atticfs.channel.ChannelData;
import org.atticfs.channel.OutChannel;
import org.atticfs.event.DataEvent;
import org.atticfs.event.DataReceiver;
import org.atticfs.types.Constraints;
import org.atticfs.types.DataAdvert;
import org.atticfs.types.DataDescription;
import org.atticfs.types.Endpoint;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class DataCenterReceiver implements DataReceiver {

    static Logger log = Logger.getLogger("org.atticfs.impl.roles.dc.DataCenterReceiver");

    private Endpoint dcEndpoint;
    private Endpoint dlsEndpoint;
    private Constraints constraints;
    private DataCenter dc;

    public DataCenterReceiver(Endpoint dcEndpoint, Endpoint dlsEndpoint, Constraints constraints, DataCenter dc) {
        this.dcEndpoint = dcEndpoint;
        this.dlsEndpoint = dlsEndpoint;
        this.constraints = constraints;
        this.dc = dc;
    }

    public DataCenterReceiver(Endpoint dcEndpoint, Endpoint dlsEndpoint, DataCenter dc) {
        this(dcEndpoint, dlsEndpoint, null, dc);
    }

    public void dataArrived(DataEvent event) {
        if (event.isSuccessful()) {
            event.getStats().print(System.out);
            int count = 2;
            boolean success = false;
            while (count > 0) {
                try {
                    notifyDls(event.getDataDescription(), event.getFile());
                    count = 0;
                    success = true;
                }catch (IOException ioe) {
                    count--;
                }
                catch (Exception e) {
                    count--;
                }
            }
            if (!success) {
                log.warning("Could not notify DLS of cached data!");
            }
        }
    }

    public void notifyDls(DataDescription desc, File file) throws Exception {
        if (desc == null || file == null || desc.getId() == null) {
            log.warning("not enough info to notify DLS.");
        }
        DataAdvert ad = new DataAdvert();
        ad.setDataDescription(desc.metadataCopy());
        ad.setEndpoint(dcEndpoint.appendToPath(desc.getId()));
        ad.setConstraints(constraints);
        OutChannel out = dc.getAttic().getChannelFactory().createOutChannel(null);
        ChannelData cd = new ChannelData(ChannelData.Action.CREATE, dlsEndpoint.appendToPath(desc.getId()));
        cd.setRequestData(ad);
        cd.setCloseOnFinish(true);
        ChannelData response = out.send(cd);
        if (response.getOutcome() == ChannelData.Outcome.OK) {
            // todo - getting a datapointer back here - what to do with it?
            log.fine("successfully notified DLS of my download");
            log.fine("adding file to metadata stores.");
            dc.updateStores(file, desc);
        } else {
            log.warning("failed to notify DLS of my download. Outcome was:" +
                    response.getOutcome() + ". Detail:" + response.getOutcomeDetail());
        }
    }
}
