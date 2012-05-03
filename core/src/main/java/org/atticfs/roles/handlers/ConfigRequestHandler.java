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

import org.atticfs.Attic;
import org.atticfs.channel.ChannelData;
import org.atticfs.config.html.Configurable;
import org.atticfs.config.html.HtmlConfig;
import org.atticfs.impl.roles.main.Controller;
import org.atticfs.util.FileUtils;
import org.atticfs.util.StringConstants;
import org.wspeer.html.BlockContainer;
import org.wspeer.html.Div;
import org.wspeer.html.HtmlWriter;
import org.wspeer.html.P;
import org.wspeer.html.annotation.AnnotationException;
import org.wspeer.html.annotation.FormProcessor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */

public class ConfigRequestHandler extends AbstractRequestHandler {

    private HtmlConfig config;
    private Controller controller;
    private boolean shutdownOnSubmit = false;
    private List<Configurable> configs;


    public ConfigRequestHandler(Controller controller, boolean shutdown) {
        this.controller = controller;
        this.configs = controller.getAdics().getConfigurables();
        this.config = new HtmlConfig(controller.getAdics(), configs);
        this.shutdownOnSubmit = shutdown;

    }

    public ConfigRequestHandler(Attic attic) {
        this.configs = attic.getConfigurables();
        this.config = new HtmlConfig(attic, configs);
    }

    public ConfigRequestHandler(Controller controller) {
        this(controller, false);
    }

    protected ChannelData handleGet(ChannelData context) {
        String path = context.getRequestPath();
        if (path.endsWith("styles.css")) {
            InputStream in = getClass().getClassLoader().getResourceAsStream("styles.css");
            if (in != null) {
                context.setResponseData(in);
                context.setMimeType("text/css");
                context.setOutcome(ChannelData.Outcome.OK);
            } else {
                context.setOutcome(ChannelData.Outcome.SERVER_ERROR);
            }
        } else if (path.endsWith("script.js")) {
            InputStream in = getClass().getClassLoader().getResourceAsStream("script.js");
            if (in != null) {
                context.setResponseData(in);
                context.setMimeType("text/javascript");
                context.setOutcome(ChannelData.Outcome.OK);
            } else {
                context.setOutcome(ChannelData.Outcome.SERVER_ERROR);
            }
        } else {
            String html;
            try {
                html = config.getHtml();
            } catch (Exception e) {
                html = error(e);
            }
            context.setResponseData(html);
            context.setMimeType("text/html");
            context.setOutcome(ChannelData.Outcome.OK);
        }
        return context;
    }

    protected ChannelData handleCreate(ChannelData context) {
        String path = context.getRequestPath();
        Object data = context.getRequestData();
        if (data == null) {
            context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
        } else {
            String params = null;
            if (data instanceof byte[]) {
                params = new String((byte[]) data);
            } else if (data instanceof String) {
                params = (String) data;
            }
            FormProcessor fp = new FormProcessor();
            if (path.endsWith("submit")) {
                try {

                    BlockContainer bc = fp.process(config, new ArrayList(configs), params);

                    context.setMimeType("text/html");
                    if (shutdownOnSubmit && controller != null) {
                        controller.shutdown();
                        context.setResponseData(response(bc, true));
                    } else {
                        //context.setOutcome(ChannelData.Outcome.SEE_OTHER);
                        //context.setLocation(getRedirect(context));
                        context.setResponseData(response(bc, false));
                    }
                    context.setOutcome(ChannelData.Outcome.OK);
                } catch (AnnotationException e) {
                    context.setResponseData(error(e));
                    context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
                }
            } else {
                context.setOutcome(ChannelData.Outcome.CLIENT_ERROR);
            }
        }
        return context;
    }

    private String getRedirect(ChannelData context) {
        String all = context.getEndpoint().toString();
        String server = all.substring(0, all.indexOf(getPath()));
        return server + getPath();
    }

    public String getPath() {
        return StringConstants.CONFIG_KEY;
    }


    private String response(BlockContainer bc, boolean shuttingDown) {
        org.wspeer.html.Html html = new org.wspeer.html.Html("Attic Configuration");
        html.addStylesheet("./config/styles.css");
        BlockContainer body = html.getBody();
        Div div = new Div();
        div.addChild(new P("Thank you for submitting your configuration."));
        if (shuttingDown) {
            div.addChild(new P("This service has now closed down. The next time you start Attic, you will launch the service you chose."));
        }
        body.addChild(div);
        body.addChild(bc);

        return HtmlWriter.writeComponent(html);
    }

    private String error(Throwable t) {
        org.wspeer.html.Html html = new org.wspeer.html.Html("Attic Configuration");
        html.addStylesheet("./config/styles.css");
        BlockContainer body = html.getBody();
        body.addChild(new P("There was a problem submitting your configuration:"));
        body.addChild(new P(FileUtils.formatThrowable(t)));
        return HtmlWriter.writeComponent(html);
    }


}
