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

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelFactory;
import org.atticfs.channel.OutChannel;
import org.atticfs.stats.EndpointStats;
import org.atticfs.types.DataDescription;
import org.atticfs.types.DataPointer;
import org.atticfs.types.Endpoint;
import org.atticfs.types.FileHash;
import org.atticfs.util.StringConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class RequestResolver {

    static Logger log = Logger.getLogger("org.atticfs.download.request.RequestResolver");


    public static RequestCollection createRequestCollection(DataPointer pointer, Attic attic) throws IOException {
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();

        CompletionService<Boolean> ecs = new ExecutorCompletionService<Boolean>(attic.getExecutor());
        final DataDescription dd = pointer.getDataDescription();
        if (dd == null) {
            throw new IOException("No data description in the data pointer.");
        }
        final FileHash fh = dd.getHash();
        if (fh == null) {
            throw new IOException("No file hash in data description. I need the hash of the file from this.");
        }
        if (fh.getHash() == null) {
            throw new IOException("No hash in file hash. I need this.");
        }
        final RequestCollection coll = new RequestCollection(dd);
        List<Endpoint> endpoints = pointer.getEndpoints();
        final Queue<Endpoint> queue = new ConcurrentLinkedQueue<Endpoint>(endpoints);
        int numThreads = attic.getDownloadConfig().getMaxFileConnections();
        if (numThreads > endpoints.size()) {
            numThreads = endpoints.size();
        }
        try {
            for (int i = 0; i < numThreads; i++) {
                futures.add(ecs.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return get(queue, dd, coll, fh);
                    }
                }));
            }
            for (int count = 0; count < numThreads; count++) {
                try {
                    Future<Boolean> f = ecs.take();
                    Boolean b = f.get();
                    log.fine("got next Future");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    log.warning("Execution exception thrown:" + e.getMessage());
                    e.printStackTrace();
                }
            }

        } finally {
            for (Future<Boolean> future : futures) {
                log.fine("cancelling future...");
                future.cancel(true);
            }
        }
        return coll;
    }

    private static Boolean get(Queue<Endpoint> queue, DataDescription dd, RequestCollection coll, FileHash fh) {
        log.fine("size of queue:" + queue.size());
        while (!queue.isEmpty()) {
            Endpoint endpoint = queue.remove();
            try {
                OutChannel out = ChannelFactory.getFactory().createOutChannel(null);
                ChannelData data = new ChannelData(ChannelData.Action.GET);
                data.setResponseType(FileHash.class);
                data.setCloseOnFinish(true);
                String meta = endpoint.getMetaEndpoint();
                if (meta != null) {
                    Endpoint target = new Endpoint(meta);
                    target = target.addQuery(StringConstants.FILE_HASH_KEY, dd.getId());
                    data.setEndpoint(target.toString());
                    data = out.send(data);
                    if (data.getOutcome() == ChannelData.Outcome.OK) {
                        if (data.getResponseData() != null && data.getResponseData() instanceof FileHash) {
                            FileHash hash = (FileHash) data.getResponseData();
                            if (hash.getHash().equals(dd.getHash().getHash())) {
                                if (hash.getNumChunks() > 0) {
                                    coll.addMapping(new EndpointRequest(hash.getChunks(), endpoint, data.getOutTime()));
                                } else {
                                    coll.addReserveMapping(new EndpointRequest(fh.getChunks(), endpoint, data.getOutTime()));
                                }
                            }
                        }
                    } else if (data.getOutcome() == ChannelData.Outcome.NOT_FOUND) {
                        coll.addReserveMapping(new EndpointRequest(fh.getChunks(), endpoint));
                    } else {   // ikelley
                   //     EndpointStats.getStats().addBadConnectionEndpoint(endpoint);
                    }
                } else {
                    coll.addReserveMapping(new EndpointRequest(fh.getChunks(), endpoint));
                }
            } catch (IOException e) {
                e.printStackTrace();
                EndpointStats.getStats().addBadConnectionEndpoint(endpoint);
            } catch (Exception e) {
                e.printStackTrace();
                EndpointStats.getStats().addBadConnectionEndpoint(endpoint);
            }

        }
        return true;

    }
}
