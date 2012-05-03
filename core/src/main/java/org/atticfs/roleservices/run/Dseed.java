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
import org.atticfs.impl.roles.dp.DataSeed;
import org.atticfs.impl.roles.main.ConfigServiceRole;
import org.atticfs.impl.ser.json.JsonSerializer;
import org.atticfs.impl.ser.xhtml.XhtmlSerializer;
import org.atticfs.impl.ser.xml.XmlSerializer;
import org.atticfs.store.index.Index;
import org.atticfs.types.Endpoint;
import org.atticfs.util.StringConstants;

/**
 * Runs a data seed. Takes up to 3 arguments. The first is required. The other two are optional
 * args:
 * 1. a data lookup endpoint, the default being http:<host>:<port>/dl/meta/pointer
 * 2. a local port number to run on
 * 3. a directory of where to put the Attic home directory
 *
 * 
 */

public class Dseed {

    public static void main(String[] args) {
        System.out.println("****************************************");
        System.out.println("invoking Dseed with args:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println("****************************************");
        if (args.length == 0) {
            System.out.println("I need a DLS endpoint...");
            System.exit(0);
        }
        int port = -1;
        String bootstrap = args[0];
        String home = null;
        String serializer = null;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
                System.out.println("Set attic DP port to " + port);

            } catch (NumberFormatException e) {
                System.out.println("bad port number:" + args[1]);
                System.exit(1);
            }
        }
        if (args.length > 2) {
            home = args[2];
        }

        Attic attic;
        if (home == null) {
            attic = new Attic();
        } else {
            attic = new Attic(home);
        }
        attic.setBootstrapEndpoint(bootstrap);
        if (port > -1) {
            attic.setPort(port);
        }
        attic.getSecurityConfig().setSecure(false);
        attic.getSecurityConfig().setTestMode(true);

        System.out.println("ARGS=" + args.length);

        if(args.length == 4) {
         serializer = args[3];
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
        attic.addRole(StringConstants.ROLE_DS);
        attic.attach("attic", new ConfigServiceRole());
        Endpoint ep = new Endpoint(attic.getBootstrapEndpoint());
        DataSeed dataSeed = new DataSeed(ep);
        attic.attach(StringConstants.DATA_PUBLISHER, dataSeed);
        attic.setProperty(Index.INDEX, "false");
        attic.init();
        attic.getDataConfig().setMoveIndexedFile(true);

        if (attic.getSerializer() instanceof  XhtmlSerializer) {
            System.out.println("Using XHTML Serializer");
        } else if (attic.getSerializer() instanceof  XmlSerializer) {
            System.out.println("Using XML Serializer");
        } else if (attic.getSerializer() instanceof  JsonSerializer) {
            System.out.println("Using JSON Serializer");
        }


    }
}