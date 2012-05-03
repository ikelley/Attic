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

package org.atticfs.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * expected paths are:
 * <p/>
 * for data either:
 * .../anthing?data=1234
 * or
 * .../data/1234
 * <p/>
 * for descriptions either
 * .../anything?description=1234
 * or
 * .../description/1234
 * <p/>
 * for file hashes either
 * .../anything?filehash=1234
 * or
 * .../filehash/1234
 *
 * 
 */

public class UriUtils {

    static Logger log = Logger.getLogger("org.atticfs.util.UriUtils");


    public static String extractId(String requestPath, String key) {
        try {
            URI uri = new URI(requestPath);
            String query = uri.getQuery();
            if (query != null) {
                Map<String, String> queries = getQueryValues(query);
                String id = queries.get(key);
                if (id != null) {
                    return id.trim();
                }
            }
            String path = uri.getPath();
            if (path.length() > 0) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (path.length() > 0) {
                    String[] components = path.split("/");
                    if (components.length > 0) {
                        if (components.length == 1) {
                            return components[0].trim();
                        } else {
                            boolean isNext = false;
                            for (String component : components) {
                                if (isNext) {
                                    return component.trim();
                                }
                                if (component.equalsIgnoreCase(key)) {
                                    isNext = true;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } catch (URISyntaxException e) {
            return requestPath;
        }
    }

    public static String appendPath(String root, String path) {
        if (root.endsWith("/")) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        } else {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }
        log.fine("UriUtils.appendPath returning:" + root + path);
        return root + path;
    }

    public static Map<String, String> getQueryValues(String input) {

        Map<String, String> ret = new HashMap<String, String>();
        String[] params = input.split("&");
        for (String param : params) {
            try {
                int eq = param.indexOf("=");
                if (eq > 0) {
                    String key = param.substring(0, eq);
                    String value = param.substring(eq + 1, param.length());
                    value = URLDecoder.decode(value, "UTF-8");
                    ret.put(key, value);
                }
            } catch (UnsupportedEncodingException e) {

            }
        }
        return ret;

    }
}
