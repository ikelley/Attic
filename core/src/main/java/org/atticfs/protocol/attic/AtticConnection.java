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

package org.atticfs.protocol.attic;

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.OutChannel;
import org.atticfs.download.request.RequestCollection;
import org.atticfs.download.request.RequestResolver;
import org.atticfs.stream.AtticInputStream;
import org.atticfs.stream.Streamer;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * URL connection for the attic protocol.
 * This supports two request properties that are propagated to an Attic instance's StreamConfig:
 * <p/>
 * 1. MAX_INMEMORY_BUFFER - maximum in memory buffer size used for verifying the stream data
 * 2. ATTEMPT_VERIFICATION - whether to attempt verification of data as it arrives.
 * This is dependent on the hashed chunk sizes being <= maximum buffer size.
 *
 * 
 */

public class
        AtticConnection extends URLConnection {

    public static final String MAX_INMEMORY_BUFFER = "org.atticfs.protocol.attic.max.inmemory.buffer";

    public static final String ATTEMPT_VERIFICATION = "org.atticfs.protocol.attic.attempt.verification";

    private AtticInputStream stream;
    private Attic attic;
    private Streamer streamer;

    public AtticConnection(URL url) {
        this(url, Attic.getDefaultAttic());
    }

    public AtticConnection(URL url, Attic attic) {
        super(url);
        this.attic = attic;

    }

    public void connect() throws IOException {
        try {


        if (!connected) {
            Endpoint ep = new Endpoint(url.toString());
            ep.setScheme(getHttpScheme());
            OutChannel out = attic.getChannelFactory().createOutChannel();
            ChannelData data = new ChannelData(ChannelData.Action.GET, ep);
            data.setResponseType(DataPointer.class);
            ChannelData resp = out.send(data);
            Object o = resp.getResponseData();
            if (resp.getOutcome() == ChannelData.Outcome.OK && o != null && o instanceof DataPointer) {
                RequestCollection coll = RequestResolver.createRequestCollection((DataPointer) resp.getResponseData(), attic);
                stream = new AtticInputStream(this.attic);
                streamer = new Streamer(coll, attic);
                stream.setSource(streamer);
                streamer.setSink(stream);
            } else {
                throw new IOException("Error retieving data pointer from " + ep);
            }
        }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    protected String getHttpScheme() {
        return "http";
    }

    public void setRequestProperty(String key, String value) {
        if (key.equals(MAX_INMEMORY_BUFFER)) {
            try {
                int buff = Integer.parseInt(value);
                attic.getStreamConfig().setMaxBufferSize(buff);
            } catch (NumberFormatException e) {

            }
        } else if (key.equals(ATTEMPT_VERIFICATION)) {
            boolean b = Boolean.parseBoolean(value);
            attic.getStreamConfig().setAttemptVerification(b);
        }
    }

    public InputStream getInputStream() throws IOException {
        connect();
        streamer.stream();
        return stream;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
