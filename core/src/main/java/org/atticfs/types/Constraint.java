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

import org.atticfs.util.FileUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class Constraint extends WireType {

    static Logger log = Logger.getLogger("org.atticfs.types.Constraint");


    private static SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");


    public enum Type {
        String,
        Boolean,
        Double,
        Integer,
        Long,
        Date
    }

    private Type type;
    private String key;
    private String value;


    public Constraint(Type type, String key, String value) {
        super(WireType.Type.Constraint);
        if (type == null) {
            type = Type.String;
        }
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public Constraint(String key, String value) {
        this(Type.String, key, value);
    }

    public Constraint(String key, Boolean value) {
        super(WireType.Type.Constraint);
        this.type = Type.Boolean;
        this.key = key;
        this.value = value.toString();
    }

    public Constraint(String key, Double value) {
        super(WireType.Type.Constraint);
        this.type = Type.Double;
        this.key = key;
        this.value = value.toString();
    }

    public Constraint(String key, Integer value) {
        super(WireType.Type.Constraint);
        this.type = Type.Integer;
        this.key = key;
        this.value = value.toString();
    }

    public Constraint(String key, Long value) {
        super(WireType.Type.Constraint);
        this.type = Type.Long;
        this.key = key;
        this.value = value.toString();
    }

    public Constraint(String key, Date value) {
        super(WireType.Type.Constraint);
        this.type = Type.Date;
        this.key = key;
        try {
            this.value = format.format(value);
        } catch (Exception e) {
            log.warning("Error formatting date:" + FileUtils.formatThrowable(e));
            this.type = Type.String;
            this.value = "null";
        }
    }

    public Type getConstraintType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean getBooleanValue() {
        return Boolean.valueOf(value);
    }

    public double getDoubleValue() {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int getIntegerValue() {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public long getLongValue() {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public Date getDateValue() {
        try {
            return format.parse(value);
        } catch (ParseException e) {

        }
        return new Date(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constraint that = (Constraint) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (type != that.type) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public static void main(String[] args) {
        Constraint c = new Constraint("key", 3);
    }
}
