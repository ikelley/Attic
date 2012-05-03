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

package org.atticfs.config.html;

import java.lang.reflect.Method;
import java.util.List;

import org.atticfs.Attic;
import org.atticfs.config.security.Keystore;
import org.atticfs.util.StringConstants;
import org.wspeer.html.Component;
import org.wspeer.html.Container;
import org.wspeer.html.HtmlWriter;
import org.wspeer.html.annotation.AnnotationProcessor;
import org.wspeer.html.annotation.Fieldset;
import org.wspeer.html.annotation.Fieldsets;
import org.wspeer.html.annotation.Form;
import org.wspeer.html.annotation.FormComponent;
import org.wspeer.html.annotation.Html;
import org.wspeer.html.annotation.TextComponent;
import org.wspeer.html.form.FieldSet;

/**
 * Class Description Here...
 *
 * 
 */

@Html(title = "Attic Config", stylesheet = "./config/styles.css")
@Form(index = 1, name = "htmlconf", action = "./config/submit", method = "POST")
@Fieldsets({
        @Fieldset(name = "fsmain", index = 1, form = "htmlconf", legend = "Main Configuration"),
        @Fieldset(name = "fsdata", index = 2, form = "htmlconf", legend = "Data Configuration"),
        @Fieldset(name = "fsdownload", index = 3, form = "htmlconf", legend = "Download Configuration"),
        @Fieldset(name = "fssecurity", index = 4, form = "htmlconf", legend = "Security Configuration"),
        @Fieldset(name = "fsstream", index = 5, form = "htmlconf", legend = "Streaming Configuration")

})
public class HtmlConfig {

    private Attic attic;
    private List<Configurable> configs;


    public HtmlConfig(Attic attic, List<Configurable> configs) {
        this.attic = attic;
        this.configs = configs;
    }

    @TextComponent(heading = "Attic Configuration", index = 0)
    public String description() {
        return "Attic configuration page";
    }

    @FormComponent(fieldset = "fssecurity", label = "Require Client Authentication")
    public boolean isRequireClientAuthentication() {
        return attic.getSecurityConfig().isRequireClientAuthentication();
    }

    public void setRequireClientAuthentication(boolean requireClientAuthentication) {
        attic.getSecurityConfig().setRequireClientAuthentication(requireClientAuthentication);
    }


    @FormComponent(fieldset = "fssecurity", label = "Secure Connections")
    public boolean isSecure() {
        return attic.getSecurityConfig().isSecure();
    }

    public void setSecure(boolean secure) {
        attic.getSecurityConfig().setSecure(secure);
    }

    public void addKeyStore(Keystore keystore) {
        attic.getSecurityConfig().addKeyStore(keystore);
    }

    public void addTrustStore(Keystore keystore) {
        attic.getSecurityConfig().addTrustStore(keystore);
    }

    @FormComponent(fieldset = "fsdata", label = "Maximum space to be used locally (MB)")
    public long getMaxLocalData() {
        return attic.getDataConfig().getMaxLocalData() / 1024 / 1024;
    }

    public void setMaxLocalData(long maxLocalData) {
        attic.getDataConfig().setMaxLocalData(maxLocalData * 1024 * 1024);
    }

    @FormComponent(fieldset = "fsdata", label = "Default file segment size (KB)")
    public int getFileSegmentHashSize() {
        return attic.getDataConfig().getFileSegmentHashSize() / 1024;
    }

    public void setFileSegmentHashSize(int fileSegmentHashSize) {
        attic.getDataConfig().setFileSegmentHashSize(fileSegmentHashSize * 1024 * 1024);
    }

    @FormComponent(fieldset = "fsdata", label = "Cache query interval (sec) For Data Center role")
    public long getDataQueryInterval() {
        return attic.getDataConfig().getDataQueryInterval();
    }

    public void setDataQueryInterval(long dataQueryInterval) {
        attic.getDataConfig().setDataQueryInterval(dataQueryInterval);
    }

