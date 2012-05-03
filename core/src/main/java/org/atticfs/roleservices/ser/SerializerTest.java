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

package org.atticfs.roleservices.ser;

import java.io.IOException;

import org.atticfs.impl.ser.xml.XmlSerializer;
import org.atticfs.ser.Serializer;
import org.atticfs.ser.SerializerFactory;
import org.atticfs.types.DataAdvert;
import org.atticfs.types.DataPointer;

/**
 * Class Description Here...
 *
 * 
 */

public class SerializerTest {

    public void testPointer() {
        DataPointer dp = TypeMaker.createDataPointer("abc");
        SerializerFactory.registerSerializer(new XmlSerializer());
        Serializer ser = SerializerFactory.getSerializer("org.atticfs.impl.ser.xml.XmlSerializer");
        try {
            ser.toStream(dp, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testAdvert() {
        DataAdvert da = TypeMaker.createDataAdvert("abc");
        Serializer ser = SerializerFactory.getSerializer("org.atticfs.impl.ser.json.JsonSerializer");
        try {
            ser.toStream(da, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SerializerTest().testPointer();
    }
}
