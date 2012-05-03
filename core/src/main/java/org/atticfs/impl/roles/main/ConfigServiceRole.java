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
import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelProperties;
import org.atticfs.channel.InChannel;
import org.atticfs.roles.AbstractServiceRole;
import org.atticfs.roles.handlers.Authenticating;
import org.atticfs.roles.handlers.ConfigRequestHandler;
import org.atticfs.util.StringConstants;

import java.io.IOException;

/**
 * Class Description Here...
 *
 * 
 */

public class ConfigServiceRole extends AbstractServiceRole implements Authenticating {

    private Controller controller;
    private boolean shutdown;

    public ConfigServiceRole(Controller controller, boolean shutdown) {
        this.controller = controller;
        this.shutdown = shutdown;
    }

    public ConfigServiceRole() {
    }

    public void init(Attic attic) throws IOException {
        super.init(attic);

        if (controller != null) {
            addChannelRequestHandler(StringConstants.CONFIG_KEY, new ConfigRequestHandler(controller, shutdown));
        } else {
            addChannelRequestHandler(StringConstants.CONFIG_KEY, new ConfigRequestHandler(attic));
        }
    }

    protected InChannel initInChannel() throws IOException {
        ChannelProperties props = new ChannelProperties();
        props.setLocalPort(getAttic().getPort());
        props.setServerContext(getPath());
        return getAttic().getChannelFactory().createInChannel(this, props);
    }


    public String getPath() {
        return "attic";
    }

    public String getAuthenticationKey(ChannelData context) {
        return StringConstants.CONFIG_KEY;
    }
}