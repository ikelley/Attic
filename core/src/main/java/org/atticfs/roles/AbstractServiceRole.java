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

package org.atticfs.roles;

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelRequestHandler;
import org.atticfs.channel.InChannel;
import org.atticfs.identity.Identity;
import org.atticfs.identity.IdentityRole;
import org.atticfs.impl.identity.DnIdentity;
import org.atticfs.impl.store.MemoryIdentityStore;
import org.atticfs.roles.handlers.Authenticating;
import org.atticfs.store.IdentityStore;
import org.atticfs.types.Endpoint;
import org.atticfs.util.UriUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public abstract class AbstractServiceRole extends AbstractRole implements ServiceRole, ChannelRequestHandler {

    protected static Logger log = Logger.getLogger("org.atticfs.roles");

    private InChannel inChannel;
    private Map<String, ChannelRequestHandler> handlers = new ConcurrentHashMap<String, ChannelRequestHandler>();
    private Map<String, ChannelRequestHandler> types = new ConcurrentHashMap<String, ChannelRequestHandler>();
    protected IdentityStore identityStore;

    /**
     * initialize the Role. Subclasses should call this if they override it.
     * super(init)
     *
     * @param attic
     * @throws IOException
     */
    public void init(Attic attic) throws IOException {
        super.init(attic);
        if (identityStore == null) {
            identityStore = new MemoryIdentityStore();
        }
        inChannel = initInChannel();
    }

    protected abstract InChannel initInChannel() throws IOException;

    public void shutdown() throws IOException {
        if (inChannel != null) {
            inChannel.close();
        }
    }

    public void addChannelRequestHandler(String type, ChannelRequestHandler handler) {
        handlers.put(getHandlerPath(handler), handler);
        types.put(type, handler);
    }

    public void removeChannelRequestHandler(String type, ChannelRequestHandler handler) {
        handlers.remove(getHandlerPath(handler));
        types.remove(type);
    }

    protected ChannelRequestHandler getHandlerForType(String type) {
        return types.get(type);
    }

    public IdentityStore getIdentityStore() {
        return identityStore;
    }

    public void setIdentityStore(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    public ChannelData handleRequest(ChannelData context) {
        ChannelRequestHandler handler = findHandler(context);
        if (handler != null) {
            if (handler instanceof Authenticating) {
                Authenticating ahandler = (Authenticating) handler;
                String authKey = ahandler.getAuthenticationKey(context);
                log.fine("current auth key is:" + authKey);
                if (authKey != null) {
                    Identity ident = context.getRemoteIdentity();
                    if (ident != null) {
                        log.fine(" name of identity connecting:" + ident.getName());
                        Identity i = identityStore.getIdentity(ident.getName());
                        if (i != null) {
                            log.fine("found matching stored identity.");
                            List<IdentityRole> roles = i.getRoles();
                            for (IdentityRole role : roles) {
                                log.fine("supported role:" + role.getName());
                            }
                            boolean authed = i.supportsRole(authKey);
                            if (!authed) {
                                log.fine("stored identity does not support the role:" + authKey);
                                context.setOutcome(ChannelData.Outcome.AUTHENTICATION_FAILED);
                                return context;
                            } else {
                                log.fine("stored identity supports role:" + authKey);
                                context.setAuthorized(true);
                                context.setOutcome(ChannelData.Outcome.OK);

                            }
                        } else {
                            context.setOutcome(ChannelData.Outcome.AUTHENTICATION_FAILED);
                            return context;
                        }
                    }
                }
                context.setAuthenticationAction(authKey);
            }
            return handler.handleRequest(context);
        } else {
            context.setOutcome(ChannelData.Outcome.NOT_FOUND);
            return context;
        }
    }

    public Endpoint getEndpoint() {
        return inChannel.getEndpoint().appendToPath(getPath());
    }

    public Endpoint getHandlerEndpoint(ChannelRequestHandler handler) {
        return getEndpoint().appendToPath(handler.getPath());
    }


    protected String getHandlerPath(ChannelRequestHandler handler) {
        return UriUtils.appendPath(getPath(), handler.getPath());
    }

    protected ChannelRequestHandler findHandler(ChannelData context) {
        String path = context.getRequestPath();
        log.fine("path:" + path);
        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }
        if (path.indexOf("?") > -1) {
            path = path.substring(0, path.indexOf("?"));
        }
        String s = extractSubstring(path, 0, "/");
        if (s == null) {
            s = path;
        }
        ChannelRequestHandler target = handlers.get(s);
        log.fine("got intial target for path " + s + ":" + target);
        while (true) {
            if (s.equals(path)) {
                break;
            }
            s = extractSubstring(path, s.length() + 1, "/");

            ChannelRequestHandler other = handlers.get(s);
            log.fine("got next target for path " + s + ":" + target);

            if (other != null) {
                target = other;
            }
        }
        log.fine(" returning target " + target);
        return target;
    }

    public void addIdentity(String name, String... roles) {
        DnIdentity ident = new DnIdentity(name);
        log.fine("created new identity with name:" + name);
        for (String role : roles) {
            IdentityRole ir = new IdentityRole(role);
            log.fine("adding role to identity:" + role);
            ident.addRole(ir);
        }
        getIdentityStore().addIdentity(ident);
    }

    public boolean removeIdentity(String name) {
        Identity i = getIdentityStore().removeIdentity(name);
        if (i == null) {
            return false;
        }
        return true;
    }

    protected String extractSubstring(String endpoint, int idx, String delim) {
        int i = endpoint.indexOf(delim, idx);
        if (i == -1) {
            return endpoint;
        }
        return endpoint.substring(0, i);
    }
}
