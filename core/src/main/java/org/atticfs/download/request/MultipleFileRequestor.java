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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.atticfs.channel.ByteRange;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelFactory;
import org.atticfs.channel.OutChannel;
import org.atticfs.download.Downloader;
import org.atticfs.download.table.DownloadTable;
import org.atticfs.stats.DownloadStats;
import org.atticfs.stats.EndpointStats;
import org.atticfs.types.DataDescription;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileSegmentHash;
import org.atticfs.util.StringConstants;
import org.wspeer.streamable.RebuiltStreamable;
import org.wspeer.streamable.StreamableFile;

/**
 * Class Description Here...
 *
 * 
 */

public class MultipleFileRequestor extends AbstractRequestor {

    public MultipleFileRequestor(DownloadTable table, Downloader downloader) {
        super(table, downloader);
    }

    public List<Downloader.FetchResult> call() throws Exception {
        List<Downloader.FetchResult> results = new ArrayList<Downloader.FetchResult>();
        long id = -1;
        while (true) {
            SegmentRequest request = table.next(id);
            if (request == null || request.getPriority() == DownloadTable.Priority.POISON) {
                break;
            }
            id = request.getEndpointRequestId();
            FileSegmentHash chunk = request.getFileSegmentHash();
            if (chunk == null) {
                singleDownload(request, results);
            } else {
                chunkedDownload(request, results);
            }
        }
        return results;
    }

