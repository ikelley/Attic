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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.atticfs.Attic;
import org.atticfs.store.DescriptionStore;
import org.atticfs.store.index.DescriptionListener;
import org.atticfs.types.DataDescription;
import org.atticfs.types.FileHash;
import org.atticfs.util.FileUtils;
import org.atticfs.util.StringConstants;

/**
 * A Description Store that stores description on disk.
 *
 * 
 */

public class FileDescriptionStore implements DescriptionStore {

    private Attic attic;
    private File dataDir;
    private File descDir;
    private DescriptionListener listener;

    public FileDescriptionStore(Attic attic, File dataDir, File descDir, DescriptionListener listener) {
        this.attic = attic;
        this.dataDir = dataDir;
        this.descDir = descDir;
        this.listener = listener;
    }

    public DataDescription getDataDescription(String id) {
        File f = createDescFile(id);
        try {
            return read(f);
        } catch (IOException e) {
            return null;
        }
    }

    public FileHash getFileHash(String id) {
        DataDescription desc = getDataDescription(id);
        if (desc != null) {
            return desc.getHash();
        }
        return null;
    }

    public DataDescription removeDescription(String id) {
        File desc = createDescFile(id);
        DataDescription dd = null;
        if (desc.exists() && desc.length() > 0) {
            try {
                dd = read(desc);
            } catch (IOException e) {

            }
            desc.delete();
        }
        return dd;
    }

    public List<DataDescription> getDataDescriptions() {
        return null;
    }

    public File getFile(String id) {
        return createDataFile(id);
    }

    public File getFile(DataDescription description) {
        return createDataFile(description.getId());
    }

    public void put(File f, DataDescription description) {
        try {
            write(descDir, description);
            File mapped = createDataFile(description.getId());
            if (! f.getAbsolutePath().equals(mapped.getAbsolutePath())) {
                if (mapped.exists() && mapped.length() > 0) {
                    mapped.delete();
                    mapped = createDataFile(description.getId());
                }
                FileUtils.rename(f, mapped);
            }
        } catch (IOException e) {
            System.out.println("Could not create file, ERROR! Very Fatal. FileDescriptionStore.java");
        }
    }

    public void removeMapping(String id) {
        File mapped = createDataFile(id);
        if (mapped.exists() && mapped.length() > 0) {
            FileUtils.rename(mapped, createUnmappedFile(id));
        }
    }

    public void removeFile(String id) {
        File data = createDataFile(id);
        if (data.exists()) {
            data.delete();
        }
    }

    public void init() {
        load(dataDir, descDir);
    }

    public void shutdown() {
    }

    private File createDataFile(String id) {
        if (id == null) {
            return null;
        }
        return new File(dataDir, id + StringConstants.EXT_DATA);
    }

    private File createUnmappedFile(String id) {
        if (id == null) {
            return null;
        }
        return new File(dataDir, id + StringConstants.EXT_UNMAPPED);
    }

    private File createDescFile(String id) {
        return new File(descDir, id + StringConstants.EXT_DESC);
    }

    public void write(File descriptionDir, DataDescription description) throws IOException {

        if (descriptionDir.exists() && !descriptionDir.isDirectory()) {
            throw new IOException("File is not a directory!");
        }
        descriptionDir.mkdirs();
        File desc = new File(descriptionDir, description.getId() + StringConstants.EXT_DESC);
        if (desc.exists() && desc.length() > 0) {
            desc.delete();
            desc = new File(descriptionDir, description.getId() + StringConstants.EXT_DESC);
        }
        try {
            FileOutputStream fout = new FileOutputStream(desc);
            attic.getSerializer().toStream(description, fout);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public DataDescription read(File description) throws IOException {
        try {
            FileInputStream fin = new FileInputStream(description);
            DataDescription dd = (DataDescription) attic.getSerializer().fromStream(fin);
            fin.close();
            return dd;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public List<String> load(File data, File desc) {
        List<String> mapped = new ArrayList<String>();
        List<String> ret = new ArrayList<String>();

        File[] datas = data.listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                if (s.endsWith(StringConstants.EXT_DATA)) {
                    return true;
                }
                return false;
            }
        });
        if (datas != null) {
            for (File file : datas) {
                String name = file.getName().replace(StringConstants.EXT_DATA, StringConstants.EXT_DESC);
                File dd = new File(desc, name);
                if (dd.exists() && dd.length() > 0) {
                    try {
                        DataDescription dataD = read(dd);
                        if (dataD != null) {
                            mapped.add(name);
                            if (listener != null) {
                                listener.descriptionMapped(dataD, file);
                            }
                        }
                    } catch (IOException e) {

                    }
                } else {
                    // there is a file but no matching DataDescription
                    // add it as a return value.
                    name = file.getName().replace(StringConstants.EXT_DESC, StringConstants.EXT_UNMAPPED);
                    File unmapped = new File(file.getParentFile(), name);
                    boolean rename = FileUtils.rename(file, unmapped);
                    if (rename) {
                        ret.add(unmapped.getAbsolutePath());
                    } else {
                        ret.add(file.getAbsolutePath());
                    }
                }
            }
        }
        File[] descs = desc.listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                if (s.endsWith(StringConstants.EXT_DESC)) {
                    return true;
                }
                return false;
            }
        });
        if (descs != null) {
            for (File file : descs) {
                if (!mapped.contains(file.getName())) {
                    file.delete();
                }
            }
        }
        return ret;
    }
}
