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

package org.atticfs.impl.channel.http;

import java.io.IOException;
import java.io.InputStream;

import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelProperties;
import org.atticfs.channel.OutChannel;
import org.atticfs.protocol.AtticProtocol;
import org.atticfs.types.Endpoint;
import org.wspeer.http.ByteRange;
import org.wspeer.http.Http;
import org.wspeer.http.HttpPeer;
import org.wspeer.http.RequestContext;
import org.wspeer.http.Resource;
import org.wspeer.http.Response;
import org.wspeer.streamable.Streamable;

/**
 * Supported values for data are:
 * <p/>
 * 1. WireType
 * 2. byte[]
 * 3. InputStream
 * 4. Streamable
 * 5. Serializable
 * 6. File
 *
 * 
 */

public class HttpClient implements OutChannel {

    private HttpPeer peer;

    public HttpClient(HttpPeer peer) {
        this.peer = peer;
    }

    public void open(ChannelProperties properties) throws IOException {

    }

    public void open() throws IOException {
    }

    public ChannelData send(ChannelData context) throws Exception {

        ChannelData.Action action = context.getAction();
        Endpoint endpoint = context.getEndpoint();
        if (action == null) {
            throw new IOException("No action defined!");
        }
        if (endpoint == null) {
            throw new IOException("No endpoint defined!");
        }
        if (endpoint.getScheme().equalsIgnoreCase(AtticProtocol.SCHEME_ATTIC)) {
            endpoint.setScheme("http");
        } else if (endpoint.getScheme().equalsIgnoreCase(AtticProtocol.SCHEME_ATTICS)) {
            endpoint.setScheme("https");
        }
        RequestContext rc = new RequestContext(endpoint.toString());

        Object data = context.getRequestData();
        if (data != null) {
            Streamable s = DataHandler.getStreamableForData(data, context.getMimeType(), rc);
            if (s == null) {
                throw new IOException("Could not serialize data:" + data);
            }
            rc.setResource(new Resource(s));
        }
        if (context.getTimeout() > 0) {
            rc.setTimeout(context.getTimeout());
        }
        rc.setBufferSize(context.getBufferSize());
        if (context.getOutputFile() != null) {
            rc.setOutputFile(context.getOutputFile());
        }
        if (context.getResponseType() != null && InputStream.class.isAssignableFrom(context.getResponseType())) {
            rc.setDirectStream(true);
        }
        if (context.getByteRange() != null) {
            rc.setRequestRange(new ByteRange(context.getByteRange().getStartOffset(), context.getByteRange().getEndOffset()));
        }
        if (context.getAcceptedMimeTypes() != null) {
            rc.setAcceptedMedia(context.getAcceptedMimeTypes());
        }
        rc.setMaxRetries(context.getConnectionRetryCount());
        rc.setCompress(context.isUseCompression());
        rc.setKeepAlive(!context.isCloseOnFinish());
        Response response = invoke(rc, action);
        if (response == null) {
            throw new IOException("could not invoke endpoint:" + endpoint + " with action:" +
                    action + " and " + (data == null ? "no data" : "data:" + data));
        }
        long start = rc.getOutStartTime();
        long end = rc.getOutEndTime();
        if (start > 0 && end > 0) {
            context.setOutTime(end - start);
        }
        long start1 = rc.getInStartTime();
        long end1 = rc.getInEndTime();
        if (start1 > 0 && end1 > 0) {
            context.setInTime(end1 - start1);
        }
        context.setOutcome(getOutcomeForStatus(response));
        context.setOutcomeDetail(response.getOutcome());
        if (context.getOutcome() != ChannelData.Outcome.OK) {
            context.setResponseType(String.class);
        }
        if (response.getContext().getResponseHeader(Http.LOCATION) != null) {
            context.setLocation(response.getContext().getResponseHeader(Http.LOCATION));
        }
        Resource res = response.getResource();
        if (res.getByteRanges() != null && res.getByteRanges().size() == 1) {
            log.fine("HttpClient.send got return byte range: " + res.getByteRanges().get(0));
            ByteRange range = res.getByteRanges().get(0);
            org.atticfs.channel.ByteRange respRange = new org.atticfs.channel.ByteRange(range.from, range.to, range.total);
            context.setByteRange(respRange);
        }
        if (res != null) {
            Streamable respData = res.getStreamable();
            if (respData != null) {
                context.setResponseData(DataHandler.getResponseObject(context.getResponseType(), respData));
            }
        }
        if (rc.getOutDataLength() > 0) {
            context.setBytesSent(rc.getOutDataLength());
        }
        if (rc.getInDataLength() > 0) {
            context.setBytesReceived(rc.getInDataLength());
        }
        return context;
    }

    private ChannelData.Outcome getOutcomeForStatus(Response response) {
        Http.StatusType status = response.getOutcomeType();
        if (status == Http.StatusType.SUCCESS) {
            return ChannelData.Outcome.OK;
        } else if (status == Http.StatusType.CLIENT_ERROR) {
            return ChannelData.Outcome.CLIENT_ERROR;
        } else if (status == Http.StatusType.SERVER_ERROR) {
            return ChannelData.Outcome.SERVER_ERROR;
        } else if (status == Http.StatusType.EXCEPTION) {
            return ChannelData.Outcome.CLIENT_ERROR;
        } else if (status == Http.StatusType.UNKNOWN) {
            return ChannelData.Outcome.UNKNOWN;
        }
        return ChannelData.Outcome.UNKNOWN;
    }

    private Response invoke(RequestContext rc, ChannelData.Action action) throws IOException {
        if (action == ChannelData.Action.UPDATE) {
            return peer.put(rc);
        } else if (action == ChannelData.Action.CREATE ||
                action == ChannelData.Action.MESSAGE) {
            return peer.post(rc);
        } else if (action == ChannelData.Action.GET) {
            return peer.get(rc);
        } else if (action == ChannelData.Action.DELETE) {
            return peer.delete(rc);
        }
        return null;
    }


    public void close() throws IOException {
        peer.close();
    }


}