    protected void chunkedDownload(SegmentRequest request, List<Downloader.FetchResult> results) throws IOException {

        Endpoint target = request.getEndpoint();
        FileSegmentHash chunk = request.getFileSegmentHash();

        OutChannel out = ChannelFactory.getFactory().createOutChannel();
        ChannelData cd = new ChannelData(ChannelData.Action.GET, target.toString());
        cd.setBufferSize(0);
        cd.setResponseType(File.class);
        cd.setUseCompression(table.getDownloadConfig().isCompress());
        cd.setConnectionRetryCount(table.getDownloadConfig().getConnectionRetryCount());
        cd.setTimeout(table.getDownloadConfig().getSocketTimeout());
        // create a directory into which to put the chunks
        File chunkDownloadDir = new File(downloader.getDownloadDir(), chunk.getStartOffset() + "");
        chunkDownloadDir.mkdirs();

        /*
        if there is an error in download, the download file is deleted.
        After download, only those files that were successfully downloaded remain.
        But they are still unverified.
        If all files download ok, then verification starts.
        If verification fails, on failure is called and the result file is deleted. The chunk files remain
        If verification succeeds, the chunk directory is deleted and onSuccess is called.
        If NOT all chunks are download, then the chunk files remain to be reused in another life.
        Hence, this requestor checks to see if a file for a chunk exists of the excepted length
        and if it does, it won't download it.
        If this verification fails, a retry is attempted overwriting the chunks that were downloaded
        from a previous request. If this verification fails, everything is deleted because we've
        had one endpoint fail on up and another returning bad chunks. So we start afresh.
         */
        boolean downloaded = request(request, cd, out, chunkDownloadDir, false);
        if (downloaded) {
            Downloader.FetchResult res = verify(request, chunkDownloadDir);
            if (res != null) {
                table.onSuccess(request);
                results.add(res);
            } else {
                if (request(request, cd, out, chunkDownloadDir, true)) {
                    res = verify(request, chunkDownloadDir);
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

    protected Downloader.FetchResult verify(SegmentRequest request, File chunkDownloadDir) throws IOException {
        FileSegmentHash chunk = request.getFileSegmentHash();
        List<Chunk> chunks = request.getDownloadChunks();

        File finalFile = new File(downloader.getDownloadDir(), chunk.getStartOffset() + StringConstants.EXT_DATA);
        RebuiltStreamable s = new RebuiltStreamable(finalFile, "application/octet-stream");
        for (Chunk downloadChunk : chunks) {
            File chunkFile = new File(chunkDownloadDir, downloadChunk.getStartOffset() + ".chunk");
            StreamableFile sf = new StreamableFile(chunkFile);
            try {
                s.addFragment(sf, downloadChunk.getStartOffset() - chunk.getStartOffset(), s.getLength());
            } catch (IOException e) {
                // fall through;
            }
        }
        log.fine("Downloader.postProcess status of data: " + s.getRebuildStatus());
        if (s.getRebuildStatus() == RebuiltStreamable.RebuildStatus.COMPLETE) {
            for (Chunk downloadChunk : chunks) {
                downloadChunk.setState(Chunk.State.VERIFIED);
            }
            FileChannel channel = new FileInputStream(finalFile).getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, finalFile.length());
            boolean b = downloader.verify(buffer, chunk.getHash());
            if (b) {
                Downloader.FetchResult res = new Downloader.FetchResult(finalFile, table.getDescription(), request.getEndpoint(), chunk);
                res.setByteRange(request.getByteRange());
                return res;
            } else {
                finalFile.delete();
                EndpointStats.getStats().addBadHashEndpoint(request.getEndpoint());
            }
        }
        return null;
    }

    protected boolean request(SegmentRequest request,
                              ChannelData cd,
                              OutChannel out,
                              File chunkDownloadDir,
                              boolean retry) {
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
            File downloadFile = new File(chunkDownloadDir, downloadChunk.getStartOffset() + ".chunk");
            if (downloadFile.exists()) {
                if (retry) {
                    downloadFile.delete();
                } else {
                    if (downloadFile.length() == (downloadChunk.getEndOffset() - downloadChunk.getStartOffset())) {
                        continue;
                    } else {
                        downloadFile.delete();
                    }
                }
            }
            doneAnything = true;
            cd.setOutputFile(downloadFile);
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
                downloadFile.delete();
                failCount++;
            } else {
                ByteRange received = cd.getByteRange();
                if (!received.equals(downloadChunk.getByteRange())) {
                    downloadChunk.setState(Chunk.State.FAILED);
                    failCount++;
                    downloadFile.delete();
                } else {
                    downloadChunk.setState(Chunk.State.UNVERIFIED);
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

    protected void singleDownload(SegmentRequest request, List<Downloader.FetchResult> results) throws IOException {

        Endpoint target = request.getEndpoint();

        DownloadStats.EndpointDownloadStats eps = downloader.getStats().getEndpointStats(request.getEndpoint());
        eps.incTotalRequests();

        OutChannel out = ChannelFactory.getFactory().createOutChannel(null);
        ChannelData cd = new ChannelData(ChannelData.Action.GET, target.toString());
        cd.setBufferSize(0);
        cd.setResponseType(File.class);
        cd.setUseCompression(table.getDownloadConfig().isCompress());
        cd.setConnectionRetryCount(table.getDownloadConfig().getConnectionRetryCount());
        cd.setTimeout(table.getDownloadConfig().getSocketTimeout());
        File f = downloader.getTargetFile();
        cd.setOutputFile(f);

        try {
            cd = out.send(cd);
        } catch (Exception e1) {
            EndpointStats.getStats().addBadConnectionEndpoint(target);
        }

        downloader.getStats().addChannelData(cd, eps);

        DataDescription dd = table.getDescription();
        if (cd.getOutcome() == ChannelData.Outcome.OK) {
            Downloader.FetchResult res;
            if (cd.getResponseData() instanceof File) {
                File ret = (File) cd.getResponseData();
                FileChannel channel = new FileInputStream(ret).getChannel();
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, ret.length());
                Boolean b = downloader.verify(buffer, dd.getHash().getHash());
                if (b == null) {
                    table.onFailure(request);
                } else if (b) {
                    res = new Downloader.FetchResult(cd.getResponseData(), dd, target);
                    table.onSuccess(request);
                    results.add(res);
                } else {
                    table.onFailure(request);
                    EndpointStats.getStats().addBadHashEndpoint(target);
                }
            } else {
                table.onFailure(request);
            }
        } else {
            if (cd.getOutcome() == ChannelData.Outcome.CLIENT_ERROR || cd.getOutcome() == ChannelData.Outcome.SERVER_ERROR) {
                EndpointStats.getStats().addBadMessageEndpoint(target);
            }
            table.onFailure(request);
        }
    }


}
