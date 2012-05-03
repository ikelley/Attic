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

package org.atticfs.download.request;

import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.atticfs.channel.ByteRange;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelFactory;
import org.atticfs.channel.OutChannel;
import org.atticfs.download.Downloader;
import org.atticfs.download.table.DownloadTable;
import org.atticfs.stats.DownloadStats;
import org.atticfs.stats.EndpointStats;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileSegmentHash;
import org.wspeer.streamable.RebuiltStreamable;
import org.wspeer.streamable.StreamableStream;

/**
 * Class Description Here...
 *
 * 
 */

public class SingleFileRequestor extends MultipleFileRequestor {

    private RebuiltStreamable rebuild;

    public SingleFileRequestor(DownloadTable table, Downloader downloader) {
        super(table, downloader);
    }

    protected void chunkedDownload(SegmentRequest request, List<Downloader.FetchResult> results) throws IOException {

        rebuild = new RebuiltStreamable(downloader.getTargetFile(), "application/octet-stream");
        Endpoint target = request.getEndpoint();

        OutChannel out = ChannelFactory.getFactory().createOutChannel();
        ChannelData cd = new ChannelData(ChannelData.Action.GET, target.toString());
        cd.setBufferSize(0);
        cd.setResponseType(InputStream.class);
        cd.setUseCompression(table.getDownloadConfig().isCompress());
        cd.setConnectionRetryCount(table.getDownloadConfig().getConnectionRetryCount());
        cd.setTimeout(table.getDownloadConfig().getSocketTimeout());
        boolean downloaded = request(request, cd, out, false);
        if (downloaded) {
            Downloader.FetchResult res = verify(request);
            if (res != null) {
                table.onSuccess(request);
                results.add(res);
            } else {
                if (request(request, cd, out, true)) {
                    res = verify(request);
                    if (res != null) {
                        table.onSuccess(request);
                        results.add(res);
                    } else {
                        table.onFailure(request);
                    }
                } else {
                    table.onFailure(request);
                }
            }
        } else {
            // here we leave the files lurking.
            table.onFailure(request);
        }
    }

    protected boolean request(SegmentRequest request, ChannelData cd, OutChannel out, boolean retry) throws IOException {
        int failCount = 0;
        List<Chunk> chunks = request.getDownloadChunks();
        Endpoint target = request.getEndpoint();
        DownloadStats.EndpointDownloadStats eps = downloader.getStats().getEndpointStats(request.getEndpoint());
        boolean doneAnything = false;
        for (Chunk downloadChunk : chunks) {
            // todo - what about reties on the same endpoint?
            if (downloadChunk.getState() != Chunk.State.UNTRIED) {
                continue;
            }
            cd.setByteRange(downloadChunk.getByteRange());
            boolean hasChunk = rebuild.hasFragment(downloadChunk.getStartOffset(), downloadChunk.getEndOffset());
            if (hasChunk) {
                if (retry) {
                    rebuild.invalidateFragment(downloadChunk.getStartOffset());
                } else {
                    continue;
                }
            }
            doneAnything = true;
            eps.incTotalRequests();
            try {
                cd = out.send(cd);
            } catch (Exception e1) {
                EndpointStats.getStats().addBadConnectionEndpoint(target);
            }
            downloader.getStats().addChannelData(cd, eps);
            if (cd.getOutcome() != ChannelData.Outcome.OK) {
                if (cd.getOutcome() == ChannelData.Outcome.CLIENT_ERROR || cd.getOutcome() == ChannelData.Outcome.SERVER_ERROR) {
                    EndpointStats.getStats().addBadMessageEndpoint(target);
                }
                downloadChunk.setState(Chunk.State.FAILED);
                if (cd.getResponseData() instanceof InputStream) {
                    ((InputStream) cd.getResponseData()).close();
                }
                failCount++;
            } else {
                if (cd.getResponseData() instanceof InputStream) {
                    ByteRange received = cd.getByteRange();
                    if (!received.equals(downloadChunk.getByteRange())) {
                        downloadChunk.setState(Chunk.State.FAILED);
                        failCount++;
                        ((InputStream) cd.getResponseData()).close();
                    } else {
                        StreamableStream ss = new StreamableStream((InputStream) cd.getResponseData());
                        long now = System.currentTimeMillis();
                        long len = rebuild.addFragment(ss, downloadChunk.getStartOffset(), rebuild.getLength());
                        eps.incTotalData(len);
                        eps.incTotalTime(System.currentTimeMillis() - now);
                        ss.getInputStream().close();
                        downloadChunk.setState(Chunk.State.UNVERIFIED);
                    }
                } else {
                    failCount++;
                }
            }
            // get out of here - too many bad chunks
            // todo - configuration option
            if (failCount > 3) {
                break;
            }
        }
        return failCount == 0 && doneAnything;
    }

    protected Downloader.FetchResult verify(SegmentRequest request) throws IOException {
        FileSegmentHash chunk = request.getFileSegmentHash();
        FileChannel channel = rebuild.getChannel();
        MappedByteBuffer buffer =
                channel.map(FileChannel.MapMode.READ_ONLY, chunk.getStartOffset(), chunk.getEndOffset() - chunk.getStartOffset() + 1);
        boolean b = downloader.verify(buffer, chunk.getHash());

        if (b) {
            Downloader.FetchResult res = new Downloader.FetchResult(rebuild, table.getDescription(), request.getEndpoint(), chunk);
            res.setByteRange(request.getByteRange());
            return res;
        } else {
            rebuild.invalidateFragment(chunk.getStartOffset());
            EndpointStats.getStats().addBadHashEndpoint(request.getEndpoint());
        }

        return null;
    }

}