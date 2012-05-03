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

import org.atticfs.channel.*;

import java.io.IOException;

/**
 * Class Description Here...
 *
 * 
 */

public class HttpTestServer {

    public static void main(String[] args) throws IOException {
        ChannelProperties props = new ChannelProperties();
        EchoHandler handler = new EchoHandler();
        props.setServerContext(handler.getPath());
        props.setLocalPort(9000);
        InChannel in = ChannelFactory.getFactory().createInChannel(handler, props);
    }

    private static class EchoHandler implements ChannelRequestHandler {

        public ChannelData handleRequest(ChannelData context) {
            System.out.println("HttpTestClient$Handler.handleRequest context action:" + context.getAction());
            System.out.println("HttpTestClient$Handler.handleRequest context target:" + context.getTarget());
            System.out.println("HttpTestClient$Handler.handleRequest context data:" + context.getRequestData());
            context.setResponseData(context.getRequestData());
            context.setOutcome(ChannelData.Outcome.OK);
            context.setOutcomeDetail("all is good.");
            return context;
        }

        public String getPath() {
            return "simpleServer";
        }
    }
}
