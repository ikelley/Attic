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

package org.atticfs.config.security;

import org.atticfs.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */

public class SecurityConfig extends Config {

    private boolean secure;
    private List<Keystore> stores = new ArrayList<Keystore>();
    private boolean requireClientAuthentication;
    private boolean testMode;

    public boolean isRequireClientAuthentication() {
        return requireClientAuthentication;
    }

    public void setRequireClientAuthentication(boolean requireClientAuthentication) {
        setterCalled("setRequireClientAuthentication");
        this.requireClientAuthentication = requireClientAuthentication;
    }

    public void addKeyStore(Keystore keystore) {
        keystore.setTrustStore(false);
        stores.add(keystore);
    }

    public void addTrustStore(Keystore keystore) {
        keystore.setTrustStore(true);
        stores.add(keystore);
    }

    public List<Keystore> getKeystores() {
        return stores;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        setterCalled("setSecure");
        this.secure = secure;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }
}
