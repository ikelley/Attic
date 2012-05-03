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

package org.atticfs.protocol;

/**
 * Class Description Here...
 *
 * 
 */

public class AtticProtocol {

    public static final String SCHEME_ATTIC = "attic";
    public static final String SCHEME_ATTICS = "attics";

    public static void registerAttic() {
        String pkgs = "java.protocol.handler.pkgs";
        String pkg = "org.atticfs.protocol";
        String handlers = System.getProperty(pkgs);
        if (handlers != null && handlers.length() > 0) {
            String[] all = handlers.split("|");
            for (String s : all) {
                if (s.equals(pkg.trim())) {
                    return;
                }
            }
            if (!handlers.endsWith("|")) {
                pkg = "|" + pkg;
            }
            handlers += pkg;
        } else {
            handlers = pkg;
        }
        System.setProperty(pkgs, handlers);
    }


}
