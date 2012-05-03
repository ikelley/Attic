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

import org.atticfs.Attic;
import org.atticfs.impl.ser.json.JsonSerializer;
import org.atticfs.impl.ser.xml.XmlSerializer;
import org.atticfs.ser.SerializerFactory;
import org.atticfs.types.*;

import java.io.*;

/**
 * Class Description Here...
 *
 * 
 */

public class TypeMaker {

    public static final File REAL_FILE_LOCATION = new File(new Attic().getDPDataHome(), "01Intro.mp3");

    public static final String descriptionId = "6da68159-5245-4adf-bc90-ccb74f713dec";
    public static final String xmlDataDescription =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<DataDescription xmlns=\"http://atticfs.org\">\n" +
                    "<id>6da68159-5245-4adf-bc90-ccb74f713dec</id>\n" +
                    "<name>01Intro.mp3</name>\n" +
                    "<description></description>\n" +
                    "<FileHash>\n" +
                    "<hash>d6a5f4aae746e18c92f18eaba9d77c61</hash>\n" +
                    "<size>6435839</size>\n" +
                    "<Segment>\n" +
                    "<hash>44bc74bb4d6225b8b8e68ea6848a57</hash>\n" +
                    "<start>0</start>\n" +
                    "\n" +
                    "<end>524287</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>bf5b8da3143d769241b21675347691</hash>\n" +
                    "<start>524288</start>\n" +
                    "<end>1048575</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>e69da54b2e42c423364640ad490dd4</hash>\n" +
                    "<start>1048576</start>\n" +
                    "<end>1572863</end>\n" +
                    "\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>932ae49ca65f7acc4291272d9fa7f</hash>\n" +
                    "<start>1572864</start>\n" +
                    "<end>2097151</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>265676af5bc512534ca078fa5e3ea</hash>\n" +
                    "<start>2097152</start>\n" +
                    "<end>2621439</end>\n" +
                    "</Segment>\n" +
                    "\n" +
                    "<Segment>\n" +
                    "<hash>6637de8cc5c36ca6145a6dcee188187</hash>\n" +
                    "<start>2621440</start>\n" +
                    "<end>3145727</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>f2e8ded158cf74e8287113f7fb137050</hash>\n" +
                    "<start>3145728</start>\n" +
                    "<end>3670015</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "\n" +
                    "<hash>0e7e4fea382bcb3e0e5e7ff9c2803f</hash>\n" +
                    "<start>3670016</start>\n" +
                    "<end>4194303</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>2e7b41effd21d5dd44b34fa04765cfb6</hash>\n" +
                    "<start>4194304</start>\n" +
                    "<end>4718591</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>b7ab2168e07738af8ca82d7bf76f866b</hash>\n" +
                    "\n" +
                    "<start>4718592</start>\n" +
                    "<end>5242879</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>f35bfa72f7e22985bf161fe6bd16750</hash>\n" +
                    "<start>5242880</start>\n" +
                    "<end>5767167</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>cdabbd2444a8b3c182a69528cb119c1</hash>\n" +
                    "<start>5767168</start>\n" +
                    "\n" +
                    "<end>6291455</end>\n" +
                    "</Segment>\n" +
                    "<Segment>\n" +
                    "<hash>1e6fe5fa73723a1cc3b02f8b5a3cc3d5</hash>\n" +
                    "<start>6291456</start>\n" +
                    "<end>6435838</end>\n" +
                    "</Segment>\n" +
                    "</FileHash>\n" +
                    "</DataDescription>";


    public static final String jsonDataDescription = "{\n" +
            "    \"DataDescription\":     {\n" +
            "        \"id\": \"6da68159-5245-4adf-bc90-ccb74f713dec\",\n" +
            "        \"name\": \"01Intro.mp3\",\n" +
            "        \"description\": \"\",\n" +
            "        \"location\": null,\n" +
            "        \"FileHash\":         {\n" +
            "            \"hash\": \"d6a5f4aae746e18c92f18eaba9d77c61\",\n" +
            "            \"size\": 6435839,\n" +
            "            \"Segment\": [\n" +
            "                {\n" +
            "                    \"hash\": \"44bc74bb4d6225b8b8e68ea6848a57\",\n" +
            "                    \"start\": 0,\n" +
            "                    \"end\": 524287\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"bf5b8da3143d769241b21675347691\",\n" +
            "                    \"start\": 524288,\n" +
            "                    \"end\": 1048575\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"e69da54b2e42c423364640ad490dd4\",\n" +
            "                    \"start\": 1048576,\n" +
            "                    \"end\": 1572863\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"932ae49ca65f7acc4291272d9fa7f\",\n" +
            "                    \"start\": 1572864,\n" +
            "                    \"end\": 2097151\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"265676af5bc512534ca078fa5e3ea\",\n" +
            "                    \"start\": 2097152,\n" +
            "                    \"end\": 2621439\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"6637de8cc5c36ca6145a6dcee188187\",\n" +
            "                    \"start\": 2621440,\n" +
            "                    \"end\": 3145727\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"f2e8ded158cf74e8287113f7fb137050\",\n" +
            "                    \"start\": 3145728,\n" +
            "                    \"end\": 3670015\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"0e7e4fea382bcb3e0e5e7ff9c2803f\",\n" +
            "                    \"start\": 3670016,\n" +
            "                    \"end\": 4194303\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"2e7b41effd21d5dd44b34fa04765cfb6\",\n" +
            "                    \"start\": 4194304,\n" +
            "                    \"end\": 4718591\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"b7ab2168e07738af8ca82d7bf76f866b\",\n" +
            "                    \"start\": 4718592,\n" +
            "                    \"end\": 5242879\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"f35bfa72f7e22985bf161fe6bd16750\",\n" +
            "                    \"start\": 5242880,\n" +
            "                    \"end\": 5767167\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"cdabbd2444a8b3c182a69528cb119c1\",\n" +
            "                    \"start\": 5767168,\n" +
            "                    \"end\": 6291455\n" +
            "                },\n" +
            "                {\n" +
            "                    \"hash\": \"1e6fe5fa73723a1cc3b02f8b5a3cc3d5\",\n" +
            "                    \"start\": 6291456,\n" +
            "                    \"end\": 6435838\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";


