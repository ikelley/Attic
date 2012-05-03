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

package org.atticfs;

import org.atticfs.channel.ChannelFactory;
import org.atticfs.config.data.DataConfig;
import org.atticfs.config.download.DownloadConfig;
import org.atticfs.config.html.Configurable;
import org.atticfs.config.security.SecurityConfig;
import org.atticfs.config.stream.StreamConfig;
import org.atticfs.roles.Role;
import org.atticfs.ser.Serializer;
import org.atticfs.util.ConfigFinder;
import org.atticfs.util.FileUtils;
import org.atticfs.util.Home;
import org.atticfs.util.StringConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class Attic {

    static Logger log = Logger.getLogger("org.atticfs.Attic");

    private String homeDir;
    private static String defHome;

    private File propsFile;
    private Properties atticProperties = new Properties();

    private SecurityConfig securityConfig = new SecurityConfig();
    private DownloadConfig downloadConfig = new DownloadConfig();
    private DataConfig dataConfig = new DataConfig();
    private StreamConfig streamConfig = new StreamConfig();
    private List<Configurable> configurables = new ArrayList<Configurable>();

    private ExecutorService executor;
    private Serializer serializer;
    private ChannelFactory channelFactory;
    private Properties props = new Properties();

    private volatile boolean initialized = false;

    private Map<String, Role> roles = new ConcurrentHashMap<String, Role>();

    static {
        File appHome = Home.home();
        defHome = appHome.getAbsolutePath();
    }


    public Attic(String homeDir) {
        this.homeDir = homeDir;
        try {
            File conf = new File(homeDir, StringConstants.CONFIG_DIR);
            conf.mkdirs();
            propsFile = new File(conf, StringConstants.ATTIC_PROPS);

            if (!propsFile.exists() || propsFile.length() == 0) {
                writeDefaultProperties();
            }
            FileInputStream fin = new FileInputStream(propsFile);
            atticProperties.load(fin);
            fin.close();

        } catch (IOException e) {
            log.severe("Could not create attic properties");
        }
        new ShutdownHook().createHook();
    }

    public Attic() {
        this(defHome);
    }

    public static Attic getDefaultAttic() {
        return new Attic().init();
    }


    private void writeDefaultProperties() {
        atticProperties.setProperty(StringConstants.PORT, "28842"); // spells attic on your mob
        atticProperties.setProperty(StringConstants.ROLE, StringConstants.ROLE_DW);
        atticProperties.setProperty(StringConstants.BOOTSTRAP_ENDPOINT, "");
        writeProperties();
    }

    private void writeProperties() {
        try {
            FileOutputStream fout = new FileOutputStream(propsFile);
            atticProperties.store(fout, "Attic Properties");
            fout.close();
        } catch (IOException e) {
            throw new RuntimeException("could not create properties.", e);
        }
    }

    public int getPort() {
        int port = -1;
        try {
            port = Integer.parseInt(atticProperties.getProperty(StringConstants.PORT));
        } catch (NumberFormatException e) {

        }
        return port;
    }

    public List<String> getRoles() {
        List<String> roles = new ArrayList<String>();
        String r = atticProperties.getProperty(StringConstants.ROLE);
        String[] ls = r.split(";");
        for (String l : ls) {
            String t = l.trim();
            if (t.length() > 0) {
                roles.add(t);
            }
        }
        return roles;
    }

    public void addRole(String role) {
        if (role == null || role.length() == 0) {
            return;
        }
        List<String> all = getRoles();
        if (all.contains(role)) {
            return;
        }
        all.add(role);
        String prop = "";
        for (String s : all) {
            prop += s + ";";
        }
        atticProperties.setProperty(StringConstants.ROLE, prop);
    }

    public void setRole(String role) {
        cleanRoles();
        addRole(role);
    }

    public void removeRole(String role) {
        if (role == null || role.length() == 0) {
            return;
        }
        List<String> all = getRoles();
        if (!all.contains(role)) {
            return;
        }
        all.remove(role);
        String prop = "";
        for (String s : all) {
            prop += s + ";";
        }
        atticProperties.setProperty(StringConstants.ROLE, prop);
    }

    public void cleanRoles() {
        atticProperties.setProperty(StringConstants.ROLE, "");
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public String getBootstrapEndpoint() {
        return atticProperties.getProperty(StringConstants.BOOTSTRAP_ENDPOINT);
    }

    public void setPort(int port) {
        atticProperties.setProperty(StringConstants.PORT, port + "");
    }

    public void setBootstrapEndpoint(String endpoint) {
        atticProperties.setProperty(StringConstants.BOOTSTRAP_ENDPOINT, endpoint);
    }

    public Attic init() {
        File home = new File(homeDir);
        home.mkdirs();
        // data center home
        File curr = new File(home, StringConstants.DATA_CENTER);
        curr.mkdirs();
        File desc = new File(curr, StringConstants.DESC_DIR);
        desc.mkdirs();
        curr = new File(curr, StringConstants.DATA_DIR);
        curr.mkdirs();

        // client home
        curr = new File(home, StringConstants.DATA_WORKER);
        curr.mkdirs();
        desc = new File(curr, StringConstants.DESC_DIR);
        desc.mkdirs();
        curr = new File(curr, StringConstants.DATA_DIR);
        curr.mkdirs();

        // manager (source) home
        curr = new File(home, StringConstants.DATA_PUBLISHER);
        curr.mkdirs();
        desc = new File(curr, StringConstants.DESC_DIR);
        desc.mkdirs();
        curr = new File(curr, StringConstants.DATA_DIR);
        curr.mkdirs();


        // data lookup home
        curr = new File(home, StringConstants.DATA_LOOKUP);
        curr.mkdirs();
        try {
            loadConfigs();
        } catch (IOException e) {
            log.warning("error loading configs: " + FileUtils.formatThrowable(e));
        }

        executor = Executors.newFixedThreadPool(getDownloadConfig().getMaxTotalConnections());

        channelFactory = ChannelFactory.getFactory();
        channelFactory.init(this);
        List<Object> confs = ConfigFinder.services(Configurable.class);
        if (confs != null) {
            for (Object conf : confs) {
                if (conf instanceof Configurable) {
                    configurables.add((Configurable) conf);
                }
            }
        }

        for (Role role : roles.values()) {
            try {
                role.init(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (getSerializer() == null) {
            throw new RuntimeException("No serializer has been set by either the application, the channel factory or the roles!");
        }
        initialized = true;
        return this;
    }

    public List<Configurable> getConfigurables() {
        return new ArrayList<Configurable>(configurables);
    }

    private void loadConfigs() throws IOException {
        File curr = new File(homeDir, StringConstants.CONFIG_DIR);
        File dataConf = new File(curr, "data.properties");
        if (!dataConf.exists() || dataConf.length() == 0) {
            dataConfig.store(new FileOutputStream(dataConf));
        } else {
            dataConfig.load(new FileInputStream(dataConf));
        }
        File dlConf = new File(curr, "download.properties");
        if (!dlConf.exists() || dlConf.length() == 0) {
            downloadConfig.store(new FileOutputStream(dlConf));
        } else {
            downloadConfig.load(new FileInputStream(dlConf));
        }
        File streamConf = new File(curr, "stream.properties");
        if (!streamConf.exists() || streamConf.length() == 0) {
            streamConfig.store(new FileOutputStream(streamConf));
        } else {
            streamConfig.load(new FileInputStream(streamConf));
        }
        File secConf = new File(curr, "security.properties");
        if (!secConf.exists() || secConf.length() == 0) {
            securityConfig.store(new FileOutputStream(secConf));
        } else {
            securityConfig.load(new FileInputStream(secConf));
        }
    }

    private void storeConfigs() throws IOException {
        File curr = new File(homeDir, StringConstants.CONFIG_DIR);
        File dataConf = new File(curr, "data.properties");
        dataConfig.store(new FileOutputStream(dataConf));
        File dlConf = new File(curr, "download.properties");
        downloadConfig.store(new FileOutputStream(dlConf));
        File streamConf = new File(curr, "stream.properties");
        streamConfig.store(new FileOutputStream(streamConf));
        File secConf = new File(curr, "security.properties");
        securityConfig.store(new FileOutputStream(secConf));
    }

    public void shutdown() {
        if (!initialized) {
            return;
        }
        try {
            for (Role role : roles.values()) {
                role.shutdown();
            }
            executor.shutdownNow();
            writeProperties();
            storeConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public File getConfigDirectory() {
        return new File(homeDir, StringConstants.CONFIG_DIR);
    }

    public Role attach(String name, Role role) {
        return roles.put(name, role);
    }

    @SuppressWarnings("unchecked")
    public <T extends Role> T getRole(String name) {
        Role role = roles.get(name);
        if (role != null) {
            try {
                return (T) role;
            } catch (Exception e) {
            }
        }
        return null;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public DownloadConfig getDownloadConfig() {
        return downloadConfig;
    }

    public DataConfig getDataConfig() {
        return dataConfig;
    }

    public StreamConfig getStreamConfig() {
        return streamConfig;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }


    public Serializer getSerializer() {
        return serializer;
    }


    public void setChannelFactoryClass(String className) {
        System.setProperty(ChannelFactory.FACTORY_PROPERTY, className);
    }

    public String getChannelFactoryClass() {
        return System.getProperty(ChannelFactory.FACTORY_PROPERTY);
    }

    public void setProperty(String key, String value) {
        props.put(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public Executor getExecutor() {
        return executor;
    }

    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }

    public File getHome() {
        return new File(homeDir);
    }

    public File getDataCenterHome() {
        return new File(homeDir, StringConstants.DATA_CENTER);
    }

    public File getDataWorkerHome() {
        return new File(homeDir, StringConstants.DATA_WORKER);
    }

    public File getDataPublisherHome() {
        return new File(homeDir, StringConstants.DATA_PUBLISHER);
    }

    public File getDataLookupHome() {
        return new File(homeDir, StringConstants.DATA_LOOKUP);
    }

    public File getDCDataHome() {
        return new File(getDataCenterHome(), StringConstants.DATA_DIR);
    }

    public File getDCDesc() {
        return new File(getDataCenterHome(), StringConstants.DESC_DIR);
    }

    public File getDPDataHome() {
        return new File(getDataPublisherHome(), StringConstants.DATA_DIR);
    }

    public File getDPDesc() {
        return new File(getDataPublisherHome(), StringConstants.DESC_DIR);
    }

    public File getDWDataHome() {
        return new File(getDataWorkerHome(), StringConstants.DATA_DIR);
    }

    public File getDWDesc() {
        return new File(getDataWorkerHome(), StringConstants.DESC_DIR);
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    private class ShutdownHook extends Thread {

        private void add() {
            try {
                Method shutdownHook = java.lang.Runtime.class.getMethod("addShutdownHook", new Class[]{java.lang.Thread.class});
                shutdownHook.invoke(Runtime.getRuntime(), new Object[]{this});
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void createHook() {
            add();
        }

        public void run() {
            log.fine("Attic$ShutdownHook.run ENTER");
            try {
                shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.fine("Attic$ShutdownHook.run EXIT");
        }
    }


}
