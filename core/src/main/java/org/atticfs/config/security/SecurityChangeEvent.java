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

import java.util.*;

/**
 * Class Description Here...
 *
 * 
 */

public class SecurityChangeEvent extends EventObject {

    private List<Keystore> addedKeystores = new ArrayList<Keystore>();
    private List<Keystore> removedKeystores = new ArrayList<Keystore>();

    private Map<String, List<String>> addedAuthorizedDNs = new HashMap<String, List<String>>();
    private Map<String, List<String>> removedAuthorizedDNs = new HashMap<String, List<String>>();


    public SecurityChangeEvent(Object o) {
        super(o);
    }

    public void addNewKeystore(Keystore keystore) {
        addedKeystores.add(keystore);
    }

    public void addRemoveKeystore(Keystore keystore) {
        removedKeystores.add(keystore);
    }

    public void addNewDN(String action, String dn) {
        List<String> l = addedAuthorizedDNs.get(action);
        if (l == null) {
            l = new ArrayList<String>();
        }
        l.add(dn);
        addedAuthorizedDNs.put(action, l);
    }

    public void addRemoveDN(String action, String dn) {
        List<String> l = removedAuthorizedDNs.get(action);
        if (l == null) {
            l = new ArrayList<String>();
        }
        l.add(dn);
        removedAuthorizedDNs.put(action, l);
    }

    public List<Keystore> getAddedKeystores() {
        return addedKeystores;
    }

    public List<Keystore> getRemovedKeystores() {
        return removedKeystores;
    }

    public Map<String, List<String>> getAddedAuthorizedDNs() {
        return addedAuthorizedDNs;
    }

    public Map<String, List<String>> getRemovedAuthorizedDNs() {
        return removedAuthorizedDNs;
    }
}
