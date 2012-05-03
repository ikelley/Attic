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

import org.atticfs.types.Endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps stats about the network and the status of the node.
 * Load is calculated as:
 * 1. num requests over a given time span.
 * 2. amount of data transfered over that time.
 * <p/>
 * The amount of data might not be able to be calculated, if the length of it is not known.
 *
 * 
 */

public class EndpointStats {

    private List<Endpoint> badConnectionEndpoints = new ArrayList<Endpoint>();

    private List<Endpoint> badHashEndpoints = new ArrayList<Endpoint>();

    private List<Endpoint> badRequestEndpoints = new ArrayList<Endpoint>();


    private static EndpointStats endpointStats = new EndpointStats();

    private EndpointStats() {
    }

    public static EndpointStats getStats() {
        return endpointStats;
    }

    public synchronized void addBadHashEndpoint(Endpoint endpoint) {
        if (!badHashEndpoints.contains(endpoint)) {
            badHashEndpoints.add(endpoint);
        }
    }

    public synchronized void addBadConnectionEndpoint(Endpoint endpoint) {
        if (!badConnectionEndpoints.contains(endpoint)) {
            badConnectionEndpoints.add(endpoint);
        }
    }

    public synchronized void addBadMessageEndpoint(Endpoint endpoint) {
        if (!badRequestEndpoints.contains(endpoint)) {
            badRequestEndpoints.add(endpoint);
        }
    }

    /**
     * endpoints that have returned data that does not match the hash provided
     *
     * @return
     */
    public synchronized List<Endpoint> getBadHashEndpoints() {
        return Collections.unmodifiableList(badHashEndpoints);
    }

    /**
     * endpoints that could not be connected to
     *
     * @return
     */
    public synchronized List<Endpoint> getBadConnectionEndpoints() {
        return Collections.unmodifiableList(badConnectionEndpoints);
    }

    /**
     * endpoints that could not understand the request
     *
     * @return
     */
    public synchronized List<Endpoint> getBadRequestEndpoints() {
        return Collections.unmodifiableList(badRequestEndpoints);
    }


}
