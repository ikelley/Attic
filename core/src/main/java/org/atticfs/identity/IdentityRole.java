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

package org.atticfs.identity;

import java.util.Properties;

/**
 * Class Description Here...
 *
 * 
 */

public class IdentityRole {

    private String name;
    private Properties properties = new Properties();

    public IdentityRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
