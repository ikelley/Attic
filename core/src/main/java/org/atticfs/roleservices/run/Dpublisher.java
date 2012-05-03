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

import java.io.File;
import java.util.UUID;

import org.atticfs.Attic;
import org.atticfs.impl.roles.dp.DataPublisher;
import org.atticfs.impl.roles.main.ConfigServiceRole;
import org.atticfs.impl.ser.json.JsonSerializer;
import org.atticfs.impl.ser.xhtml.XhtmlSerializer;
import org.atticfs.impl.ser.xml.XmlSerializer;
import org.atticfs.types.DataDescription;
import org.atticfs.types.Endpoint;
import org.atticfs.util.StringConstants;

/**
 * Runs a data publisher. Takes up to 4 arguments. The first is required. The other three are optional
 * args:
 * 1. a data lookup endpoint, the default being http:<host>:<port>/dl/meta/pointer
 * 2. a local port number to run on
 * 3. a directory of where to put the Attic home directory
 * 4. path to a local file to publish
 * <p/>
 * <p/>
 * This creates a dummy DataDescription and then publishes the file.
 * <p/>
 * NOTE: geting the publisher to index a file like this is typically not recommended. It means
 * the file will be renamed, and will not be in the default location for indexed files.
 * <p/>
 * A better way of indexing files is to drop them into the default location first. This is
 * <attic-home>/dp/data
 * <p/>
 * This means on startup again, the file can be mapped back to the data description that has been
 * written (if writing out data descriptions to file is supported through the data config:
 * attic.getDataConfig().isWriteDescriptionsToDisk()
 *
 * 
 */

public class Dpublisher {

    public static void main(String[] args) {
        System.out.println("****************************************");
        System.out.println("invoking Dpublisher with args:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println("****************************************");
        if (args.length < 1) {
            System.out.println("I need a DLS endpoint");
            System.exit(0);
        }
        int port = -1;
        String bootstrap = args[0];
        String uid = UUID.randomUUID().toString();
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


       if(args.length > 3) {
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


        File file = null;
        if (args.length > 4) {
            String f = args[4];
            file = new File(f);
            if (!file.exists() || file.length() == 0) {
                System.out.println("The file you specified does not exist..");
                System.exit(0);
            }
        }
        if (args.length > 5) {
            uid = args[5];
        }


        attic.cleanRoles();
        attic.addRole(StringConstants.ROLE_DP);
        attic.attach("attic", new ConfigServiceRole());

        Endpoint ep = new Endpoint(attic.getBootstrapEndpoint());
        DataPublisher publisher = new DataPublisher(ep);

        attic.attach(StringConstants.DATA_PUBLISHER, publisher);

        //attic.setProperty(Index.INDEX, "false");
        attic.init();
        DataDescription dummy = new DataDescription(uid);
        dummy.setDescription("A roleservices data description");
        dummy.setProject("Test Project");
        if (file != null) {
            publisher.index(dummy, file);
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