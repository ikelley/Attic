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

/**
 * estimated Goodput for a file download
 *
 * 
 */

public class Goodput {

    private volatile long totalTime = 0;
    private volatile long totalData = 0;
    private boolean compromised = false;
    private double kbps = 0.0;

    public synchronized void addChannelData(long outTime, long inTime, long outData, long inData) {
        if (inTime < 0 || outTime < 0 || outData < 0 || inData < 0) {
            compromised = true;
        }
        if (inTime > 0) {
            totalTime += inTime;
        }
        if (outTime > 0) {
            totalTime += outTime;
        }
        if (inData > 0) {
            totalData += inData;
        }
        if (outData > 0) {
            totalData += outData;
        }

    }

    public void compile() {
        // TODO umm... is this right????
        kbps = (8 * (double) totalData) / ((double) totalTime / 1000.0) / 1000.0;
    }

    public double getKbps() {
        return kbps;
    }

    public double getMbps() {
        return kbps / 1000.0;
    }

    public double getMBps() {
        return getMbps() / 8.0;
    }

    public boolean isCompromised() {
        return compromised;
    }
}
