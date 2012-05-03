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
import org.atticfs.impl.roles.dc.DataCenter;
import org.atticfs.impl.roles.main.ConfigServiceRole;
import org.atticfs.impl.ser.json.JsonSerializer;
import org.atticfs.impl.ser.xhtml.XhtmlSerializer;
import org.atticfs.impl.ser.xml.XmlSerializer;
import org.atticfs.types.Endpoint;
import org.atticfs.util.StringConstants;

/**
 * runs a data center. takes up to 4 arguments. The first three are required.
 * The fourth is optional
 * args:
 * 1. a data lookup endpoint, the default being http:<host>:<port>/dl/meta/pointer
 * 2. a local port number to run on
 * 3. a query interval in seconds
 * 4. Where to put the Attic home directory
 *
 * 
 */

public class Dcenter {

    public static void main(String[] args) {
        System.out.println("****************************************");
        System.out.println("invoking Dcenter with args:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println("****************************************");
        if (args.length < 3) {
            System.out.println("I need a DLS endpoint, a local port, a query interval");
            System.exit(0);
        }
        long queryInterval = -1;
        int port = -1;
        String bootstrap;
        String home = null;
        bootstrap = args[0];
        String serializer = null;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("bad port number:" + args[1]);
            System.exit(1);
        }
        try {
            queryInterval = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("bad query interval:" + args[2]);
            System.exit(1);
        }
        if (args.length == 4) {
            home = args[3];
        }

        Attic attic;
        if (home == null) {
            attic = new Attic();
        } else {
            attic = new Attic(home);
        }
        attic.getSecurityConfig().setSecure(false);
        attic.getSecurityConfig().setTestMode(true);

        if(args.length == 5) {
         serializer = args[4];
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
        attic.addRole(StringConstants.ROLE_DC);
        attic.attach("attic", new ConfigServiceRole());

        attic.getDataConfig().setDataQueryInterval(queryInterval);
        attic.setBootstrapEndpoint(bootstrap);
        attic.setPort(port);

        DataCenter dc = new DataCenter(new Endpoint(attic.getBootstrapEndpoint()));
        attic.attach(StringConstants.DATA_CENTER, dc);
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