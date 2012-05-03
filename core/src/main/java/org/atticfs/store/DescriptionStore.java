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

import org.atticfs.types.DataDescription;
import org.atticfs.types.FileHash;

import java.io.File;
import java.util.List;

/**
 * provides access to descriptions based on the id,
 * and FileHashes based on a description id.
 * FileHashes represent the chunks actually present on the host.
 * This may be a subset of those defined in the data description, which should be
 * a full description received from some authority.
 *
 * 
 */
public interface DescriptionStore {

    public DataDescription getDataDescription(String id);

    public FileHash getFileHash(String id);

    public DataDescription removeDescription(String id);

    public List<DataDescription> getDataDescriptions();

    public File getFile(String id);

    public File getFile(DataDescription description);

    public void put(File f, DataDescription description);

    /**
     * remove a mapping to a file, but do not remove the actual file
     *
     * @param id
     */
    public void removeMapping(String id);

    /**
     * remove a mapping, and remove the actual file.
     *
     * @param id
     */
    public void removeFile(String id);

    public void init();

    public void shutdown();
}
