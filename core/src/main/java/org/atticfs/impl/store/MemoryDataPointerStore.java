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

package org.atticfs.impl.store;

import org.atticfs.store.DataPointerStore;
import org.atticfs.types.DataAdvert;
import org.atticfs.types.DataPointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Description Here...
 *
 * 
 */

public class MemoryDataPointerStore implements DataPointerStore {

    private Map<String, DataPointer> pointers = new ConcurrentHashMap<String, DataPointer>();

    public DataPointer updateDataPointer(DataAdvert advert) {
        DataPointer dp = pointers.get(advert.getDataDescription().getId());
        if (dp != null) {
            dp.setDataDescription(advert.getDataDescription());
            dp.addEndpoint(advert.getEndpoint());
            return dp;
        }
        return null;

    }

    public DataPointer createDataPointer(DataAdvert advert) {
        DataPointer dp = new DataPointer();
        dp.setDataDescription(advert.getDataDescription());
        dp.addEndpoint(advert.getEndpoint());
        pointers.put(dp.getDataDescription().getId(), dp);
        return dp;
    }


    public DataPointer deleteDataPointer(String id) {
        return pointers.remove(id);
    }

    public DataPointer getDataPointer(String id) {
        return pointers.get(id);
    }

    public DataPointer addEndpointToDataPointer(DataAdvert advert) {
        DataPointer dp = getDataPointer(advert.getDataDescription().getId());
        if (dp != null) {
            dp.addEndpoint(advert.getEndpoint());
            pointers.put(advert.getDataDescription().getId(), dp);
            return dp;
        }
        return null;
    }

    public DataPointer removeEndpointFromDataPointer(DataAdvert advert) {
        DataPointer dp = getDataPointer(advert.getDataDescription().getId());
        if (dp != null) {
            dp.removeEndpoint(advert.getEndpoint());
            pointers.put(advert.getDataDescription().getId(), dp);
            return dp;
        }
        return null;
    }

    public List<DataPointer> getDataPointers() {
        return new ArrayList<DataPointer>(pointers.values());
    }

    public void init() {
    }

    public void shutdown() {
    }

}
