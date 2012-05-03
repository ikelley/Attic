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

package org.atticfs.types;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class Description Here...
 *
 * 
 */

public class Endpoint extends WireType {


    private String address = null;
    private int port;
    private String scheme = "";
    private String path = "";
    private String query = null;
    private String metaEndpoint = null;

    public Endpoint() {
        super(WireType.Type.Endpoint);
    }

    public Endpoint(String uri) {
        super(WireType.Type.Endpoint);
        try {
            URI u = new URI(uri);
            this.scheme = u.getScheme();
            this.address = u.getHost();
            this.port = u.getPort();
            this.path = u.getPath();
            this.query = u.getQuery();
        } catch (URISyntaxException e) {
            // probably no protocol. look for a port divider.
            int i = uri.indexOf(":");
            int slash = uri.indexOf("/");
            if (slash == -1) {
                slash = uri.length();
            }
            if (i > -1) {
                this.address = uri.substring(0, i);
                try {
                    this.port = Integer.parseInt(uri.substring(i + 1, slash));
                } catch (NumberFormatException e1) {
                }

            } else {
                this.address = uri.substring(0, slash);
            }
            if (slash < uri.length()) {
                String left = uri.substring(slash, uri.length());
                if (left.indexOf("?") > -1) {
                    this.path = left.substring(0, left.indexOf("?"));
                    this.query = left.substring(left.indexOf("?") + 1, left.length());
                    if (query.length() == 0) {
                        query = null;
                    }
                } else {
                    this.path = left;
                }
            }
        }
    }

    public Endpoint(String address, int port) {
        super(WireType.Type.Endpoint);
        setAddress(address);
        setPort(port);
    }

    public Endpoint(String scheme, String address, int port) {
        super(WireType.Type.Endpoint);
        setScheme(scheme);
        setAddress(address);
        setPort(port);
    }

    public Endpoint(String scheme, String address, int port, String path) {
        super(WireType.Type.Endpoint);
        setScheme(scheme);
        setAddress(address);
        setPort(port);
        setPath(path);
    }

    public Endpoint(String scheme, String address, int port, String path, String query) {
        super(WireType.Type.Endpoint);
        setScheme(scheme);
        setAddress(address);
        setPort(port);
        setPath(path);
        setQuery(query);
    }

    public Endpoint(String scheme, String address, int port, String path, String query, String metaEndpoint) {
        super(WireType.Type.Endpoint);
        setScheme(scheme);
        setAddress(address);
        setPort(port);
        setPath(path);
        setQuery(query);
        setMetaEndpoint(metaEndpoint);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path == null) {
            this.path = "";
        } else {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            this.path = path;
        }
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Endpoint addQuery(String key, String value) {
        Endpoint ep = new Endpoint(getScheme(), getAddress(), getPort(), getPath(), getQuery(), getMetaEndpoint());
        String query = ep.getQuery();
        if (query == null) {
            query = key + "=" + value;
        } else {
            query += "&" + key + "=" + value;
        }
        ep.setQuery(query);
        return ep;
    }

    public Endpoint appendToPath(String element) {
        if (this.path.endsWith("/")) {
            if (element.startsWith("/")) {
                element = element.substring(1, element.length());
            }
        } else {
            if (!element.startsWith("/")) {
                element = "/" + element;
            }
        }

        String path = this.path + element;
        return new Endpoint(getScheme(), getAddress(), getPort(), path, getQuery(), getMetaEndpoint());
    }

    public String getMetaEndpoint() {
        return metaEndpoint;
    }

    public void setMetaEndpoint(String metaEndpoint) {
        this.metaEndpoint = metaEndpoint;
    }

    public String toString() {
        try {
            URI uri = new URI(scheme, null, address, port, path, query, null);
            return uri.toString();
        } catch (URISyntaxException e) {
            String portStr = "";
            if (port > -1) {
                portStr = ":" + port;
            }
            String q = "";
            if (getQuery() != null) {
                q = "?" + getQuery();
            }
            return address + portStr + path + q;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endpoint endpoint = (Endpoint) o;

        if (port != endpoint.port) return false;
        if (address != null ? !address.equals(endpoint.address) : endpoint.address != null) return false;
        if (path != null ? !path.equals(endpoint.path) : endpoint.path != null) return false;
        if (query != null ? !query.equals(endpoint.query) : endpoint.query != null) return false;
        if (scheme != null ? !scheme.equals(endpoint.scheme) : endpoint.scheme != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (scheme != null ? scheme.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }
}
