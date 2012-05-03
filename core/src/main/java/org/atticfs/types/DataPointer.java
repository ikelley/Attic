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

package org.atticfs.types;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ikelley
 * Date: May 6, 2008
 * Time: 8:22:26 PM
 */
public class DataPointer extends WireType {

    static Logger log = Logger.getLogger("org.atticfs.types.DataPointer");

    private DataDescription dataDescription = null;
    private Set<Endpoint> endpoints = new HashSet<Endpoint>();

    public DataPointer() {
        this(new DataDescription());
    }

    public DataPointer(DataDescription dataDescription) {
        super(WireType.Type.DataPointer);
        this.dataDescription = dataDescription;
    }

    public DataPointer(DataDescription du, Set<Endpoint> dcs) {
        super(WireType.Type.DataPointer);
        this.dataDescription = du;
        this.endpoints.addAll(dcs);
    }

    public DataDescription getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(DataDescription dataDescription) {
        this.dataDescription = dataDescription;
    }

    public Set<Endpoint> getEndpointSet() {
        return endpoints;
    }

    public List<Endpoint> getEndpoints() {
        return new ArrayList<Endpoint>(endpoints);
    }

    public void setEndpoints(Set<Endpoint> hosts) {
        this.endpoints.clear();
        this.endpoints.addAll(hosts);
    }

    public boolean addEndpoint(Endpoint dc) {
        boolean b = this.endpoints.add(dc);
        log.fine("DataPointer.updateDataPointer ADDED=" + b);
        return b;
    }

    public boolean removeEndpoint(Endpoint dc) {
        boolean b = this.endpoints.remove(dc);
        log.fine("DataPointer.removeEndpoint REMOVED=" + b);
        return b;
    }

    public void addEndpoint(String uri) {
        this.endpoints.add(new Endpoint(uri));
    }

    public void addEndpoint(String uri, String meta) {
        Endpoint ep = new Endpoint(uri);
        ep.setMetaEndpoint(meta);
        this.endpoints.add(ep);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataPointer that = (DataPointer) o;

        if (dataDescription != null ? !dataDescription.equals(that.dataDescription) : that.dataDescription != null) return false;
        if (endpoints != null ? !endpoints.equals(that.endpoints) : that.endpoints != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dataDescription != null ? dataDescription.hashCode() : 0;
        result = 31 * result + (endpoints != null ? endpoints.hashCode() : 0);
        return result;
    }
}
