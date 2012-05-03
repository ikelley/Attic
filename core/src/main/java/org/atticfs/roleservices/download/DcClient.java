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

package org.atticfs.roleservices.download;

import org.atticfs.Attic;
import org.atticfs.download.Downloader;
import org.atticfs.download.request.RequestCollection;
import org.atticfs.download.request.RequestResolver;
import org.atticfs.event.DataEvent;
import org.atticfs.event.DataReceiver;
import org.atticfs.stats.Goodput;
import org.atticfs.roleservices.ser.TypeMaker;
import org.atticfs.types.DataDescription;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class Description Here...
 *
 * 
 */

public class DcClient implements DataReceiver {


    public void download() throws IOException {
        Attic attic = new Attic();
        attic.init();

        attic.getSecurityConfig().setSecure(false);
        attic.getSecurityConfig().setTestMode(true);

        DataPointer dp = createDataPointer();
        RequestCollection mc = RequestResolver.createRequestCollection(dp, attic);

        File download = new File("download-roleservices");
        download.mkdirs();
        Downloader downloader = new Downloader(this, mc, download, attic);
        downloader.download();


    }

    private DataPointer createDataPointer() throws IOException {
        DataDescription dd = TypeMaker.getXmlDataDescription();
        int[] ports = new int[]{8181, 8282, 8383, 8484};
        Set<Endpoint> endpoints = new HashSet<Endpoint>();
        for (int i = 0; i < ports.length; i++) {
            Endpoint endpoint = new Endpoint("http://localhost:" + ports[i] + "/dc");
            endpoints.add(endpoint);
        }
        DataPointer pointer = new DataPointer(dd, endpoints);
        return pointer;

    }

    public void dataArrived(DataEvent event) {
        System.out.println("DcClient.dataArrived " + event.getDetail());
        File f = event.getFile();
        if (f != null) {
            System.out.println("DcClient.dataArrived " + f.getAbsolutePath());
        }
        Goodput c = event.getStats().getGoodput();
        if (c != null) {
            c.compile();
            System.out.println("DcClient.dataArrived kbps:" + c.getKbps());
            System.out.println("DcClient.dataArrived mbps:" + c.getMbps());
            System.out.println("DcClient.dataArrived MBps:" + c.getMBps());

        }

    }

    public static void main(String[] args) {
        try {

            new DcClient().download();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
