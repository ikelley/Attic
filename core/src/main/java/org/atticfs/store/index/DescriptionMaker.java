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

package org.atticfs.store.index;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.atticfs.types.DataDescription;
import org.atticfs.types.FileHash;
import org.atticfs.util.FileUtils;
import org.atticfs.util.StringConstants;

/**
 * receives callbacks from an Indexer.
 * This uses a DataDescription as a template and adds the file name and file hash to it
 * before returning the two to a DescriptionListener.
 * <p/>
 * The description listener is responsible for persisting the description and doing
 * whatever else is required once a description -> file mapping has been created.
 *
 * 
 */

public class DescriptionMaker implements IndexListener {

    static Logger log = Logger.getLogger("org.atticfs.store.index.DescriptionMaker");


    private DataDescription description;
    private DescriptionListener listener;
    private boolean moveIndexedFile = false;

    public DescriptionMaker(DataDescription description, DescriptionListener listener, boolean moveIndexedFile) {
        this.description = description;
        this.listener = listener;
        this.moveIndexedFile = moveIndexedFile;
    }

    public void index(FileMapping mapping) {
        FileHash hash = mapping.getFileHash();
        File f = mapping.getFile();
        log.fine("index with file:" + f.getAbsolutePath());
        DataDescription dd = description.metadataCopy();
        dd.setHash(hash);
        //dd.setName(f.getName());
        dd.setName(description.getName());
        File indexed = new File(f.getParentFile(), dd.getId() + StringConstants.EXT_DATA);
        if (moveIndexedFile) {
            boolean rename = FileUtils.rename(f, indexed);
            if (!rename) {
                log.warning("Could not rename:" + f.getAbsolutePath());
            } else {
                listener.descriptionMapped(dd, indexed);
            }
        } else {
            try {
                FileUtils.copyFilesRecursive(f, indexed);
                listener.descriptionMapped(dd, indexed);
            } catch (IOException e) {
                log.warning("Error copying files:" + e.getMessage());
            }
        }

    }

    public void indexComplete() {
    }
}
