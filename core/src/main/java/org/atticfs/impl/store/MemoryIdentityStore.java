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

package org.atticfs.impl.store;

import org.atticfs.identity.Identity;
import org.atticfs.store.IdentityStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Class Description Here...
 *
 * 
 */

public class MemoryIdentityStore implements IdentityStore {

    private Map<String, Identity> idents = new HashMap<String, Identity>();

    public Identity getIdentity(String name) {
        return idents.get(name);
    }

    public void addIdentity(Identity identity) {
        idents.put(identity.getName(), identity);
    }

    public Identity removeIdentity(String name) {
        return idents.remove(name);
    }

    public void init() {
    }

    public void shutdown() {
    }
}
