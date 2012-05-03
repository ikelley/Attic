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

package org.atticfs.log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A Log represents a single log entry. It supports the concept of a Log starting and ending
 * because a Log may represent a process that is longer than a simple send or receive.
 * It also supports logging under error conditions.
 * <p/>
 * Logs are processed by LogProcessors. Log supports global processors which are attached to every
 * Log object created, as well as local processor that only knows about a single Log instance.
 * Global processors will process every Log object in the system. A local processor will only process
 * the single Log.
 * <p/>
 * The static method addLogProcessor() adds a global processor.
 * A Log optionally takes the local processor in its constructor.
 * <p/>
 * When one of the log methods is called, the Log first calls the local processor if it exists.
 * It then cycles through the global processors calling their methods and passing itself
 * as a parameter.
 * <p/>
 * Log also supports minimal java-esque logging.
 * if you get hold of the static Logger object using the static method getLogger()
 * <p/>
 * The toString() method wraps the output in XML comment tags and then calls super.toString().
 *
 * @author Andrew Harrison
 * @version $Revision: 1.15 $
 * @created Jun 18, 2008: 1:52:27 PM
 * @date $Date: 2009-02-19 16:02:56 $ modified by $Author: harrison $
 * @todo need to use a map to store processors
 */


public class Log extends LogProperties {


    public static enum Type {
        STATUS, // no message - just lifecycle log
        SND, // a send
        RCV, // a receive
        RSP,  // a received response from a message
        SND_RSP // a response sent to a message
    }

    private static Vector<LogProcessor> processors = new Vector<LogProcessor>();
    private static Vector<LogProcessor> instanceProcessors = new Vector<LogProcessor>();
    private static Vector<String> enabledLogs = new Vector<String>();
    protected static Vector<String> enabledClasses = new Vector<String>();

    private static boolean hasClosed = false;

    static {
        Properties props = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("proto.logging.properties");
        if (in == null) {
            String f = System.getProperty("proto.logging.properties");
            if (f != null) {
                try {
                    in = new FileInputStream(f);
                } catch (Exception e) {

                }
            }
        }
        if (in != null) {
            try {
                props.load(in);

                String enable = props.getProperty("enableLogs").trim();
                if (enable != null) {
                    if (enable.equals("!")) {
                        setDisabled(true);
                    } else {
                        String[] types = enable.split("[\\s,]+");
                        for (String type : types) {
                            enabledLogs.add(type);
                        }
                    }
                }
                String enableClasses = props.getProperty("enableDebugClasses").trim();
                if (enableClasses != null) {
                    String[] types = enableClasses.split("[\\s,]+");
                    for (String type : types) {
                        enabledClasses.add(type);
                    }
                }
            } catch (Exception e) {

            }
        }
    }


    public static void addLogProcessor(LogProcessor processor) {
        processors.add(processor);
    }

    public static void removeLogProcessor(LogProcessor processor) {
        processors.remove(processor);
    }

    public static void enableOnly(String... scopes) {
        for (String scope : scopes) {
            enabledLogs.add(scope);
        }
    }


    private boolean output(LogProcessor processor) {
        if (processor.isDisabled()) {
            return false;
        }
        if (!enabledLogs.contains(getName())) {
            return false;
        }
        List<String> scopes = processor.getScopes();
        for (String scope : scopes) {
            if (scope.equals(getName())) {
                return true;
            }
        }
        return false;
    }

    private static AtomicInteger next = new AtomicInteger(0);
    private ExchangeInfo exchangeInfo;
    private static boolean disabled = false;
    private LogProcessor logProcessor;

    public Log(Type type) {
        this("Log", type, null);
    }

    public Log(Type type, LogProcessor logProcessor) {
        this("Log", type, logProcessor);
    }

    public Log(String name, Type type) {
        this(name, type, null);
    }

    public Log(String name, Type type, LogProcessor logProcessor) {
        super(name);
        put("type", type.toString());
        put("id", String.valueOf(next.incrementAndGet()));
        if (logProcessor != null) {
            this.logProcessor = logProcessor;
            instanceProcessors.add(logProcessor);
        }
    }

    public static boolean isDisabled() {
        return disabled;
    }

    /**
     * If set to true, disables the processors from being called and reduces the toString()
     * method to a simple statement announcing disability to avoid excess processing
     *
     * @param disable
     */
    public static void setDisabled(boolean disable) {
        disabled = disable;
    }

    public ExchangeInfo getExchangeInfo() {
        return exchangeInfo;
    }

    public void setExchangeInfo(ExchangeInfo exchangeInfo) {
        this.exchangeInfo = exchangeInfo;
        addLogProperties(exchangeInfo);
    }

    public LogProcessor getLogProcessor() {
        return logProcessor;
    }

