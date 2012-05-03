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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class Description Here...
 *
 * 
 */

public class Constraints extends WireType {

    private Map<String, Constraint> constraints = new HashMap<String, Constraint>();

    public Constraints() {
        super(WireType.Type.Constraints);
    }

    public void addConstraint(Constraint constraint) {
        constraints.put(constraint.getKey(), constraint);
    }

    public void addConstraint(String key, String value) {
        constraints.put(key, new Constraint(key, value));
    }

    public void addConstraint(String key, Boolean value) {
        constraints.put(key, new Constraint(key, value));
    }

    public void addConstraint(String key, Double value) {
        constraints.put(key, new Constraint(key, value));
    }

    public void addConstraint(String key, Integer value) {
        constraints.put(key, new Constraint(key, value));
    }

    public int size() {
        return constraints.size();
    }

    public String getValue(String key) {
        Constraint c = constraints.get(key);
        if (c != null) {
            return c.getValue();
        }
        return null;
    }

    public int getIntegerValue(String key) {
        Constraint c = constraints.get(key);
        if (c != null) {
            return c.getIntegerValue();
        }
        return -1;
    }

    public double getDoubleValue(String key) {
        Constraint c = constraints.get(key);
        if (c != null) {
            return c.getDoubleValue();
        }
        return -1;
    }

    public boolean getBooleanValue(String key) {
        Constraint c = constraints.get(key);
        if (c != null) {
            return c.getBooleanValue();
        }
        return false;
    }

    public Constraint removeConstraint(String key) {
        return constraints.remove(key);
    }

    public Constraint getConstraint(String key) {
        return constraints.get(key);
    }

    public List<Constraint> getConstraints() {
        return new ArrayList<Constraint>(constraints.values());
    }

    public List<String> getConstraintKeys() {
        return new ArrayList<String>(constraints.keySet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constraints that = (Constraints) o;

        if (constraints != null ? !constraints.equals(that.constraints) : that.constraints != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return constraints != null ? constraints.hashCode() : 0;
    }
}
