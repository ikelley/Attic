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

package org.atticfs.impl.channel.http;

import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelProperties;
import org.atticfs.channel.ChannelRequestHandler;
import org.atticfs.channel.InChannel;
import org.atticfs.impl.identity.X509Identity;
import org.atticfs.types.Endpoint;
import org.wspeer.http.*;
import org.wspeer.http.target.AbstractTarget;
import org.wspeer.streamable.Streamable;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */

public class HttpServer extends AbstractTarget implements InChannel {

    private HttpPeer peer;
    private ChannelRequestHandler handler = null;

    public HttpServer(HttpPeer peer) {
        this.peer = peer;
    }

    public void open(ChannelProperties properties) throws IOException {
        String name = "";
        int port = 8080;
        TargetProperties tps = new TargetProperties();
        if (properties != null) {
            if (properties.getServerContext() != null) {
                name = properties.getServerContext();
            }
            int pport = properties.getLocalPort();
            if (pport > 0) {
                port = pport;
            }
            File output = properties.getOutputDirectory();
            if (output != null) {
                tps.setOutputDirectory(output);
            }
        }

        setPath(name);
        peer.addTarget(this);
        peer.setTargetProperties(this, tps);
        peer.setPort(port);
        peer.open();
    }

    public void open() throws IOException {
        open(null);
    }

    public void close() throws IOException {
        peer.removeTarget(this);
        peer.close();
    }

    public void setRequestHandler(ChannelRequestHandler handler) {
        this.handler = handler;
    }

    public Endpoint getEndpoint() {
        return new Endpoint(peer.getEndpoint());
    }

    public ChannelRequestHandler getRequestHandler() {
        return handler;
    }

    public synchronized Resource getResource(RequestContext context) throws RequestProcessException {
        if (handler != null) {
            return new HandlerResource(this, context.getRequestTarget());
        }
        return null;
    }

    private int getStatusFromOutcome(ChannelData context) {
        ChannelData.Outcome outcome = context.getOutcome();
        if (outcome == null) {
            return 500;
        }
        if (outcome == ChannelData.Outcome.AUTHENTICATION_FAILED) {
            return 403;
        } else if (outcome == ChannelData.Outcome.OK) {
            return 200;
        } else if (outcome == ChannelData.Outcome.CLIENT_ERROR) {
            return 400;
        } else if (outcome == ChannelData.Outcome.SEE_OTHER) {
            return 303;
        } else if (outcome == ChannelData.Outcome.SERVER_ERROR) {
            return 500;
        } else if (outcome == ChannelData.Outcome.NOT_FOUND) {
            return 404;
        } else if (outcome == ChannelData.Outcome.ACTION_NOT_ALLOWED) {
            return 405;
        } else if (outcome == ChannelData.Outcome.NOT_MODIFIED) {
            return 304;
        } else if (outcome == ChannelData.Outcome.CREATED) {
            return 201;
        }
        return 500;
    }


    private String createTarget(String request) {
        if (request.startsWith(getPath().toString())) {
            request = request.substring(getPath().toString().length());
        }
        if (request.startsWith("/")) {
            request = request.substring(1);
        }
        return request;
    }

    private static class HandlerResource extends Resource {

        private HttpServer server;

        public HandlerResource(HttpServer server, String path) {
            super(path);
            this.server = server;
        }

        public HandlerResource(HttpServer server, String path, Streamable s) {
            super(path, s);
            this.server = server;
        }

        public void onGet(RequestContext context) throws org.wspeer.http.RequestProcessException {
            ChannelData.Action action = ChannelData.Action.GET;
            ChannelData cd = new ChannelData(action);
            setupChannelData(cd, context);

            cd = server.getRequestHandler().handleRequest(cd);

            int status = server.getStatusFromOutcome(cd);
            if (status >= 400) {
                throw new RequestProcessException("Error occured", status);
            }
            context.setResponseCode(status);
            try {
                if (cd.getResponseData() != null) {
                    Streamable s = DataHandler.getStreamableForData(cd.getResponseData(), cd.getMimeType(), context);
                    if (s != null) {
                        context.setResponseEntity(s);
                    }
                }
            } catch (Exception e) {
                throw new RequestProcessException(e, -1);
            }
        }