    @FormComponent(fieldset = "fsdownload", label = "Stream data directly to the target file")
    public boolean isStreamToTargetFile() {
        return attic.getDownloadConfig().isStreamToTargetFile();
    }

    public void setStreamToTargetFile(boolean streamToTargetFile) {
        attic.getDownloadConfig().setStreamToTargetFile(streamToTargetFile);
    }

    @FormComponent(fieldset = "fsdownload", label = "Download buffer size (KB)")
    public int getBufferSize() {
        return attic.getDownloadConfig().getBufferSize() / 1024;
    }

    public void setBufferSize(int bufferSize) {
        attic.getDownloadConfig().setBufferSize(bufferSize * 1024);
    }

    @FormComponent(fieldset = "fsdownload", label = "Socket Timeout (MS)")
    public int getSocketTimeout() {
        return attic.getDownloadConfig().getSocketTimeout();
    }

    public void setSocketTimeout(int timeout) {
        attic.getDownloadConfig().setSocketTimeout(timeout);
    }

    @FormComponent(fieldset = "fsdownload", label = "Maximum number of total connections allowed")
    public int getMaxTotalConnections() {
        return attic.getDownloadConfig().getMaxTotalConnections();
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        attic.getDownloadConfig().setMaxTotalConnections(maxTotalConnections);
    }

    @FormComponent(fieldset = "fsdownload", label = "Maximum number of connections per download")
    public int getMaxFileConnections() {
        return attic.getDownloadConfig().getMaxFileConnections();
    }

    public void setMaxFileConnections(int maxFileConnections) {
        attic.getDownloadConfig().setMaxFileConnections(maxFileConnections);
    }

    @FormComponent(fieldset = "fsdownload", label = "Connection idle timeout (sec)")
    public int getConnectionIdleTime() {
        return attic.getDownloadConfig().getConnectionIdleTime() / 1000;
    }

    public void setConnectionIdleTime(int connectionIdleTime) {
        attic.getDownloadConfig().setConnectionIdleTime(connectionIdleTime * 1000);
    }

    @FormComponent(fieldset = "fsdownload", label = "Download chunk size (KB)")
    public int getDownloadChunkSize() {
        return attic.getDownloadConfig().getDownloadChunkSize() / 1024;
    }

    public void setDownloadChunkSize(int downloadChunkSize) {
        attic.getDownloadConfig().setDownloadChunkSize(downloadChunkSize * 1024);
    }

    @FormComponent(fieldset = "fsdownload", label = "Download retry count")
    public int getRetryCount() {
        return attic.getDownloadConfig().getRetryCount();
    }

    @FormComponent(fieldset = "fsdownload", label = "Connection retry count")
    public int getConnectionRetryCount() {
        return attic.getDownloadConfig().getConnectionRetryCount();
    }

    public void setConnectionRetryCount(int count) {
        attic.getDownloadConfig().setConnectionRetryCount(count);
    }

    public void setRetryCount(int retryCount) {
        attic.getDownloadConfig().setRetryCount(retryCount);
    }

    @FormComponent(fieldset = "fsdownload", label = "Request Compressed data")
    public boolean isCompress() {
        return attic.getDownloadConfig().isCompress();
    }

    public void setCompress(boolean compress) {
        attic.getDownloadConfig().setCompress(compress);
    }

    @FormComponent(fieldset = "fsmain", label = "Local server port number", index = 6)
    public int getPort() {
        return attic.getPort();
    }

    public void setPort(int port) {
        attic.setPort(port);
    }

    @FormComponent(fieldset = "fsmain", label = "Attic bootstrap (lookup service) host address", index = 7)
    public String getBootstrapEndpoint() {
        return attic.getBootstrapEndpoint();
    }

    public void setBootstrapEndpoint(String endpoint) {
        attic.setBootstrapEndpoint(endpoint);
    }

