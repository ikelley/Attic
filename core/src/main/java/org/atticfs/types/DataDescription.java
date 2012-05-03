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

import java.util.UUID;


/**
 * User: flcosta
 * Date: 19/Jun/2008
 * Time: 15:02:30
 */
public class DataDescription extends WireType {

    private String name = "nobody";
    private String id;
    private String description = "";
    private String project = "";
    private String location = null;

    private FileHash hash;

    public DataDescription() {
        this(UUID.randomUUID().toString(), "");
    }

    public DataDescription(String id) {
        this(id, "");
    }

    public DataDescription(String id, String name) {
        super(WireType.Type.DataDescription);
        this.id = id;
        this.name = name;
    }

    public FileHash getHash() {
        return hash;
    }

    public void setHash(FileHash hash) {
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String createId() {
        return UUID.randomUUID().toString();
    }

    /**
     * returns a DataDescription with only the metadata in it
     * not the file hash info.
     *
     * @return
     */
    public DataDescription metadataCopy() {
        DataDescription dd = new DataDescription(getId(), getName());
        dd.setDescription(getDescription());
        dd.setName(getName());
        dd.setProject(getProject());
        dd.setLocation(getLocation());
        return dd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataDescription that = (DataDescription) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }
}
