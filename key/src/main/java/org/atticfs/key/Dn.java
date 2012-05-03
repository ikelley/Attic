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

package org.atticfs.key;

/**
 * produces something like:
 * EMAILADDRESS=a.b.harrison@cs.cf.ac.uk, CN=Andrew Harrison, OU=COMSC, O=Cardiff University, L=Cardiff, ST=Wales, C=UK
 *
 * 
 */

public class Dn {


    private String commonName;
    private String organization;
    private String organizationalUnit;
    private String locality;
    private String province;
    private String country;
    private String emailAddress;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getCommonName() != null && getCommonName().length() > 0) {
            sb.append("CN=").append(getCommonName()).append(", ");
        }
        if (getEmailAddress() != null && getEmailAddress().length() > 0) {
            sb.append("EMAILADDRESS=").append(getEmailAddress()).append(", ");
        }
        if (getOrganizationalUnit() != null && getOrganizationalUnit().length() > 0) {
            sb.append("OU=").append(getOrganizationalUnit()).append(", ");
        }
        if (getOrganization() != null && getOrganization().length() > 0) {
            sb.append("O=").append(getOrganization()).append(", ");
        }
        if (getLocality() != null && getLocality().length() > 0) {
            sb.append("L=").append(getLocality()).append(", ");
        }
        if (getProvince() != null && getProvince().length() > 0) {
            sb.append("ST=").append(getProvince()).append(", ");
        }
        if (getCountry() != null && getCountry().length() > 0) {
            sb.append("C=").append(getCountry());
        }
        String ret = sb.toString();
        if (ret.endsWith(", ")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }

    public String getCommonName() {
        return commonName;
    }

    public Dn setCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public Dn setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public Dn setOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
        return this;
    }

    public String getLocality() {
        return locality;
    }

    public Dn setLocality(String locality) {
        this.locality = locality;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public Dn setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public Dn setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Dn setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public static void main(String[] args) {
        Dn dn = new Dn();
        dn.setCommonName("Attic CA")
                .setOrganization("Cardiff University")
                .setOrganizationalUnit("COMSC")
                .setLocality("Cardiff")
                .setProvince("Wales")
                .setCountry("UK");
        System.out.println(dn);

    }
}
