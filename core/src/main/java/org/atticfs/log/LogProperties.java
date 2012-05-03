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

package org.atticfs.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Properties that are logged. These have a name, and a set of key/value pairs.
 * They can recursively contain other properties.
 * You can also store and retrieve lists of values from LogProperties.
 * to store use:
 * <p/>
 * putArray(String key String[] values).
 * <p/>
 * to retrieve, check that the value is an array using:
 * <p/>
 * isArray(String key);
 * <p/>
 * then use:
 * <p/>
 * getArray(String key) which returns an array.
 * <p/>
 * If an array is stored and the simple:
 * <p/>
 * get(String key)
 * <p/>
 * is sued on it, the first element of the array will be returned.
 * <p/>
 * The toString() method of this class returns a nicely formatted output
 * of the Properties.
 *
 * @author Andrew Harrison
 * @version $Revision: 1.5 $
 * @created Jun 18, 2008: 6:18:21 PM
 * @date $Date: 2009-02-19 16:02:56 $ modified by $Author: harrison $
 * @todo Put your notes here...
 */

public class LogProperties {

    public static final String NL = "\n";

    private String name;
    private static boolean singleLine = false;
    private TreeMap<String, String[]> props = new TreeMap<String, String[]>();
    private List<LogProperties> logProperties = new ArrayList<LogProperties>();
    private HashMap<String, Boolean> lists = new HashMap<String, Boolean>();

    public LogProperties(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static boolean isSingleLine() {
        return singleLine;
    }

    public static void setSingleLine(boolean b) {
        singleLine = b;
    }

    public void addLogProperties(LogProperties logProps) {
        removeLogProperties(logProps.getName());
        logProperties.add(logProps);
    }

    public void put(String key, String value) {
        props.put(key, new String[]{value});
        lists.put(key, false);
    }

    public boolean isArray(String key) {
        Boolean b = lists.get(key);
        if (b != null && b.booleanValue()) {
            return true;
        }
        return false;
    }

    public void putArray(String key, String[] values) {
        props.put(key, values);
        lists.put(key, true);
    }

    public String get(String key) {
        String[] ss = props.get(key);
        if (ss != null && ss.length > 0) {
            return ss[0];
        }
        return null;
    }

    public String[] getArray(String key) {
        return props.get(key);
    }

    public String[] keys() {
        return props.keySet().toArray(new String[props.size()]);
    }

    public LogProperties[] getAllLogProperties() {
        return logProperties.toArray(new LogProperties[logProperties.size()]);
    }

    public LogProperties getLogProperties(String name) {
        for (LogProperties logProperty : logProperties) {
            if (logProperty.getName().equals(name)) {
                return logProperty;
            }
        }
        return null;
    }

    public void removeLogProperties(String name) {
        for (LogProperties logProperty : logProperties) {
            if (logProperty.getName().equals(name)) {
                logProperties.remove(logProperty);
                break;
            }
        }
    }

    protected String fromLogProperties(LogProperties logProps, int indent, int incr) {

        String ind = "";
        for (int i = 0; i < indent; i++) {
            ind += " ";
        }
        String sep = " ";
        if (!isSingleLine()) {
            sep = NL + ind;
        }
        StringBuilder sb = new StringBuilder(sep + logProps.getName() + "(");
        for (int i = 0; i < incr; i++) {
            ind += " ";
        }
        sb.append(sep);
        String[] keys = logProps.keys();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (logProps.isArray(key)) {
                String[] vals = logProps.getArray(key);
                sb.append(key).append("[");
                for (int j = 0; j < vals.length; j++) {
                    sb.append(vals[j]);
                    if (j < vals.length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("]");
            } else {
                sb.append(key).append(":").append(logProps.get(key));
            }
            if (i < keys.length - 1) {
                sb.append(sep);
            }
        }
        LogProperties[] sub = logProps.getAllLogProperties();
        for (LogProperties logProperties : sub) {
            sb.append(fromLogProperties(logProperties, indent + incr, incr));
        }
        sb.append(" )");
        return sb.toString();
    }

    public String toString() {
        return fromLogProperties(this, 0, 2);
    }

}
