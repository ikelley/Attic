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

package org.atticfs.download.request;

import org.atticfs.download.table.DownloadTable;
import org.atticfs.types.DataDescription;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * collection of HashMappings
 * reserve mappings are those for which no information was provided by the data host.
 * These have all the chunks defined in them, as defined in the DataDescription.
 *
 * 
 */

public class RequestCollection {

    static Logger log = Logger.getLogger("org.atticfs.download.request.RequestCollection");


    private DataDescription dataDescription;

    private List<EndpointRequest> mappings = new Vector<EndpointRequest>();
    private List<EndpointRequest> reserves = new Vector<EndpointRequest>();
    private DownloadTable.EndpointRequestComparator c = new DownloadTable.EndpointRequestComparator();

    public RequestCollection(DataDescription dataDescription) {
        this.dataDescription = dataDescription;
    }

    public RequestCollection() {
    }

    public DataDescription getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(DataDescription dataDescription) {
        this.dataDescription = dataDescription;
    }

    public List<EndpointRequest> getMappings() {
        EndpointRequest[] arr = mappings.toArray(new EndpointRequest[mappings.size()]);
        Arrays.sort(arr, c);
        return Arrays.asList(arr);
    }

    public void addMapping(EndpointRequest hash) {
        if (hash.getEndpoint() != null) {
            this.mappings.add(hash);
        }
    }

    public List<EndpointRequest> getReserveMappings() {
        EndpointRequest[] arr = reserves.toArray(new EndpointRequest[reserves.size()]);
        Arrays.sort(arr, c);
        return Arrays.asList(arr);
    }

    public void addReserveMapping(EndpointRequest hash) {
        if (hash.getEndpoint() != null) {
            this.reserves.add(hash);
        }
    }

    /**
     * returns the ordered list of all the mappings in string form
     *
     * @return
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("RequestCollection: hashmappings:\n");
        List<EndpointRequest> main = getMappings();
        sb.append("main:\n");
        for (EndpointRequest hashMapping : main) {
            sb.append(hashMapping.toString());
        }
        List<EndpointRequest> reserve = getReserveMappings();
        sb.append("reserve:\n");
        for (EndpointRequest hashMapping : reserve) {
            sb.append(hashMapping.toString());
        }
        return sb.toString();

    }


}