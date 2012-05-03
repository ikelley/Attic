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

package org.atticfs.impl.ser.json;

import org.atticfs.types.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Class Description Here...
 *
 * 
 */

public class JDeserializer {

    @SuppressWarnings("unchecked")
    public static WireType deserialize(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Tokener jt = new Tokener(reader);
        Object val = jt.nextValue();
        if (val instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) val;
            return readFromMap(map);
        }
        throw new Exception("unexpected token:" + val);
    }

    @SuppressWarnings("unchecked")
    private static WireType readFromMap(Map<String, Object> map) throws Exception {
        if (map.keySet().size() == 1) {
            String key = map.keySet().iterator().next();
            Object o = map.get(key);
            if (o instanceof Map) {
                Map mo = (Map<String, Object>) o;
                if (key.equals(WireType.Type.DataCollection.toString())) {
                    return dataCollectionFromJson(mo);
                } else if (key.equals(WireType.Type.DataDescription.toString())) {
                    return descriptionFromJson(mo);
                } else if (key.equals(WireType.Type.FileHash.toString())) {
                    return fileHashFromJson(mo);
                } else if (key.equals(WireType.Type.Segment.toString())) {
                    return segmentFromJson(mo);
                } else if (key.equals(WireType.Type.PointerCollection.toString())) {
                    return pointerCollectionFromJson(mo);
                } else if (key.equals(WireType.Type.DataPointer.toString())) {
                    return pointerFromJson(mo);
                } else if (key.equals(WireType.Type.Endpoint.toString())) {
                    return endpointFromJson(mo);
                } else if (key.equals(WireType.Type.Constraints.toString())) {
                    return constraintsFromJson(mo);
                } else if (key.equals(WireType.Type.Constraint.toString())) {
                    return constraintFromJson(mo);
                } else if (key.equals(WireType.Type.DataAdvert.toString())) {
                    return dataAdvertFromJson(mo);
                } else if (key.equals(WireType.Type.DataQuery.toString())) {
                    return dataQueryFromJson(mo);
                }
            }
        }
        throw new Exception("JSON does not contain a single object representation");
    }

    @SuppressWarnings("unchecked")
    private static DataCollection dataCollectionFromJson(Map<String, Object> map) throws Exception {
        DataCollection dc = new DataCollection();
        if (map.keySet().size() == 1) {
            String key = map.keySet().iterator().next();
            Object o = map.get(key);
            if (o instanceof List) {
                List l = (List) o;
                for (Object o1 : l) {
                    if (o1 instanceof Map) {
                        Map m = (Map) o1;
                        DataDescription dd = descriptionFromJson(m);
                        if (dd != null) {
                            dc.addDataDescription(dd);
                        }
                    }
                }
            }
        }
        return dc;
    }

    @SuppressWarnings("unchecked")
    private static PointerCollection pointerCollectionFromJson(Map<String, Object> map) throws Exception {
        PointerCollection pc = new PointerCollection();
        if (map.keySet().size() == 1) {
            String key = map.keySet().iterator().next();
            Object o = map.get(key);
            if (o instanceof List) {
                List l = (List) o;
                for (Object o1 : l) {
                    if (o1 instanceof Map) {
                        Map m = (Map) o1;
                        DataPointer dd = pointerFromJson(m);
                        if (dd != null) {
                            pc.addDataPointer(dd);
                        }
                    }
                }
            }
        }
        return pc;
    }

    @SuppressWarnings("unchecked")
    private static DataDescription descriptionFromJson(Map<String, Object> map) throws Exception {
        DataDescription dd = new DataDescription();
        String id = (String) map.get("id");
        if (id != null) {
            dd.setId(id);
        }
        dd.setId(id);
        String name = (String) map.get("name");
        if (name != null) {
            dd.setName(name);
        }
        String proj = (String) map.get("project");
        if (proj != null) {
            dd.setProject(proj);
        }
        String desc = (String) map.get("description");
        if (desc != null) {
            dd.setDescription(desc);
        }
        String location = (String) map.get("location");
        if (location != null) {
            dd.setLocation(location);
        }
        Map<String, Object> hash = (Map<String, Object>) map.get(WireType.Type.FileHash.toString());
        if (hash != null) {
            FileHash fh = fileHashFromJson(hash);
            if (fh != null) {
                dd.setHash(fh);
            }
        }
        return dd;

    }

    @SuppressWarnings("unchecked")
    private static Endpoint endpointFromJson(Map<String, Object> map) throws Exception {
        String url = (String) map.get("url");
        if (url == null) {
            throw new IOException("no url for endpoint. Cannot go on...");
        }
        Endpoint e = new Endpoint(url);

        String meta = (String) map.get("meta");
        if (meta != null) {
            e.setMetaEndpoint(meta);
        }
        return e;
    }

    @SuppressWarnings("unchecked")
    private static DataPointer pointerFromJson(Map<String, Object> map) throws Exception {
        DataPointer dp = new DataPointer();
        Map<String, Object> dd = (Map<String, Object>) map.get(WireType.Type.DataDescription.toString());
        if (dd != null) {
            DataDescription desc = descriptionFromJson(dd);
            if (dd != null) {
                dp.setDataDescription(desc);
            }
        }
        List<Object> eps = (List<Object>) map.get(WireType.Type.Endpoint.toString());
        if (eps != null) {
            for (Object ep : eps) {
                if (ep instanceof Map) {
                    Endpoint e = endpointFromJson((Map<String, Object>) ep);
                    if (e != null) {
                        dp.addEndpoint(e);
                    }
                }
            }
        }
        return dp;
    }

    @SuppressWarnings("unchecked")
    private static FileHash fileHashFromJson(Map<String, Object> map) throws Exception {
        FileHash fh = new FileHash();
        String hash = (String) map.get("hash");
        if (hash == null) {
            throw new IOException("no hash for file hash. Cannot go on...");
        }
        fh.setHash(hash);
        Long size = (Long) map.get("size");
        if (size != null) {
            fh.setSize(size);
        }

        List<Object> chunks = (List<Object>) map.get(WireType.Type.Segment.toString());
        if (chunks != null) {
            for (Object chunk : chunks) {
                if (chunk instanceof Map) {
                    FileSegmentHash fsh = segmentFromJson((Map<String, Object>) chunk);
                    if (fsh != null) {
                        fh.addSegment(fsh);
                    }
                }
            }
        }
        return fh;
    }

    @SuppressWarnings("unchecked")
    private static DataAdvert dataAdvertFromJson(Map<String, Object> map) throws Exception {
        DataAdvert da = new DataAdvert();
        Map<String, Object> dd = (Map<String, Object>) map.get(WireType.Type.DataDescription.toString());
        if (dd != null) {
            DataDescription d = descriptionFromJson(dd);
            if (d != null) {
                da.setDataDescription(d);
            }
        }
        Map<String, Object> e = (Map<String, Object>) map.get(WireType.Type.Endpoint.toString());
        if (e != null) {
            Endpoint ep = endpointFromJson(e);
            if (ep != null) {
                da.setEndpoint(ep);
            }
        }
        Map<String, Object> c = (Map<String, Object>) map.get(WireType.Type.Constraints.toString());
        if (c != null) {
            Constraints cs = constraintsFromJson(c);
            if (cs != null) {
                da.setConstraints(cs);
            }
        }
        return da;
    }

    @SuppressWarnings("unchecked")
    private static DataQuery dataQueryFromJson(Map<String, Object> map) throws Exception {
        DataQuery dq = new DataQuery();

        Map<String, Object> e = (Map<String, Object>) map.get(WireType.Type.Endpoint.toString());
        if (e != null) {
            Endpoint ep = endpointFromJson(e);
            if (ep != null) {
                dq.setEndpoint(ep);
            }
        }
        Map<String, Object> c = (Map<String, Object>) map.get(WireType.Type.Constraints.toString());
        if (c != null) {
            Constraints cs = constraintsFromJson(c);
            if (cs != null) {
                dq.setConstraints(cs);
            }
        }
        return dq;
    }

    @SuppressWarnings("unchecked")
    private static FileSegmentHash segmentFromJson(Map<String, Object> map) throws Exception {
        FileSegmentHash fsh = new FileSegmentHash();
        String hash = (String) map.get("hash");
        if (hash == null) {
            throw new IOException("no hash for file segment hash. Cannot go on...");
        }
        Long s = (Long) map.get("start");
        if (s == null) {
            throw new IOException("no start offset for file segment hash. Cannot go on...");
        }
        Long e = (Long) map.get("end");
        if (e == null) {
            throw new IOException("no end offset for file segment hash. Cannot go on...");
        }
        fsh.setHash(hash);
        fsh.setStartOffset(s);
        fsh.setEndOffset(e);
        return fsh;
    }

    @SuppressWarnings("unchecked")
    private static Constraints constraintsFromJson(Map<String, Object> map) throws Exception {
        Constraints c = new Constraints();
        List<Object> cs = (List<Object>) map.get(WireType.Type.Constraints.toString());
        if (cs != null) {
            for (Object con : cs) {
                if (con instanceof Map) {
                    Constraint cons = constraintFromJson((Map<String, Object>) con);
                    if (cons != null) {
                        c.addConstraint(cons);
                    }
                }
            }
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    private static Constraint constraintFromJson(Map<String, Object> map) throws Exception {
        String type = (String) map.get("type");
        if (type == null) {
            type = Constraint.Type.String.toString();
        }
        String k = (String) map.get("key");
        if (k == null) {
            throw new IOException("no key for constraint. Cannot go on...");
        }
        String v = (String) map.get("value");
        if (v == null) {
            v = "";
        }
        Constraint c = new Constraint(Constraint.Type.valueOf(type), k, v);

        return c;
    }

}
