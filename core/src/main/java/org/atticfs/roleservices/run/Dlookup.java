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
import org.atticfs.impl.roles.dl.DataLookup;
import org.atticfs.impl.roles.main.ConfigServiceRole;
import org.atticfs.impl.ser.json.JsonSerializer;
import org.atticfs.impl.ser.xhtml.XhtmlSerializer;
import org.atticfs.impl.ser.xml.XmlSerializer;
import org.atticfs.util.StringConstants;

/**
 * Runs a DataLookup service. Arguments are optional
 * args:
 * 1. local port to run on
 * 2. where to place the Attic home directory
 *
 * 
 */

public class Dlookup {

    public static void main(String[] args) {
        System.out.println("****************************************");
        System.out.println("invoking Dlookup with args:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println("****************************************");
        int port = -1;
        String home = null;
        String serializer = null;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("bad port number:" + args[0]);
                System.exit(1);
            }
            System.out.println("Set attic DL port to " + args[0]);
        }
        if (args.length > 1) {
            home = args[1];
        }
        Attic attic;
        if (home == null) {
            attic = new Attic();
        } else {
            attic = new Attic(home);
        }
        if (port > -1) {
            attic.setPort(port);
        }
        attic.getSecurityConfig().setSecure(false);
        attic.getSecurityConfig().setTestMode(true);

        System.out.println("ARGS=" + args.length);

        if(args.length == 3) {
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
        attic.addRole(StringConstants.ROLE_DL);
        attic.attach("attic", new ConfigServiceRole());

        DataLookup dl = new DataLookup();
        attic.attach(StringConstants.DATA_LOOKUP, dl);
        attic.init();

        if (attic.getSerializer() instanceof  XhtmlSerializer) {
            System.out.println("Using XHTML Serializer");
        } else if (attic.getSerializer() instanceof  XmlSerializer) {
            System.out.println("Using XML Serializer");
        } else if (attic.getSerializer() instanceof  JsonSerializer) {
            System.out.println("Using JSON Serializer");
        }


    }
}