    public static final String xmlAdvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<DataAdvert xmlns=\"http://atticfs.org\">\n" +
            "    <DataDescription>\n" +
            "        <name>no name</name>\n" +
            "        <description>no description</description>\n" +
            "        <project>no project</project>\n" +
            "        <FileHash>\n" +
            "            <size>6435839</size>\n" +
            "            <hash>d6a5f4aae746e18c92f18eaba9d77c61</hash>\n" +
            "        </FileHash>\n" +
            "    </DataDescription>\n" +
            "    <Constraints>\n" +
            "        <Constraint type=\"Date\">\n" +
            "            <key>expires</key>\n" +
            "            <value>Thu, 01 Jan 1970 00:00:01 GMT</value>\n" +
            "        </Constraint>\n" +
            "        <Constraint type=\"Integer\">\n" +
            "            <key>replica</key>\n" +
            "            <value>2147483647</value>\n" +
            "        </Constraint>\n" +
            "    </Constraints>\n" +
            "</DataAdvert>";

    public static DataDescription createDataDescription(String id) {
        DataDescription dd = new DataDescription(id, id + "name");
        dd.setDescription("This is a data description");
        return dd;
    }

    public static DataPointer createDataPointer(String id) {
        DataDescription dd = createDataDescription(id);
        FileHash fh = createFileHash(id);
        dd.setHash(fh);
        DataPointer pointer = new DataPointer(dd);
        for (int i = 0; i < 5; i++) {
            pointer.addEndpoint(createEndpoint());
        }
        return pointer;
    }

    public static FileHash createFileHash(String id) {
        FileHash fh = new FileHash();
        fh.setSize(100);
        fh.setHash("hash" + id);
        for (int i = 0; i < 10; i++) {
            int offset = i * 10;
            FileSegmentHash seg = new FileSegmentHash("hash" + id + i, offset, offset + 9);
            fh.addSegment(seg);
        }
        return fh;
    }

    public static DataAdvert createDataAdvert(String id) {
        DataAdvert da = new DataAdvert(getXmlDataDescription(), createEndpoint());
        da.getConstraints().addConstraint(new Constraint(DataAdvert.REPLICA, 3));
        da.getConstraints().addConstraint(new Constraint(DataAdvert.EXPIRY, System.currentTimeMillis() + (1000 * 60 * 5)));
        da.getDataDescription().setId(id);
        return da;
    }

    public static DataAdvert createDataAdvert() {
        DataAdvert da = new DataAdvert(getXmlDataDescription(), createEndpoint());
        da.getConstraints().addConstraint(new Constraint(DataAdvert.REPLICA, 3));
        da.getConstraints().addConstraint(new Constraint(DataAdvert.EXPIRY, System.currentTimeMillis() + (1000 * 60 * 5)));
        return da;
    }

    private static int count = 0;

    public static Endpoint createEndpoint() {
        Endpoint ep = new Endpoint("http://foobar" + (count++) + ".org");
        ep.setMetaEndpoint("http://foobar.meta.org");
        return ep;
    }

    public static DataDescription getXmlDataDescription() {
        try {
            return (DataDescription) new XmlSerializer().fromStream(new ByteArrayInputStream(xmlDataDescription.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataAdvert getXmlDataAdvert() {
        try {
            return (DataAdvert) new XmlSerializer().fromStream(new ByteArrayInputStream(xmlAdvert.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataDescription getJsonDataDescription() {
        try {
            return (DataDescription) new JsonSerializer().fromStream(new ByteArrayInputStream(jsonDataDescription.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataPointer getIntroPointer() {
        DataPointer dp = new DataPointer(getJsonDataDescription());

        dp.addEndpoint("http://localhost:7070/dp/data/" + dp.getDataDescription().getId(), "http://localhost:7070/dp/meta");
        dp.addEndpoint("http://0.0.0.0:8181/dc/data/" + dp.getDataDescription().getId(), "http://0.0.0.0:8181/dc/meta");
        dp.addEndpoint("http://0.0.0.0:8282/dc/data/" + dp.getDataDescription().getId(), "http://0.0.0.0:8282/dc/meta");
        return dp;
    }

    public static DataPointer readPointer() {
        try {
            InputStream in = new FileInputStream("/Users/scmabh/work/maven/attic/data/pointer.txt");
            if (in == null) {
                System.out.println("TypeMaker.readPointer could not read in file!!!!!!!!!!!");
            }
            return (DataPointer) SerializerFactory.getSerializer("org.atticfs.impl.ser.json.JsonSerializer").fromStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void main(String[] args) {
        DataAdvert da = getXmlDataAdvert();
        System.out.println("TypeMaker.main advert:" + da);

    }
}
