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

import javax.net.ssl.KeyManagerFactory;

/**
 * Class Description Here...
 *
 * 
 */

public class Keystore {

    private boolean isTrustStore = false;

    private String name = "no-name";
    private String keystoreLocation = "";
    private String keystorePassword = "";
    private String alias = "";
    private String keyPassword = null;
    private String keystoreType = "JKS";
    private String algType = "SunX509";
    private String authority = "";

    /**
     * create a Keystore for accessing a certificate
     *
     * @param keystoreLocation
     * @param keystorePassword
     * @param alias
     * @param keyPassword
     */
    public Keystore(String keystoreLocation, String keystorePassword, String alias, String keyPassword) {
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.alias = alias;
        this.keyPassword = keyPassword;
        this.algType = KeyManagerFactory.getDefaultAlgorithm();
    }

    public Keystore(String keystoreLocation, String keystorePassword, String alias) {
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.alias = alias;
        this.algType = KeyManagerFactory.getDefaultAlgorithm();

    }

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getAlgType() {
        return algType;
    }

    public void setAlgType(String algType) {
        this.algType = algType;
    }

    public String getAuthority() {
        return authority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public boolean isTrustStore() {
        return isTrustStore;
    }

    public void setTrustStore(boolean trustStore) {
        isTrustStore = trustStore;
    }
}
