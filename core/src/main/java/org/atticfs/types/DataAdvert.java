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

/**
 * A DataAdvert should at least contain a replica value and possibly
 * an expires value.
 *
 * 
 */

public class DataAdvert extends WireType {

    public static final String REPLICA = "replica";
    public static final String EXPIRY = "expires";
    public static final String DEREFERENCE = "dereference";

    private DataDescription dataDescription;
    private Endpoint endpoint;
    private Constraints constraints = new Constraints();


    public DataAdvert() {
        super(WireType.Type.DataAdvert);
    }

    public DataAdvert(DataDescription dataDescription) {
        super(WireType.Type.DataAdvert);
        this.dataDescription = dataDescription;
    }

    public DataAdvert(DataDescription dataDescription, Endpoint endpoint) {
        super(WireType.Type.DataAdvert);
        this.dataDescription = dataDescription;
        this.endpoint = endpoint;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public DataDescription getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(DataDescription dataDescription) {
        this.dataDescription = dataDescription;
    }

    public Constraints getConstraints() {
        return constraints;
    }

    public void setConstraints(Constraints constraints) {
        if (constraints != null) {
            this.constraints = constraints;
        }
    }

    public void addConstraint(Constraint constraint) {
        constraints.addConstraint(constraint);
    }

    public Constraint getConstraint(String key) {
        return constraints.getConstraint(key);
    }
}
