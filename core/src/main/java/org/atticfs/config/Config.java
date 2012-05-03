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

package org.atticfs.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Class Description Here...
 *
 * 
 */

public abstract class Config {

    private Set<String> calledMethods = new HashSet<String>();


    protected Method findSetter(String key) {
        String name = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
        if (calledMethods.contains(name)) {
            return null;
        }
        Method[] ms = getClass().getMethods();
        for (Method m : ms) {
            if (m.getName().equals(name) && m.getReturnType() == void.class && m.getParameterTypes().length == 1) {
                return m;
            }
        }
        return null;
    }

    protected String createKey(Method m) {
        String name = m.getName();
        if (name.startsWith("get")) {
            return name.substring(3, 4).toLowerCase() + name.substring(4);
        } else if (name.startsWith("is")) {
            return name.substring(2, 3).toLowerCase() + name.substring(3);
        }
        return null;
    }

    private Object getSimpleObject(String value, Class cls) {
        String realVal = value;

        try {
            if (cls.equals(String.class)) {
                return realVal;
            }
            if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
                if (realVal.equalsIgnoreCase("on")) {
                    realVal = "true";
                }
            }
            if (isPrimitiveOrWrapper(cls) || cls.isEnum()) {
                if (isPrimitive(cls)) {
                    cls = getWrapperForPrimitive(cls);
                }
                Method m = cls.getMethod("valueOf", String.class);
                return m.invoke(null, realVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void store(OutputStream out) throws IOException {
        Properties p = new Properties();
        Method[] ms = getClass().getDeclaredMethods();
        for (Method m : ms) {
            String name = m.getName();
            if (name.startsWith("get") || name.startsWith("is")) {
                if (m.getParameterTypes().length == 0 && isAcceptable(m.getReturnType())) {
                    try {
                        Object ret = m.invoke(this, new Object[0]);
                        String key = createKey(m);
                        if (key != null) {
                            p.setProperty(key, ret.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        p.store(out, getClass().getSimpleName() + " Properties");
    }

    public void load(InputStream in) throws IOException {
        Properties p = new Properties();
        p.load(in);
        Iterator it = p.keySet().iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            Method m = findSetter(s);
            if (m != null) {
                Object val = getSimpleObject(p.getProperty(s), m.getParameterTypes()[0]);
                try {
                    m.invoke(this, val);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static boolean isPrimitive(Class cls) {
        return cls.equals(boolean.class) ||
                cls.equals(byte.class) ||
                cls.equals(short.class) ||
                cls.equals(char.class) ||
                cls.equals(int.class) ||
                cls.equals(long.class) ||
                cls.equals(double.class) ||
                cls.equals(float.class);
    }


    private static Class getWrapperForPrimitive(Class cls) {
        if (cls.equals(boolean.class)) {
            return Boolean.class;
        } else if (cls.equals(byte.class)) {
            return Byte.class;
        } else if (cls.equals(short.class)) {
            return Short.class;
        } else if (cls.equals(char.class)) {
            return Character.class;
        } else if (cls.equals(int.class)) {
            return Integer.class;
        } else if (cls.equals(long.class)) {
            return Long.class;
        } else if (cls.equals(double.class)) {
            return Double.class;
        } else if (cls.equals(float.class)) {
            return Float.class;
        } else {
            return null;
        }
    }

    public static boolean isPrimitiveOrWrapper(Class cls) {
        return isPrimitive(cls) || isWrapper(cls);
    }

    public boolean isAcceptable(Class cls) {
        return cls.isEnum() || cls.equals(String.class) || isPrimitiveOrWrapper(cls);
    }

    public static boolean isWrapper(Class cls) {
        return cls.equals(Boolean.class) ||
                cls.equals(Byte.class) ||
                cls.equals(Short.class) ||
                cls.equals(Character.class) ||
                cls.equals(Integer.class) ||
                cls.equals(Long.class) ||
                cls.equals(Double.class) ||
                cls.equals(Float.class);
    }

    protected void setterCalled(String method) {
        calledMethods.add(method);
    }


}
