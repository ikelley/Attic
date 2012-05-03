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

package org.atticfs.impl.roles.main;

import org.atticfs.Attic;
import org.atticfs.impl.roles.dc.DataCenter;
import org.atticfs.impl.roles.dl.DataLookup;
import org.atticfs.impl.roles.dp.DataPublisher;
import org.atticfs.impl.roles.dp.DataSeed;
import org.atticfs.impl.roles.dw.DataWorker;
import org.atticfs.roles.Role;
import org.atticfs.types.Endpoint;
import org.atticfs.util.Home;
import org.atticfs.util.StringConstants;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 */

public class Controller {

    private Attic attic;

    public void run() throws Exception {
        attic = new Attic();
        List<String> type = attic.getRoles();
        attic.attach("attic", new ConfigServiceRole());
        for (String s : type) {
            Role role = getRole(attic, s);
            attic.attach(s, role);
        }
        attic.init();
    }

    public void install() throws Exception {
        attic = new Attic();
        ConfigServiceRole conf = new ConfigServiceRole(this, true);
        attic.attach("attic", conf);
        attic.init();
        try {
            // not really needed, but just in case
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        Launcher.openURL(conf.getEndpoint().appendToPath(StringConstants.CONFIG_KEY).toString());
    }

    public void shutdown() {
        Timer timer = new Timer();
        timer.schedule(new ShutdownTimer(), 4000);
    }

    private Role getRole(Attic attic, String type) throws Exception {
        String dls = attic.getBootstrapEndpoint();
        if (type.equals(StringConstants.ROLE_DL)) {
            return new DataLookup();
        } else if (type.equals(StringConstants.ROLE_DW)) {
            return new DataWorker();
        } else if (type.equals(StringConstants.ROLE_DP)) {
            if (dls == null || dls.length() == 0) {
                throw new Exception("No dls endpoint defined.");
            }
            return new DataPublisher(new Endpoint(dls));
        } else if (type.equals(StringConstants.ROLE_DC)) {
            if (dls == null || dls.length() == 0) {
                throw new Exception("No dls endpoint defined.");
            }
            return new DataCenter(new Endpoint(dls));
        } else if (type.equals(StringConstants.ROLE_DS)) {
            if (dls == null || dls.length() == 0) {
                throw new Exception("No dls endpoint defined.");
            }
            return new DataSeed(new Endpoint(dls));
        }
        throw new Exception("Unknown Role:" + type);
    }

    public Attic getAdics() {
        return attic;
    }

    public static void main(String[] args) {
        File home = Home.home();
        home = new File(home, StringConstants.CONFIG_DIR);
        home.mkdirs();
        File props = new File(home, StringConstants.ATTIC_PROPS);
        Controller c = new Controller();
        try {
            if (!props.exists() || props.length() == 0) {
                c.install();
            } else {
                c.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class ShutdownTimer extends TimerTask {

        public void run() {
            if (attic != null) {
                attic.shutdown();
            }
            System.exit(0);
        }
    }


}
