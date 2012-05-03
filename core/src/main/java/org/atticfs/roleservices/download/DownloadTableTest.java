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
import org.atticfs.download.request.RequestCollection;
import org.atticfs.download.request.RequestResolver;
import org.atticfs.roleservices.ser.TypeMaker;
import org.atticfs.types.DataDescription;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class Description Here...
 *
 * 
 */

public class DownloadTableTest {

    public void testTable() {
        try {
            /*DCTest.main(new String[]{"8181"});
            DCTest.main(new String[]{"8282"});
            DCTest.main(new String[]{"8383"});
            DCTest.main(new String[]{"8484"});
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            DataPointer dp = createDataPointer();
            RequestCollection mc = RequestResolver.createRequestCollection(dp, new Attic().init());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
