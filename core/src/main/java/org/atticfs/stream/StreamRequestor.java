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

package org.atticfs.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelFactory;
import org.atticfs.channel.OutChannel;
import org.atticfs.download.request.SegmentRequest;
import org.atticfs.download.table.DownloadTable;
import org.atticfs.stats.DownloadStats;
import org.atticfs.stats.EndpointStats;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileSegmentHash;
import org.atticfs.util.FileUtils;

/**
 * Class Description Here...
 *
 * 
 */

public class StreamRequestor implements Callable<Streamer.StreamFetchResult> {

    static Logger log = Logger.getLogger("org.atticfs.stream.StreamRequestor");

    protected DownloadTable table;
    protected DownloadStats stats;

    private int maxBufferSize;
    private boolean attemptVerification;


    public StreamRequestor(DownloadTable table, DownloadStats stats, boolean verify, int maxBuffer) {
        this.table = table;
        this.stats = stats;
        this.attemptVerification = verify;
        this.maxBufferSize = maxBuffer;
    }

    public Streamer.StreamFetchResult call() throws Exception {
        long id = -1;
        SegmentRequest request = table.next(id);

        if (request == null || request.getPriority() == DownloadTable.Priority.POISON) {
            log.fine("StreamRequestor.call null or poison - returning null");
            return null;
        }
        log.fine("StreamRequestor.call got next segment:" + request.getFileSegmentHash());
        DownloadStats.EndpointDownloadStats eps = stats.getEndpointStats(request.getEndpoint());
        eps.incTotalRequests();
        id = request.getEndpointRequestId();
        Endpoint target = request.getEndpoint();
        log.fine(" getting data from " + target);
        FileSegmentHash chunk = request.getFileSegmentHash();
        ChannelData cd = new ChannelData(ChannelData.Action.GET, target.toString());
        boolean doingVerify = false;
        if (chunk != null && attemptVerification && chunk.getSize() <= maxBufferSize) {
            doingVerify = true;
        }
        cd.setBufferSize(0);
        cd.setResponseType(InputStream.class);
        cd.setUseCompression(table.getDownloadConfig().isCompress());
        cd.setConnectionRetryCount(table.getDownloadConfig().getConnectionRetryCount());
        cd.setTimeout(table.getDownloadConfig().getSocketTimeout());
        OutChannel out = ChannelFactory.getFactory().createOutChannel(null);
        if (chunk == null) {
            try {
                cd = out.send(cd);
            } catch (IOException e1) {
                EndpointStats.getStats().addBadConnectionEndpoint(target);
            }
            stats.addChannelData(cd, eps);
            if (cd.getOutcome() == ChannelData.Outcome.OK) {
                return new Streamer.StreamFetchResult(request, (InputStream) cd.getResponseData(), true);
            } else {
                if (cd.getOutcome() == ChannelData.Outcome.CLIENT_ERROR || cd.getOutcome() == ChannelData.Outcome.SERVER_ERROR) {
                    EndpointStats.getStats().addBadMessageEndpoint(target);
                }
                return new Streamer.StreamFetchResult(request, null, false);
            }
        } else {
            cd.setByteRange(request.getByteRange());
            try {
                cd = out.send(cd);
            } catch (IOException e1) {
                EndpointStats.getStats().addBadConnectionEndpoint(target);
                return new Streamer.StreamFetchResult(request, null, false);
            }
            stats.addChannelData(cd, eps);
            if (cd.getOutcome() == ChannelData.Outcome.OK) {
                if (cd.getResponseData() instanceof InputStream) {
                    InputStream in = (InputStream) cd.getResponseData();
                    if (doingVerify) {
                        ByteArrayOutputStream bout = FileUtils.verify(in, chunk.getHash());
                        in.close();
                        if (bout == null) {
                            return new Streamer.StreamFetchResult(request, null, false);
                        } else {
                            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                            return new Streamer.StreamFetchResult(request, bin, true);
                        }
                    } else {
                        return new Streamer.StreamFetchResult(request, in, true);
                    }
                } else {
                    return new Streamer.StreamFetchResult(request, null, false);
                }

            } else {
                if (cd.getOutcome() == ChannelData.Outcome.CLIENT_ERROR || cd.getOutcome() == ChannelData.Outcome.SERVER_ERROR) {
                    EndpointStats.getStats().addBadMessageEndpoint(target);
                }
                return new Streamer.StreamFetchResult(request, null, false);
            }
        }

    }

}