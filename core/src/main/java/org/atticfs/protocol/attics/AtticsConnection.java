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

package org.atticfs.protocol.attics;

import org.atticfs.Attic;
import org.atticfs.protocol.attic.AtticConnection;

import java.net.URL;

/**
 * URL connection for the attics protocol.
 * This supports two request properties that are propagated to an Attic instance's StreamConfig:
 * <p/>
 * 1. MAX_INMEMORY_BUFFER - maximum in memory buffer size used for verifying the stream data
 * 2. ATTEMPT_VERIFICATION - whether to attempt verification of data as it arrives.
 * This is dependent on the hashed chunk sizes being <= maximum buffer size.
 *
 * 
 */

public class AtticsConnection extends AtticConnection {

    public AtticsConnection(URL url) {
        super(url);
    }

    public AtticsConnection(URL url, Attic attic) {
        super(url, attic);
    }

    protected String getHttpScheme() {
        return "https";
    }

}