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

import java.io.File;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class Home {

    static Logger log = Logger.getLogger("org.atticfs.util.Home");


    public static File home() {
        File appHome;
        String attic = "Attic";
        File home = new File(System.getProperty("user.home"));
        if (!home.isDirectory()) {
            log.severe("User home not a valid directory: " + home);
            appHome = new File(attic);
        } else {
            String os = System.getProperty("os.name").toLowerCase();
            log.fine("OS is " + os);
            if (os.indexOf("mac os x") > -1) {
                File libDir = new File(home, "Library/Application Support");
                libDir.mkdirs();
                appHome = new File(libDir, attic);
            } else if (os.startsWith("windows")) {
                String APPDATA = System.getenv("APPDATA");
                File appData = null;
                if (APPDATA != null) {
                    appData = new File(APPDATA);
                }
                if (appData != null && appData.isDirectory()) {
                    appHome = new File(appData, attic);
                } else {
                    log.severe("Could not find %APPDATA%: " + APPDATA);
                    appHome = new File(home, attic);
                }
            } else {
                appHome = new File(home, "." + attic.toLowerCase());
            }
        }
        if (!appHome.exists()) {
            if (appHome.mkdir()) {
            } else {
                log.severe("Could not create " + appHome);
            }
        }
        return appHome;
    }
}
