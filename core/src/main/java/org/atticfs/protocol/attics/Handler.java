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

import org.atticfs.protocol.AtticProtocol;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Class Description Here...
 *
 * 
 */

public class Handler extends URLStreamHandler {


    protected URLConnection openConnection(URL url) throws IOException {

        String scheme = url.getProtocol();
        if (!(scheme.equalsIgnoreCase(AtticProtocol.SCHEME_ATTICS))) {
            throw new IOException("unsupported URL scheme:" + scheme);
        }

        return new AtticsConnection(url);
    }
}