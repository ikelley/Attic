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

package org.atticfs.store;

import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;

import java.util.List;

/**
 * used by receivers of data pointers, as opposed to minters.
 * This maps DataPointers to Endpoints from which you can get the DataPointer.
 *
 * 
 */
public interface DataPointerLocationStore {

    /**
     * add an Endpoint -> DataPointer mapping
     *
     * @param endpoint
     * @param pointer
     */
    public void add(Endpoint endpoint, DataPointer pointer);

    /**
     * remove a particular DataPointer
     *
     * @param pointer
     * @return
     */
    public Endpoint remove(DataPointer pointer);

    /**
     * get the Endpoint associated with a DataPointer
     *
     * @param pointer
     * @return
     */
    public Endpoint get(DataPointer pointer);

    /**
     * get all Pointers associated with a DataDescription id
     *
     * @param descriptionId
     * @return
     */
    public List<DataPointer> get(String descriptionId);

    /**
     * remove all DataPointers associated with a DataDescription id
     *
     * @param descriptionId
     * @return
     */
    public List<DataPointer> remove(String descriptionId);

    public void init();

    public void shutdown();

}
