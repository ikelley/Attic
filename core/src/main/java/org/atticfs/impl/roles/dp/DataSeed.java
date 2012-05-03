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

import org.atticfs.Attic;
import org.atticfs.impl.store.MemoryDataAdvertStore;
import org.atticfs.impl.store.MemoryDataPointerStore;
import org.atticfs.impl.store.MemoryDataQueryStore;
import org.atticfs.roles.AdvertStorage;
import org.atticfs.roles.handlers.SeedRequestHandler;
import org.atticfs.store.DataAdvertStore;
import org.atticfs.store.DataPointerStore;
import org.atticfs.store.DataQueryStore;
import org.atticfs.types.Endpoint;
import org.atticfs.util.StringConstants;

import java.io.IOException;

/**
 * Extends a DataPublisher to add a SeedRequestHandler.
 * This handler receives data from elsewhere and then publishes
 * it.
 *
 * 
 */

public class DataSeed extends DataPublisher implements AdvertStorage {


    private DataAdvertStore dataAdvertStore;
    private DataQueryStore dataQueryStore;
    private DataPointerStore dataPointerStore;

    public DataSeed(Endpoint publishEndpoint) {
        super(publishEndpoint);
    }

    public DataSeed() {
    }

    @Override
    public void init(Attic attic) throws IOException {
        super.init(attic);
        if (dataAdvertStore == null) {
            dataAdvertStore = new MemoryDataAdvertStore();
        }
        if (dataQueryStore == null) {
            dataQueryStore = new MemoryDataQueryStore();
        }
        if (dataPointerStore == null) {
            dataPointerStore = new MemoryDataPointerStore();
        }
        addChannelRequestHandler(StringConstants.SEED_KEY, new SeedRequestHandler(dataAdvertStore,
                getDescriptionStore(),
                getAttic().getDPDataHome(),
                this));
        dataAdvertStore.init();
        dataQueryStore.init();
        dataPointerStore.init();

    }

    public void shutdown() throws IOException {
        super.shutdown();
        dataAdvertStore.shutdown();
        dataQueryStore.shutdown();
        dataPointerStore.shutdown();

    }


    public DataAdvertStore getDataAdvertStore() {
        return dataAdvertStore;
    }

    public void setDataAdvertStore(DataAdvertStore dataAdvertStore) {
        this.dataAdvertStore = dataAdvertStore;
    }

    public DataQueryStore getDataQueryStore() {
        return dataQueryStore;
    }

    public void setDataQueryStore(DataQueryStore dataQueryStore) {
        this.dataQueryStore = dataQueryStore;
    }

    public DataPointerStore getDataPointerStore() {
        return dataPointerStore;
    }

    public void setDataPointerStore(DataPointerStore dataPointerStore) {
        this.dataPointerStore = dataPointerStore;
    }


}