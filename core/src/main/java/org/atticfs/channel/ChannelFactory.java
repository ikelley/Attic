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

import org.atticfs.Attic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * 
 */

public abstract class ChannelFactory {

    static Logger log = Logger.getLogger("org.atticfs.channel.ChannelFactory");

    public static final String FACTORY_PROPERTY = "org.atticfs.channel.ChannelFactory";

    private static volatile ChannelFactory cf;

    public static synchronized ChannelFactory getFactory() {
        if (cf != null) {
            return cf;
        }
        String cls = System.getProperty(FACTORY_PROPERTY);
        if (cls == null) {
            InputStream in = ChannelFactory.class.getClassLoader().getResourceAsStream(FACTORY_PROPERTY);
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                try {
                    cls = reader.readLine().trim();
                    if (cls != null) {
                        Class clazz = Class.forName(cls);

                        cf = (ChannelFactory) clazz.newInstance();
                    }

                } catch (Exception e) {
                    log.fine(" could not factory load class " + cls + " message:" + e.getMessage());
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.fine("error closing stream");
                    }
                }
            }
        } else {
            try {
                Class clazz = Class.forName(cls);
                cf = (ChannelFactory) clazz.newInstance();
            } catch (Exception e) {
                log.warning(" could not factory load class " + cls + " message:" + e.getMessage());
            }
        }
        if (cf == null) {
            try {
                Class clazz = Class.forName("org.atticfs.impl.channel.http.HttpChannelFactory");
                cf = (ChannelFactory) clazz.newInstance();
            } catch (Exception e) {
                log.warning(" could not factory load default class 'org.atticfs.impl.channel.http.HttpChannelFactory' message:" + e.getMessage());
            }
        }

        return cf;
    }


    public abstract OutChannel createOutChannel(ChannelProperties properties) throws IOException;

    public abstract OutChannel createOutChannel() throws IOException;

    public abstract InChannel createInChannel(ChannelRequestHandler handler) throws IOException;

    public abstract InChannel createInChannel(ChannelRequestHandler handler, ChannelProperties properties) throws IOException;

    public abstract void init(Attic attic);


}
