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

import org.atticfs.config.Config;
import org.atticfs.roles.Role;
import org.wspeer.html.annotation.Form;

/**
 * Class Description Here...
 *
 * 
 */

@Form(name = "htmlconf")
public abstract class Configurable extends Config {

    /**
     * return a name for the configuration. This can one or two words - but don't get too verbose.
     * It's not a description.
     */
    public abstract String getName();

    public abstract void configure(Role role);
}
