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

/**
 * attic URI/URL components
 *
 * 
 */

public class StringConstants {
    /**
     * path component to data
     */
    public static final String DATA_KEY = "data";
    /**
     * path component to meta data - descriptions and file hashes
     */
    public static final String META_KEY = "meta";

    /**
     * path component to publisher role
     */
    public static final String PUBLISH_KEY = "publish";

    /**
     * path component that can be used to specify a seed endpoint
     */
    public static final String SEED_KEY = "seed";

    /**
     * path component that can be used to identify a cache request.
     */
    public static final String CACHE_KEY = "cache";

    public static final String POINTER_KEY = "pointer";

    public static final String CONFIG_KEY = "config";

    /**
     * key for file ids. This can either be a query or a path component
     */
    public static final String FILE_ID_KEY = "fileid";
    /**
     * key for file hashes. This can either be a query or a path component
     */
    public static final String FILE_HASH_KEY = "filehash";
    /**
     * key for data descriptions. This can either be a query or a path component
     */
    public static final String DESCRIPTION_KEY = "description";

    /**
     * config keys
     */
    public static final String CONFIG_DIR = "config";
    public static final String ATTIC_PROPS = "attic.properties";
    public static final String PORT = "port";
    public static final String ROLE = "role";
    public static final String BOOTSTRAP_ENDPOINT = "bootstrap-endpoint";

    public static final String ROLE_DW = "DW";
    public static final String ROLE_DC = "DC";
    public static final String ROLE_DL = "DL";
    public static final String ROLE_DP = "DP";
    public static final String ROLE_DS = "DS";
    public static final String DATA_CENTER = "dc";
    public static final String DATA_WORKER = "dw";
    public static final String DATA_PUBLISHER = "dp";
    public static final String DATA_LOOKUP = "dl";
    public static final String DESC_DIR = "desc";
    public static final String DATA_DIR = "data";
    public static final String EXT_DESC = ".atticmd";
    public static final String EXT_DATA = ".atticd";
    public static final String EXT_UNMAPPED = ".unmapped";
}
