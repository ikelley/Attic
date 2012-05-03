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

package org.atticfs.impl.identity;

import org.atticfs.identity.Identity;
import org.atticfs.identity.IdentityRole;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class Description Here...
 *
 * 
 */

public class X509Identity implements Identity {

    private List<IdentityRole> identityRoles = new ArrayList<IdentityRole>();
    private List<Identity> authorities = new ArrayList<Identity>();
    private String name;
    private X509Certificate token;

    public X509Identity(List<X509Certificate> certificates) {
        if (certificates.size() > 0) {
            token = certificates.get(0);
            name = token.getSubjectDN().toString();
        }
        for (int count = 1; count < certificates.size(); count++) {
            X509Certificate cert = certificates.get(count);
            authorities.add(new X509Identity(cert));
        }
    }


    public void setRoles(List<IdentityRole> list) {
        this.identityRoles = list;
    }

    public X509Identity(X509Certificate certificate) {
        token = certificate;
        name = token.getSubjectDN().toString();
    }

    public List<IdentityRole> getRoles() {
        return Collections.unmodifiableList(identityRoles);
    }

    public boolean supportsRole(String role) {
        for (IdentityRole r : identityRoles) {
            if (r.getName().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public void addRole(IdentityRole identityRole) {
        if (!identityRoles.contains(identityRole)) {
            identityRoles.add(identityRole);
        }
    }

    public String getName() {
        return name;
    }

    public X509Certificate getToken() {
        return token;
    }

    public List<Identity> getAuthorities() {
        return authorities;
    }

    public boolean matches(Identity other) {
        if (other == null) {
            return false;
        }
        return getName().equals(other.getName());
    }

}
