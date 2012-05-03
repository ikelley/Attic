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

package org.atticfs.roles;

import org.atticfs.types.DataAdvert;
import org.atticfs.types.DataDescription;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;

import java.io.File;
import java.io.IOException;

/**
 * Publisher interface.
 * This role typically has files locally that it wants to publish
 *
 * 
 */
public interface Publisher extends ServiceRole {

    /**
     * publish a DataAdvert and return a DataPointer to the application.
     * The endpoint on the network of the DataPointer can be retrieved
     * via getEndpoint(pointer);
     *
     * @param advert
     * @return
     * @throws IOException
     */
    public DataPointer publish(DataAdvert advert) throws Exception;

    public DataPointer unpublish(DataAdvert advert) throws Exception;

    /**
     * get the URL of a data pointer
     *
     * @param pointer
     * @return
     */
    public Endpoint getEndpoint(DataPointer pointer);

    /**
     * create an endpoint which can be added to a DataAdvert
     * representing the seed endpoint of the data
     *
     * @param advert
     * @return
     */
    public Endpoint createEndpoint(DataAdvert advert);

    /**
     * Index a File, or files in a directory. This creates
     * data descriptions for local files.
     * <p/>
     * IMPLEMENTATION NOTE: indexing a file will result in the file being renamed to
     * <data-description-id>.atticd
     * <p/>
     * The location of the file will not change.
     *
     * @param template
     * @param file
     */
    public void index(DataDescription template, File file);

}