    public void setLogProcessor(LogProcessor logProcessor) {
        if (this.logProcessor != null) {
            instanceProcessors.remove(this.logProcessor);
            this.logProcessor.close();
        }
        this.logProcessor = logProcessor;
    }

    public String getAction() {
        return get("action");
    }

    public void setAction(String action) {
        put("action", action);
    }

    public int getId() {
        return Integer.parseInt(get("id"));
    }

    public int getRelatesTo() {
        String rel = get("relatesTo");
        if (rel != null) {
            return Integer.parseInt(rel);
        }
        return -1;
    }

    public void setRelatesTo(int relatesTo) {
        put("relatesTo", String.valueOf(relatesTo));
    }

    public Type getType() {
        return Type.valueOf(get("type"));
    }

    public String getRole() {
        return get("role");
    }

    public void setRole(String role) {
        put("role", role);
    }

    public void addDescription(String title, String description) {
        addLogProperties(new Description(title, description));
    }

    public LogProperties getThrowable() {
        return getLogProperties("Throwable");
    }

    public void setThrowable(Throwable throwable) {
        String sep = " ";
        if (!isSingleLine()) {
            sep = NL;
        }
        removeLogProperties("Throwable");
        LogProperties t = new LogProperties("Throwable");
        t.put("message", throwable.getMessage());
        StackTraceElement[] trace = throwable.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append(sep);
        for (int i = 0; i < trace.length; i++) {
            sb.append("[" + i + "]").append(trace[i].toString()).append(sep);
        }
        t.put("stackTrace", sb.toString());
        addLogProperties(t);
    }

    public long getTime() {
        String t = get("time");
        if (t == null) {
            t = String.valueOf(System.currentTimeMillis());
            put("time", t);
        }
        return Long.valueOf(t);
    }

    public void setTime(long time) {
        put("time", String.valueOf(time));
    }

    public String toString() {
        if (isDisabled()) {
            return NL + "<!--Log is currently disabled-->";
        }
        String sep = " ";
        if (!isSingleLine()) {
            sep = NL;
        }
        StringBuilder sb = new StringBuilder("<!--");
        sb.append(super.toString());
        sb.append(sep + "-->");

        return sb.toString();
    }

    public void logStart() {
        if (isDisabled()) {
            return;
        }
        getTime();
        if (logProcessor != null && output(logProcessor)) {
            logProcessor.start(this);
        }
        for (LogProcessor processor : processors) {
            if (output(processor)) {
                processor.start(this);
            }
        }
    }

    /**
     * calls close() on the local processor
     */
    public void logEnd() {
        if (isDisabled()) {
            return;
        }
        getTime();
        if (logProcessor != null) {
            if (output(logProcessor)) {
                logProcessor.end(this);
            }
            logProcessor.close();
        }
        for (LogProcessor processor : processors) {
            if (output(processor)) {
                processor.end(this);
            }
        }
    }

    /**
     * calls close() on the local processor
     */
    public void logError(Throwable t) {
        if (isDisabled()) {
            return;
        }
        setThrowable(t);
        getTime();
        if (logProcessor != null) {
            if (output(logProcessor)) {
                logProcessor.error(this);
            }
            logProcessor.close();
        }
        for (LogProcessor processor : processors) {
            if (output(processor)) {
                processor.error(this);
            }
        }
    }

    /**
     * closes the global processors
     */
    public static synchronized void close() {
        if (!hasClosed) {
            for (LogProcessor processor : processors) {
                processor.close();
            }
            for (LogProcessor processor : instanceProcessors) {
                processor.close();
            }
            hasClosed = true;
        }
    }

    public static class Description extends LogProperties {

        public Description(String title, String description) {
            super(title);
            put("description", description);
        }
    }

    public static class ExchangeInfo extends LogProperties {

        public ExchangeInfo(String protocol, String localAddress, int localPort, String remoteAddress, int remotePort) {
            super("ExchangeInfo");
            String node = "localhost";

            put("node", node);
            put("protocol", protocol);
            put("localAddress", localAddress);
            put("localPort", String.valueOf(localPort));
            put("remoteAddress", remoteAddress);
            put("remotePort", String.valueOf(remotePort));
        }

        public ExchangeInfo(String protocol, String localAddress, int localPort, String remoteAddress, int remotePort, String node) {
            this(protocol, localAddress, localPort, remoteAddress, remotePort);
            put("node", node);
        }

        public String getProtocol() {
            return get("protocol");
        }

        public String getLocalAddress() {
            return get("localAddress");
        }

        public int getLocalPort() {
            return Integer.parseInt(get("localPort"));
        }

        public String getRemoteAddress() {
            return get("remoteAddress");
        }

        public int getRemotePort() {
            return Integer.parseInt(get("remotePort"));
        }

        public String getNode() {
            return get("node");
        }
    }


}