        public void onDelete(RequestContext context) throws org.wspeer.http.RequestProcessException {
            ChannelData.Action action = ChannelData.Action.DELETE;
            ChannelData cd = new ChannelData(action);
            setupChannelData(cd, context);

            if (server.getRequestHandler() != null) {
                cd = server.getRequestHandler().handleRequest(cd);
            }

            int status = server.getStatusFromOutcome(cd);
            if (status >= 400) {
                throw new RequestProcessException("Error occured", status);
            }
            context.setResponseCode(status);
            try {
                if (cd.getResponseData() != null) {
                    Streamable s = DataHandler.getStreamableForData(cd.getResponseData(), cd.getMimeType(), context);
                    if (s != null) {
                        context.setResponseEntity(s);
                    }
                }
            } catch (Exception e) {
                throw new RequestProcessException(e, -1);
            }
        }

        public void onPost(RequestContext context) throws org.wspeer.http.RequestProcessException {
            ChannelData.Action action = ChannelData.Action.CREATE;
            ChannelData cd = new ChannelData(action);
            setupChannelData(cd, context);
            Streamable s = context.getRequestEntity();
            log.fine("HttpServer.create request entity is " + s);
            if (s != null) {
                log.fine("HttpServer.create got streamable in data");
                Object payload = DataHandler.getObjectFromStreamable(s);
                log.fine("HttpServer$HandlerResource.onPost payload:" + payload);
                cd.setRequestData(payload);
            }

            cd = server.getRequestHandler().handleRequest(cd);
            int status = server.getStatusFromOutcome(cd);
            if (status >= 400) {
                throw new RequestProcessException("Error occured", status);
            }
            context.setResponseCode(status);
            Object ret = cd.getResponseData();
            try {
                Streamable str = DataHandler.getStreamableForData(ret, cd.getMimeType(), context);
                if (str != null) {
                    context.setResponseEntity(str);
                }
            } catch (Exception e) {
                throw new RequestProcessException(e, -1);
            }

            if (cd.getLocation() != null) {
                context.setResponseHeader(Http.LOCATION, cd.getLocation());
            }
        }

        public void onPut(RequestContext context) throws org.wspeer.http.RequestProcessException {
            ChannelData.Action action = ChannelData.Action.UPDATE;
            ChannelData cd = new ChannelData(action);
            setupChannelData(cd, context);
            Streamable s = context.getRequestEntity();
            if (s != null) {
                Object payload = DataHandler.getObjectFromStreamable(s);
                cd.setRequestData(payload);
            }
            if (server.getRequestHandler() != null) {
                cd = server.getRequestHandler().handleRequest(cd);
            }

            int status = server.getStatusFromOutcome(cd);
            if (status >= 400) {
                throw new RequestProcessException("Error occured", status);
            }
            context.setResponseCode(status);
            try {
                if (cd.getResponseData() != null) {
                    s = DataHandler.getStreamableForData(cd.getResponseData(), cd.getMimeType(), context);
                    if (s != null) {
                        context.setResponseEntity(s);
                    }
                }
            } catch (Exception e) {
                throw new RequestProcessException(e, -1);
            }
        }

        protected void setupChannelData(ChannelData cd, RequestContext context) {
            cd.setRequestPath(context.getRequestPath());
            cd.setEndpoint(context.getRequestEndpoint());
            cd.setTarget(server.createTarget(context.getRequestTarget()));
            List<X509Certificate> local = context.getConnection().getConnectionContext().getLocalCertificates();
            if (local != null && local.size() > 0) {
                cd.setLocalIdentity(new X509Identity(local));
            }
            List<X509Certificate> remote = context.getConnection().getConnectionContext().getRemoteCertificates();
            if (remote != null && remote.size() > 0) {
                cd.setRemoteIdentity(new X509Identity(remote));
            }
        }
    }
}
