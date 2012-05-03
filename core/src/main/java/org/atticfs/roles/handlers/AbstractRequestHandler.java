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

package org.atticfs.roles.handlers;

import org.atticfs.channel.ChannelData;
import org.atticfs.channel.ChannelRequestHandler;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public abstract class AbstractRequestHandler implements ChannelRequestHandler {

    protected static Logger log = Logger.getLogger("org.atticfs.roles.handlers");


    public ChannelData handleRequest(ChannelData context) {
        ChannelData.Action action = context.getAction();
        if (action == ChannelData.Action.GET) {
            return handleGet(context);
        } else if (action == ChannelData.Action.CREATE) {
            return handleCreate(context);
        } else if (action == ChannelData.Action.MESSAGE) {
            return handleMessage(context);
        } else if (action == ChannelData.Action.UPDATE) {
            return handleUpdate(context);
        } else if (action == ChannelData.Action.DELETE) {
            return handleDelete(context);
        }
        return null;
    }

    protected ChannelData handleGet(ChannelData context) {
        context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
        return context;
    }

    protected ChannelData handleCreate(ChannelData context) {
        context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
        return context;
    }

    protected ChannelData handleMessage(ChannelData context) {
        context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
        return context;
    }

    protected ChannelData handleUpdate(ChannelData context) {
        context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
        return context;
    }

    protected ChannelData handleDelete(ChannelData context) {
        context.setOutcome(ChannelData.Outcome.ACTION_NOT_ALLOWED);
        return context;
    }


    public abstract String getPath();

    protected Object loadResource(String prefix, String path) {
        int ind = path.indexOf(prefix);
        if (ind > -1) {
            path = path.substring(ind + prefix.length());
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        return in;

    }
}
