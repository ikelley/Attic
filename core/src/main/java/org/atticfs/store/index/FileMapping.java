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

import org.atticfs.types.FileHash;

import java.io.File;

/**
 * Class Description Here...
 *
 * 
 */

public class FileMapping {

    private File file;
    private FileHash fileHash;

    public FileMapping(File file, FileHash fileHash) {
        this.file = file;
        this.fileHash = fileHash;
    }

    public File getFile() {
        return file;
    }

    public FileHash getFileHash() {
        return fileHash;
    }
}
