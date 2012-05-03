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

import org.atticfs.identity.Identity;
import org.atticfs.types.Constraints;
import org.atticfs.types.DataAdvert;

import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */
public interface DataAdvertStore {

    /**
     * return a DataAdvert if it was successfully added
     * NOTE: this does NOT return a previously mapped
     * advert - it should return the newly added advert if it has been added
     * as it may have been altered
     *
     * @param advert
     * @return
     */
    public DataAdvert addDataAdvert(Identity identity, DataAdvert advert);

    public DataAdvert getDataAdvert(String id);

    public DataAdvert deleteDataAdvert(Identity identity, String id);

    public List<DataAdvert> getDataAdverts();

    public List<DataAdvert> getDataAdverts(Constraints constraints);

    /**
     * notification that the data the advert refers to has been cached by someone.
     *
     * @param advert
     */
    public void dataCached(DataAdvert advert);

    /**
     * notification that the data the advert refers to has been uncached in some way
     * (i.e., less nodes now advertise the data)
     *
     * @param advert
     */
    public void dataUncached(DataAdvert advert);

    public Identity getIdentity(DataAdvert advert);

    public void init();

    public void shutdown();


}
