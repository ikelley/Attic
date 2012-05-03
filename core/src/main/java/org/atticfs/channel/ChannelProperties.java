package org.atticfs.channel;

import java.io.File;
import java.net.InetAddress;

/**
 * The ... class ...
 * <p/>
 * Created by scmijt
 * Date: Apr 8, 2009
 * Time: 10:48:46 AM
 */
public class ChannelProperties {

    private InetAddress localAddress;
    private int localPort;
    private String serverContext = null;
    private File outputDirectory = null;

    public ChannelProperties() {
    }

    public InetAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getServerContext() {
        return serverContext;
    }

    public void setServerContext(String serverContext) {
        this.serverContext = serverContext;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
