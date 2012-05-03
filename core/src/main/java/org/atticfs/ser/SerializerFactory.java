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

package org.atticfs.ser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Class Description Here...
 *
 * 
 */

public class SerializerFactory {

    private static Map<String, Serializer> serializers = new HashMap<String, Serializer>();

    /**
     * return true if an existing serializer class was already registered with this mime type
     *
     * @param ser
     * @return
     */
    public static boolean registerSerializer(Serializer ser) {
        Serializer existing = serializers.put(ser.getMimeType(), ser);
        return existing != null;
    }

    public static Serializer getSerializerForMime(String mime) {
        return serializers.get(mime);
    }

    public static String[] getRegisteredMimeTypes() {
        return serializers.keySet().toArray(new String[serializers.size()]);
    }

    public static Serializer getSerializer(String className) {
        Set<String> set = serializers.keySet();
        for (String s : set) {
            Serializer ser = serializers.get(s);
            if (ser.getClass().getName().equals(className)) {
                return ser;
            }
        }
        return null;
    }
}
