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

package org.atticfs.roleservices.download;


import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelFactory;
import org.atticfs.channel.ChannelProperties;
import org.atticfs.channel.OutChannel;
import org.atticfs.types.*;

import java.io.IOException;

/**
 * Class Description Here...
 *
 * 
 */

public class HttpTestClient {


    public static void main(String[] args) throws Exception {
        new HttpTestClient().run();
    }

    public HttpTestClient() {


    }

    public void run() throws Exception {

        OutChannel out = ChannelFactory.getFactory().createOutChannel(new ChannelProperties());
        ChannelData cd = new ChannelData(ChannelData.Action.MESSAGE, "http://localhost:9000/simpleServer");
        cd.setRequestData(createPointer());
        cd.setResponseType(DataPointer.class);
        cd = out.send(cd);
        System.out.println("HttpTestClient$Handler.handleRequest context action:" + cd.getAction());
        System.out.println("HttpTestClient$Handler.handleRequest context outcome:" + cd.getOutcome());
        System.out.println("HttpTestClient$Handler.handleRequest context data:" + cd.getResponseData());
        cd = new ChannelData(ChannelData.Action.CREATE, "http://localhost:9000/simpleServer");
        cd.setRequestData(createBytes());
        cd.setResponseType(byte[].class);
        cd = out.send(cd);
        System.out.println("HttpTestClient$Handler.handleRequest context action:" + cd.getAction());
        System.out.println("HttpTestClient$Handler.handleRequest context outcome:" + cd.getOutcome());
        System.out.println("HttpTestClient$Handler.handleRequest context data:" + cd.getResponseData());


    }

    private byte[] createBytes() {
        StringBuilder sb = new StringBuilder("Test string:");
        for (int i = 0; i < 1000; i++) {
            sb.append("some content ").append(i).append("\n");
        }
        return sb.toString().getBytes();
    }


    private DataPointer createPointer() {
        DataDescription desc = new DataDescription("1234");
        desc.setName("desc name");
        desc.setDescription("a short description of data");
        FileHash fh = new FileHash();
        fh.setHash("fh-hash");
        fh.setSize(100);

        for (int i = 0; i < 10; i++) {
            long offset = i * 10;
            FileSegmentHash fsh = new FileSegmentHash("hash" + 1, offset, offset + 9);
            fh.addSegment(fsh);
        }
        desc.setHash(fh);

        DataPointer dp = new DataPointer();
        dp.setDataDescription(desc);
        dp.addEndpoint(new Endpoint("http://198.162.0.1:8080"));
        dp.addEndpoint(new Endpoint("http://198.162.0.2:9090"));
        return dp;
    }
}