    @FormComponent(fieldset = "fsmain", label = "Perform Worker Role", index = 1)
    public boolean isWorker() {
        return attic.hasRole(StringConstants.ROLE_DW);
    }

    public void setWorker(boolean b) {
        if (b) {
            attic.addRole(StringConstants.ROLE_DW);
        } else {
            attic.removeRole(StringConstants.ROLE_DW);
        }
    }

    @FormComponent(fieldset = "fsmain", label = "Perform Data Center Role", index = 2)
    public boolean isDataCenter() {
        return attic.hasRole(StringConstants.ROLE_DC);
    }

    public void setDataCenter(boolean b) {
        if (b) {
            attic.addRole(StringConstants.ROLE_DC);
        } else {
            attic.removeRole(StringConstants.ROLE_DC);
        }
    }

    @FormComponent(fieldset = "fsmain", label = "Perform Lookup Service Role", index = 3)
    public boolean isDataLookup() {
        return attic.hasRole(StringConstants.ROLE_DL);
    }

    public void setDataLookup(boolean b) {
        if (b) {
            attic.addRole(StringConstants.ROLE_DL);
        } else {
            attic.removeRole(StringConstants.ROLE_DL);
        }
    }

    @FormComponent(fieldset = "fsmain", label = "Perform Publisher Role", index = 4)
    public boolean isDataPublisher() {
        return attic.hasRole(StringConstants.ROLE_DP);
    }

    public void setDataPublisher(boolean b) {
        if (b) {
            attic.addRole(StringConstants.ROLE_DP);
        } else {
            attic.removeRole(StringConstants.ROLE_DP);
        }
    }

    @FormComponent(fieldset = "fsmain", label = "Perform Data Seed Role", index = 5)
    public boolean isDataSeed() {
        return attic.hasRole(StringConstants.ROLE_DS);
    }

    public void setDataSeed(boolean b) {
        if (b) {
            attic.addRole(StringConstants.ROLE_DS);
        } else {
            attic.removeRole(StringConstants.ROLE_DS);
        }
    }


    @FormComponent(fieldset = "fsstream", label = "Maximum in-memory buffer (KB)")
    public int getMaxBufferSize() {
        return attic.getStreamConfig().getMaxBufferSize() / 1024;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        attic.getStreamConfig().setMaxBufferSize(maxBufferSize * 1024);
    }

    @FormComponent(fieldset = "fsstream", label = "Verify chunks from stream (if buffer size permits)")
    public boolean isAttemptVerification() {
        return attic.getStreamConfig().isAttemptVerification();
    }

    public void setAttemptVerification(boolean attemptVerification) {
        attic.getStreamConfig().setAttemptVerification(attemptVerification);
    }

    public synchronized String getHtml() throws Exception {
        AnnotationProcessor ap = new AnnotationProcessor();
        org.wspeer.html.Html html = ap.buildComponent(this);
        if (html == null) {
            return null;
        }

        Container con = (Container) html.getFirstChildRecursive("name", "htmlconf");
        if (con == null) {
            return null;
        }
        Component but = html.getFirstChildRecursive("type", "submit");
        if (but != null) {
            but.getParent().removeChild(but);
        }
        for (Configurable config : configs) {

            FieldSet fs = new FieldSet();
            fs.addLegend(config.getName());
            Method[] methods = config.getClass().getDeclaredMethods();

            boolean hasContent = false;
            for (Method method : methods) {
                FormComponent formComp = method.getAnnotation(FormComponent.class);
                if (formComp != null) {
                    org.wspeer.html.Container fc = ap.createFormComponent(formComp, method, config);
                    if (fc != null) {
                        fs.addChild(fc);
                        hasContent = true;
                    }
                }
            }
            if (hasContent) {
                con.addChild(fs);
            }
        }
        con.addChild(but);
        return HtmlWriter.writeComponent(html);
    }


}
