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

package org.atticfs.stats;

import org.atticfs.channel.ChannelData;
import org.atticfs.types.Endpoint;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * statistics for a download.
 *
 * 
 */

public class DownloadStats {

    private String id;
    private int numThreads = -1;
    private int numChunks = -1;
    private long preStartTime = -1;
    private long preEndTime = -1;
    private long postStartTime = -1;
    private long postEndTime = -1;
    private long startTime = -1;
    private long endTime = -1;
    private String initialStatus = "unknown";
    private String finalStatus = "unknown";
    private AtomicInteger verifyDuplicates = new AtomicInteger(0);
    private Goodput goodput = new Goodput();

    private Map<Endpoint, EndpointDownloadStats> epstats = new HashMap<Endpoint, EndpointDownloadStats>();
    private Map<String, String> verified = new HashMap<String, String>();

    public DownloadStats(String id) {
        this.id = id;
    }

    public EndpointDownloadStats getEndpointStats(Endpoint endpoint) {
        synchronized (epstats) {
            EndpointDownloadStats stats = epstats.get(endpoint);
            if (stats == null) {
                stats = new EndpointDownloadStats(endpoint);
                epstats.put(endpoint, stats);
            }

            return stats;
        }
    }

    public Goodput getGoodput() {
        return goodput;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getNumHosts() {
        return epstats.size();
    }

    public int getVerifyDuplicates() {
        return verifyDuplicates.get();
    }

    private void incVerifyDuplicates() {
        this.verifyDuplicates.incrementAndGet();
    }

    public void onVerify(String hash) {
        synchronized (verified) {
            if (verified.containsKey(hash)) {
                incVerifyDuplicates();
            } else {
                verified.put(hash, hash);
            }
        }
    }

    public int getNumChunks() {
        return numChunks;
    }

    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }

    public String getId() {
        return id;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(String initialStatus) {
        this.initialStatus = initialStatus;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getPreStartTime() {
        return preStartTime;
    }

    public void setPreStartTime(long preStartTime) {
        this.preStartTime = preStartTime;
    }

    public long getPreEndTime() {
        return preEndTime;
    }

    public void setPreEndTime(long preEndTime) {
        this.preEndTime = preEndTime;
    }

    public long getPostStartTime() {
        return postStartTime;
    }

    public void setPostStartTime(long postStartTime) {
        this.postStartTime = postStartTime;
    }

    public long getPostEndTime() {
        return postEndTime;
    }

    public void setPostEndTime(long postEndTime) {
        this.postEndTime = postEndTime;
    }

    public double totalTimeSeconds() {
        if (startTime > 0 && endTime > 0) {
            return (endTime - startTime) / 1000.0;
        }
        return -1;
    }

    public double totalPreTimeSeconds() {
        if (preStartTime > 0 && preEndTime > 0) {
            return (preEndTime - preStartTime) / 1000.0;
        }
        return -1;
    }

    public double totalPostTimeSeconds() {
        if (postStartTime > 0 && postEndTime > 0) {
            return (postEndTime - postStartTime) / 1000.0;
        }
        return -1;
    }

    public void addChannelData(ChannelData data, EndpointDownloadStats eps) {

        long outT = data.getOutTime();
        long inT = data.getInTime();

        long outData = data.getBytesSent();
        long inData = data.getBytesReceived();
        synchronized (eps) {
            if (outT > 0) {
                eps.incTotalTime(outT);
            }
            if (inT > 0) {
                eps.incTotalTime(inT);
            }
            if (outData > 0) {
                eps.incTotalData(outData);
            }
            if (inData > 0) {
                eps.incTotalData(inData);
            }
        }
        getGoodput().addChannelData(outT, inT, outData, inData);
    }

    public String display() {
        goodput.compile();
        return toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("*******************************\n");
        sb.append("Id:                     ").append(id).append("\n");
        sb.append("Threads:                ").append(numThreads).append("\n");
        sb.append("Hosts:                  ").append(getNumHosts()).append("\n");
        sb.append("Chunks:                 ").append(numChunks).append("\n");
        sb.append("Pre Process (secs):     ").append(totalPreTimeSeconds()).append("\n");
        sb.append("Download (secs):        ").append(totalTimeSeconds()).append("\n");
        sb.append("Post Process (secs):    ").append(totalPostTimeSeconds()).append("\n");
        sb.append("Initial Status:         ").append(initialStatus).append("\n");
        sb.append("Final Status:           ").append(finalStatus).append("\n");
        sb.append("Duplicate Downloads:    ").append(verifyDuplicates.get()).append("\n");
        sb.append("Goodput MBps:           ").append(goodput.getMBps()).append("\n");
        for (EndpointDownloadStats endpointDownloadStats : epstats.values()) {
            sb.append(endpointDownloadStats.toString());
        }
        sb.append("*******************************\n");
        return sb.toString();
    }

    public void print(PrintStream stream) {
        stream.println(toString());
    }


    public static class EndpointDownloadStats {
        private Endpoint endpoint;
        private AtomicLong totalData = new AtomicLong(0);
        private AtomicInteger totalRequests = new AtomicInteger(0);
        private AtomicLong totalTime = new AtomicLong(0);

        public EndpointDownloadStats(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        public long getTotalData() {
            return totalData.get();
        }

        public void incTotalData(long totalData) {
            this.totalData.addAndGet(totalData);
        }

        public int getTotalRequests() {
            return totalRequests.get();
        }

        public void incTotalRequests() {
            this.totalRequests.incrementAndGet();
        }

        public long getTotalTime() {
            return totalTime.get();
        }

        public void incTotalTime(long totalTime) {
            this.totalTime.addAndGet(totalTime);
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("----\n");
            sb.append("  Endpoint:             ").append(endpoint).append("\n");
            sb.append("  Total Requests:       ").append(totalRequests.get()).append("\n");
            sb.append("  Total Time (secs):    ").append((totalTime.get() / 1000.0)).append("\n");
            sb.append("  total Data (MB):      ").append((totalData.get() / 1024.0 / 1024.0)).append("\n");
            sb.append("----\n");
            return sb.toString();

        }
    }
}
