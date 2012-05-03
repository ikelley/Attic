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
 * Class Description Here...
 *
 * 
 */

public class DataQuery extends WireType {

    private Endpoint endpoint;
    private Constraints constraints = new Constraints();

    public DataQuery() {
        super(WireType.Type.DataQuery);
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataQuery that = (DataQuery) o;

        if (constraints != null ? !constraints.equals(that.constraints) : that.constraints != null) return false;
        if (endpoint != null ? !endpoint.equals(that.endpoint) : that.endpoint != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpoint != null ? endpoint.hashCode() : 0;
        result = 31 * result + (constraints != null ? constraints.hashCode() : 0);
        return result;
    }
}
