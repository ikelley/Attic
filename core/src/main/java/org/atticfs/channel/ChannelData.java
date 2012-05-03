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

package org.atticfs.channel;

import java.io.File;
import java.util.List;

import org.atticfs.identity.Identity;
import org.atticfs.types.Endpoint;

/**
 * ...
 *
 * 
 */

public class ChannelData {

    public static enum Action {
        MESSAGE, // the channel data contains a message
        GET, // get a resource. Request payload depends on implementation. Response payload is expected
        DELETE, // delete a resource. Request payload depends on implementation. Response payload requirement is implementation specific
        UPDATE, // change a resource. Payload required (the update). Response payload requirement is implementation specific
        CREATE // create a new resource. Payload required (the new resource representation). Response payload requirement is implementation specific
    }

    public static enum Outcome {
        OK, // all is good
        CLIENT_ERROR, // your fault
        SERVER_ERROR, // their fault
        AUTHENTICATION_FAILED,
        NOT_FOUND,
        CREATED,
        ACTION_NOT_ALLOWED,
        NOT_MODIFIED,
        SEE_OTHER,
        UNKNOWN // no one taking responsibility
    }

    private Endpoint endpoint;
    private Object requestData;
    private Object responseData;
    private Action action;
    private Class responseType;
    private Outcome outcome = Outcome.UNKNOWN;
    private String outcomeDetail;
    private String target;
    private ByteRange byteRange;
    private Identity remoteIdentity;
    private Identity localIdentity;


    private String requestPath;
    private String mimeType = "text/plain";

    private String location;

    private int timeout = -1;
    private long outTime = -1;
    private long inTime = -1;
    private int bufferSize = 8196;
    private File outputFile;
    private int connectionRetryCount = 0;


    private boolean closeOnFinish = false;
    private boolean useCompression = true;

    private long bytesSent = -1;
    private long bytesReceived = -1;

    private boolean authorized = false;
    private String authenticationAction = "unknown";

    private List<String> acceptedMimeTypes = null;

    public ChannelData(Action action, String endpoint) {
        this.endpoint = new Endpoint(endpoint);
        this.action = action;
    }

    public ChannelData(Action action, Endpoint endpoint) {
        this.endpoint = endpoint;
        this.action = action;
    }

    public ChannelData(Action action) {
        this.action = action;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = new Endpoint(endpoint);
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public String getOutcomeDetail() {
        return outcomeDetail;
    }

    public void setOutcomeDetail(String outcomeDetail) {
        this.outcomeDetail = outcomeDetail;
    }

    public Object getRequestData() {
        return requestData;
    }

    public void setRequestData(Object requestData) {
        this.requestData = requestData;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }

    /**
     * class of expected response type, if a response is expected.
     *
     * @return
     */
    public Class getResponseType() {
        return responseType;
    }

    public void setResponseType(Class responseType) {
        this.responseType = responseType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ByteRange getByteRange() {
        return byteRange;
    }

    public void setByteRange(ByteRange byteRange) {
        this.byteRange = byteRange;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getOutTime() {
        return outTime;
    }

    public void setOutTime(long outTime) {
        this.outTime = outTime;
    }

    public long getInTime() {
        return inTime;
    }

    public void setInTime(long inTime) {
        this.inTime = inTime;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public Identity getRemoteIdentity() {
        return remoteIdentity;
    }

    public void setRemoteIdentity(Identity remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
    }

    public Identity getLocalIdentity() {
        return localIdentity;
    }

    public void setLocalIdentity(Identity localIdentity) {
        this.localIdentity = localIdentity;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getAuthenticationAction() {
        return authenticationAction;
    }

    public void setAuthenticationAction(String authenticationAction) {
        this.authenticationAction = authenticationAction;
    }

    public boolean isCloseOnFinish() {
        return closeOnFinish;
    }

    public void setCloseOnFinish(boolean closeOnFinish) {
        this.closeOnFinish = closeOnFinish;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }

    public void setAcceptedMimeTypes(List<String> acceptedMimeTypes) {
        this.acceptedMimeTypes = acceptedMimeTypes;
    }

    public boolean isUseCompression() {
        return useCompression;
    }

    public void setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
    }

    public int getConnectionRetryCount() {
        return connectionRetryCount;
    }

    public void setConnectionRetryCount(int connectionRetryCount) {
        this.connectionRetryCount = connectionRetryCount;
    }
}
