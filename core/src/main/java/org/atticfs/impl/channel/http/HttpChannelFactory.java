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

import org.atticfs.Attic;
import org.atticfs.channel.*;
import org.atticfs.config.security.Keystore;
import org.atticfs.config.security.SecurityChangeEvent;
import org.atticfs.config.security.SecurityChangeListener;
import org.atticfs.config.security.SecurityConfig;
import org.atticfs.ser.Serializer;
import org.atticfs.ser.SerializerFactory;
import org.wspeer.http.HttpPeer;
import org.wspeer.http.PeerProperties;
import org.wspeer.security.KeystoreDetails;
import org.wspeer.security.SecurityContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Class Description Here...
 *
 * 
 */

public class HttpChannelFactory extends ChannelFactory implements SecurityChangeListener {

    private HttpPeer peer;
    private PeerProperties props;

    public HttpChannelFactory() {
        props = new PeerProperties();
    }

    private synchronized HttpPeer getPeer() {
        if (peer == null) {
            peer = new HttpPeer();
        }
        return peer;
    }

    @Override
    public OutChannel createOutChannel(ChannelProperties properties) throws IOException {
        HttpClient client = new HttpClient(getPeer());
        client.open(properties);
        return client;
    }

    @Override
    public OutChannel createOutChannel() throws IOException {
        return new HttpClient(getPeer());
    }

    @Override
    public InChannel createInChannel(ChannelRequestHandler handler) throws IOException {
        InChannel server = new HttpServer(getPeer());
        server.setRequestHandler(handler);
        return server;
    }

    @Override
    public InChannel createInChannel(ChannelRequestHandler handler, ChannelProperties properties) throws IOException {
        InChannel server = new HttpServer(getPeer());
        server.setRequestHandler(handler);
        server.open(properties);
        return server;
    }

    public synchronized void init(Attic attic) {
        if (peer != null) {
            return;
        }
        SecurityConfig config = attic.getSecurityConfig();
        props.setHttps(config.isSecure());
        props.setConnectionIdleTimeout(attic.getDownloadConfig().getConnectionIdleTime());
        SecurityContext sc = new SecurityContext(attic.getHomeDir() + File.separator + "sec");
        sc.setRequireClientAuth(config.isRequireClientAuthentication());
        List<Keystore> keystores = config.getKeystores();
        for (Keystore k : keystores) {
            KeystoreDetails details = new KeystoreDetails(k.getKeystoreLocation(),
                    k.getKeystorePassword(),
                    k.getAlias(),
                    k.getKeyPassword());
            if (k.getAuthority() == null || k.getAuthority().length() == 0 || k.getAuthority().equalsIgnoreCase("default")) {
                details.setAuthority("default");
            } else {
                details.setAuthority(k.getAuthority());
            }
            details.setAlgType(k.getAlgType());
            String name = k.getName();
            if (name.length() == 0) {
                Random r = new Random();
                name = Long.toString(Math.abs(r.nextLong()), 36);
                k.setName(name);
            }
            if (k.isTrustStore()) {
                sc.addTrustStore(k.getName(), details);
            } else {
                sc.addKeyStore(k.getName(), details);
            }
        }

        props.setSecurityContext(sc);
        try {
            sc.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SerializerFactory.registerSerializer(new org.atticfs.impl.ser.xml.XmlSerializer());
        SerializerFactory.registerSerializer(new org.atticfs.impl.ser.json.JsonSerializer());
        SerializerFactory.registerSerializer(new org.atticfs.impl.ser.xhtml.XhtmlSerializer());

        Serializer ser = attic.getSerializer();
        if (ser == null) {
            ser = SerializerFactory.getSerializerForMime("application/json");
            attic.setSerializer(ser);
        }
        DataHandler.setSerializer(ser);
        peer = new HttpPeer(props);
    }

    public void securityChangeHappened(SecurityChangeEvent event) {
    }
}
