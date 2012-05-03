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

package org.atticfs.roleservices.run;

import org.atticfs.Attic;
import org.atticfs.impl.roles.dw.DataWorker;
import org.atticfs.impl.roles.main.ConfigServiceRole;
import org.atticfs.impl.ser.json.JsonSerializer;
import org.atticfs.impl.ser.xhtml.XhtmlSerializer;
import org.atticfs.impl.ser.xml.XmlSerializer;
import org.atticfs.types.Endpoint;
import org.atticfs.util.StringConstants;

import java.io.IOException;

/**
 * Class Description Here...
 *
 * 
 */

public class Dworker {

    public static void main(String[] args) {
        System.out.println("****************************************");
        System.out.println("invoking Dworker with args:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println("****************************************");
        if (args.length == 0) {
            System.out.println("I need a Data Pointer endpoint....");
            System.exit(0);
        }
        String pointer = args[0].trim();
        String home = null;
        if (args.length > 1) {
            home = args[1];
        }

        Attic attic;
        if (home == null) {
            attic = new Attic();
        } else {
            attic = new Attic(home);
        }
        attic.getSecurityConfig().setSecure(false);
        attic.getSecurityConfig().setTestMode(true);

        String serializer = null;
         if(args.length == 2) {
         serializer = args[2];
        }
        if (serializer != null) {
            if (serializer.equals("XHTML")) {
               attic.setSerializer(new XhtmlSerializer());
            } else if (serializer.equals("JSON")) {
               attic.setSerializer(new JsonSerializer());
            } else {
               attic.setSerializer(new XmlSerializer());
            }
        } else {
            attic.setSerializer(new XmlSerializer());
        }

        attic.cleanRoles();
        attic.addRole(StringConstants.ROLE_DW);
        ConfigServiceRole confService = new ConfigServiceRole();
        attic.attach("attic", confService);

        DataWorker dw = new DataWorker();
        attic.attach(StringConstants.DATA_WORKER, dw);
        attic.init();
        try {
            dw.getPointer(new Endpoint(new String(pointer)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (attic.getSerializer() instanceof  XhtmlSerializer) {
            System.out.println("Using XHTML Serializer");
        } else if (attic.getSerializer() instanceof  XmlSerializer) {
            System.out.println("Using XML Serializer");
        } else if (attic.getSerializer() instanceof  JsonSerializer) {
            System.out.println("Using JSON Serializer");
        }


    }
}