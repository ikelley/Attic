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

package org.atticfs.impl.ser.xhtml;

import org.atticfs.ser.Serializer;
import org.atticfs.types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wspeer.html.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * reading from the stream presumes that only a single wire type is embedded in the html.
 * If more than one are present, one will be used to build the wire type. Which one, in terms
 * of its appearance in the html cannot be guaranteed.
 *
 * 
 */

public class XhtmlSerializer implements Serializer {

    public static final String ATTR_CLS = "class";
    // html does not support namespaces. This is a prefix added to all class names used by attic
    public static final String CLS_PREFIX = "attic-";
    public static final String MIME_XHTML = "application/xhtml+xml";
    public static final String MIME_HTML = "text/html";

    public static final String ADVERT = WireType.Type.DataAdvert.toString().toLowerCase();
    public static final String QUERY = WireType.Type.DataQuery.toString().toLowerCase();
    public static final String DESC = WireType.Type.DataDescription.toString().toLowerCase();

    public static final String POINTER = WireType.Type.DataPointer.toString().toLowerCase();
    public static final String FILEHASH = WireType.Type.FileHash.toString().toLowerCase();
    public static final String SEGMENT = WireType.Type.Segment.toString().toLowerCase();
    public static final String POINTER_COLL = WireType.Type.PointerCollection.toString().toLowerCase();
    public static final String ENDPOINT = WireType.Type.Endpoint.toString().toLowerCase();
    public static final String CONSTRAINTS = WireType.Type.Constraints.toString().toLowerCase();
    public static final String CONSTRAINT = WireType.Type.Constraint.toString().toLowerCase();
    public static final String DATA_COLL = WireType.Type.DataCollection.toString().toLowerCase();


    private static String[] types = {
            CLS_PREFIX + ADVERT,
            CLS_PREFIX + QUERY,
            CLS_PREFIX + DESC,
            CLS_PREFIX + POINTER,
            CLS_PREFIX + FILEHASH,
            CLS_PREFIX + SEGMENT,
            CLS_PREFIX + POINTER_COLL,
            CLS_PREFIX + ENDPOINT,
            CLS_PREFIX + CONSTRAINTS,
            CLS_PREFIX + CONSTRAINT,
            CLS_PREFIX + DATA_COLL
    };

    public void setFeature(String feature, String value) {
    }

    public void toStream(WireType type, OutputStream out) throws IOException {
        Html html = new Html("Attic Metadata");
        html.addStylesheet("http://www.atticfs.org/styles.css");
        Container c = null;
        if (type instanceof DataDescription) {
            c = descriptionToHtml((DataDescription) type);
        } else if (type instanceof DataPointer) {
            c = pointerToHtml((DataPointer) type);
        } else if (type instanceof PointerCollection) {
            c = pointersToHtml((PointerCollection) type);
        } else if (type instanceof DataCollection) {
            c = descriptionsToHtml((DataCollection) type);
        } else if (type instanceof FileHash) {
            c = filehashToHtml((FileHash) type);
        } else if (type instanceof FileSegmentHash) {
            c = segmentToHtml((FileSegmentHash) type);
        } else if (type instanceof DataAdvert) {
            c = advertToHtml((DataAdvert) type);
        } else if (type instanceof DataQuery) {
            c = queryToHtml((DataQuery) type);
        } else if (type instanceof Constraints) {
            c = constraintsToHtml((Constraints) type);
        } else if (type instanceof Endpoint) {
            c = endpointToHtml((Endpoint) type);
        } else if (type instanceof Constraint) {
            c = constraintToHtml((Constraint) type);
        }
        if (c != null) {
            html.getBody().addChild(c);
        } else {
            // add error message
            Div div = new Div();
            div.addChild(new TextContainer("h3", "Error parsing message type"));
            html.getBody().addChild(div);
        }
        String content = HtmlWriter.writeComponent(html);
        out.write(content.getBytes());
        out.flush();

    }

    public WireType fromStream(InputStream in) throws Exception {
        Document doc = newDocument(in);
        Element root = doc.getDocumentElement();
        Map<String, List<Element>> map = findElements(root, types);
        if (map.keySet().size() < 1) {
            return null;
        }
        String first = map.keySet().iterator().next();
        Element e = getMappedElement(map, first);
        if (e != null) {
            return parse(first, e);
        }
        return null;
    }

    private WireType parse(String type, Element root) {
        if (type.equals(CLS_PREFIX + ADVERT)) {
            return advertFromElement(root);
        } else if (type.equals(CLS_PREFIX + QUERY)) {
            return queryFromElement(root);
        } else if (type.equals(CLS_PREFIX + DESC)) {
            return descriptionFromElement(root);
        } else if (type.equals(CLS_PREFIX + POINTER)) {
            return pointerFromElement(root);
        } else if (type.equals(CLS_PREFIX + POINTER_COLL)) {
            return pointersFromElement(root);
        } else if (type.equals(CLS_PREFIX + FILEHASH)) {
            return filehashFromElement(root);
        } else if (type.equals(CLS_PREFIX + SEGMENT)) {
            return segmentFromElement(root);
        } else if (type.equals(CLS_PREFIX + ENDPOINT)) {
            return endpointFromElement(root);
        } else if (type.equals(CLS_PREFIX + CONSTRAINTS)) {
            return constraintsFromElement(root);
        } else if (type.equals(CLS_PREFIX + CONSTRAINT)) {
            return constraintFromElement(root);
        } else if (type.equals(CLS_PREFIX + DATA_COLL)) {
            return descriptionsFromElement(root);
        }

        return null;
    }

    private DataAdvert advertFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + DESC,
                CLS_PREFIX + CONSTRAINTS, CLS_PREFIX + ENDPOINT);
        Element e = getMappedElement(nodes, CLS_PREFIX + DESC);
        if (e == null) {
            return null;
        }
        DataDescription dd = descriptionFromElement(e);
        if (dd == null) {
            return null;
        }
        Element ep = getMappedElement(nodes, CLS_PREFIX + ENDPOINT);
        if (ep == null) {
            return null;
        }
        Endpoint end = endpointFromElement(ep);
        if (end == null) {
            return null;
        }
        DataAdvert advert = new DataAdvert(dd, end);
        Element es = getMappedElement(nodes, CLS_PREFIX + CONSTRAINTS);
        if (es != null) {
            Constraints cs = constraintsFromElement(es);
            if (cs != null) {
                advert.setConstraints(cs);
            }
        }
        return advert;
    }


    private Container advertToHtml(DataAdvert advert) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + ADVERT);
        div.addChild(new TextContainer("h3", "Data Advert"));
        DataDescription dd = advert.getDataDescription();
        if (dd != null) {
            div.addChild(descriptionToHtml(dd));
        }
        Endpoint ep = advert.getEndpoint();
        if (ep != null) {
            div.addChild(endpointToHtml(ep));
        }
        Constraints cs = advert.getConstraints();
        if (cs.size() > 0) {
            div.addChild(constraintsToHtml(cs));
        }

        return div;
    }

    private DataQuery queryFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + CONSTRAINTS,
                CLS_PREFIX + ENDPOINT);
        Element e = getMappedElement(nodes, CLS_PREFIX + ENDPOINT);
        DataQuery advert = new DataQuery();
        if (e != null) {
            Endpoint ep = endpointFromElement(e);
            advert.setEndpoint(ep);
        }
        Element es = getMappedElement(nodes, CLS_PREFIX + CONSTRAINTS);
        if (es != null) {
            Constraints cs = constraintsFromElement(es);
            if (cs != null) {
                advert.setConstraints(cs);
            }
        }
        return advert;
    }


    private Container queryToHtml(DataQuery query) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + QUERY);
        div.addChild(new TextContainer("h3", "Data Query"));
        Endpoint ep = query.getEndpoint();
        if (ep != null) {
            div.addChild(endpointToHtml(ep));
        }
        Constraints cs = query.getConstraints();
        if (cs.size() > 0) {
            div.addChild(constraintsToHtml(cs));
        }

        return div;
    }

    private DataDescription descriptionFromElement(Element root) {
        DataDescription dd = new DataDescription();
        Map<String, List<Element>> nodes = findElements(root,
                CLS_PREFIX + "id",
                CLS_PREFIX + "name",
                CLS_PREFIX + "description",
                CLS_PREFIX + "location",
                CLS_PREFIX + "project",
                CLS_PREFIX + FILEHASH);
        String id = getMappedString(nodes, CLS_PREFIX + "id");
        if (id != null) {
            dd.setId(id);
        }
        String name = getMappedString(nodes, CLS_PREFIX + "name");
        if (name != null) {
            dd.setName(name);
        }
        String desc = getMappedString(nodes, CLS_PREFIX + "description");
        if (desc != null) {
            dd.setDescription(desc);
        }
        String proj = getMappedString(nodes, CLS_PREFIX + "project");
        if (proj != null) {
            dd.setProject(proj);
        }
        String loc = getMappedString(nodes, CLS_PREFIX + "location");
        if (loc != null) {
            dd.setLocation(loc);
        }
        Element el = getMappedElement(nodes, CLS_PREFIX + FILEHASH);
        if (el != null) {
            FileHash fh = filehashFromElement(el);
            if (fh != null) {
                dd.setHash(fh);
            }
        }
        return dd;
    }


    private Container descriptionToHtml(DataDescription desc) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + DESC);
        div.addChild(new TextContainer("h3", "File Metadata"));
        addPara("Data identifier:", CLS_PREFIX + "id", desc.getId(), div);
        if (desc.getName() != null)
            addPara("Data name:", CLS_PREFIX + "name", desc.getName(), div);
        if (desc.getProject() != null)
            addPara("Associated project:", CLS_PREFIX + "project", desc.getProject(), div);
        if (desc.getDescription() != null)
            addPara("Data description:", CLS_PREFIX + "description", desc.getDescription(), div);
        if (desc.getLocation() != null)
            addPara("Data location:", CLS_PREFIX + "location", desc.getLocation(), div);
        if (desc.getHash() != null) {
            Container c = filehashToHtml(desc.getHash());
            div.addChild(c);
        }
        return div;
    }

    private DataPointer pointerFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + DESC,
                CLS_PREFIX + ENDPOINT);
        Element e = getMappedElement(nodes, CLS_PREFIX + DESC);
        if (e == null) {
            return null;
        }
        DataDescription dd = descriptionFromElement(e);
        if (dd == null) {
            return null;
        }
        List<Element> es = getMappedElements(nodes, CLS_PREFIX + ENDPOINT);
        if (es.size() == 0) {
            return null;
        }
        DataPointer dp = new DataPointer(dd);
        for (Element element : es) {
            Endpoint ep = endpointFromElement(element);
            if (ep != null) {
                dp.addEndpoint(ep);
            }
        }
        if (dp.getEndpoints().size() == 0) {
            return null;
        }
        return dp;
    }


    private Container pointerToHtml(DataPointer pointer) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + POINTER);
        div.addChild(new TextContainer("h3", "Data Pointer"));
        if (pointer.getDataDescription() != null) {
            div.addChild(descriptionToHtml(pointer.getDataDescription()));
        }
        List<Endpoint> eps = pointer.getEndpoints();
        for (Endpoint ep : eps) {
            div.addChild(endpointToHtml(ep));
        }
        return div;
    }

    private FileHash filehashFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + "hash",
                CLS_PREFIX + "size", CLS_PREFIX + SEGMENT);
        String hash = getMappedString(nodes, CLS_PREFIX + "hash");
        String size = getMappedString(nodes, CLS_PREFIX + "size");
        List<Element> segs = getMappedElements(nodes, CLS_PREFIX + SEGMENT);
        FileHash fh = new FileHash();

        if (hash != null && size != null) {
            try {
                fh.setHash(hash);
                fh.setSize(Long.parseLong(size));
                for (Element seg : segs) {
                    FileSegmentHash fsh = segmentFromElement(seg);
                    if (fsh == null) {
                        return null;
                    }
                    fh.addSegment(fsh);
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return fh;
    }

    private Container filehashToHtml(FileHash fh) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + FILEHASH);
        div.addChild(new TextContainer("h3", "File Details"));
        addPara("MD5 hash of data:", CLS_PREFIX + "hash", fh.getHash(), div);
        addPara("Data size:", CLS_PREFIX + "size", fh.getSize() + "", div);

        List<FileSegmentHash> segs = fh.getChunks();
        for (FileSegmentHash seg : segs) {
            Container c = segmentToHtml(seg);
            div.addChild(c);
        }
        return div;
    }

    private void addPara(String desc, String cls, String value, Container parent) {
        Span p = new Span(desc + " ");
        p.addClass(CLS_PREFIX + "field");
        Span span = new Span(value);
        span.addClass(cls);
        span.addClass(CLS_PREFIX + "field-val");
        p.addChild(span);
        parent.addChild(p);
    }


    private FileSegmentHash segmentFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + "hash",
                CLS_PREFIX + "start", CLS_PREFIX + "end");
        String hash = getMappedString(nodes, CLS_PREFIX + "hash");
        String start = getMappedString(nodes, CLS_PREFIX + "start");
        String end = getMappedString(nodes, CLS_PREFIX + "end");
        if (hash != null && start != null && end != null) {
            try {
                return new FileSegmentHash(hash, Long.parseLong(start), Long.parseLong(end));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }


    private Container segmentToHtml(FileSegmentHash fsh) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + SEGMENT);
        div.addChild(new TextContainer("h4", "File Segment Details"));
        addPara("MD5 hash of segment:", CLS_PREFIX + "hash", fsh.getHash(), div);
        addPara("Segment start offset (in bytes):", CLS_PREFIX + "start", fsh.getStartOffset() + "", div);
        addPara("Segment end offset (in bytes):", CLS_PREFIX + "end", fsh.getEndOffset() + "", div);
        return div;
    }

    private Endpoint endpointFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + "url",
                CLS_PREFIX + "meta");
        String url = getMappedString(nodes, CLS_PREFIX + "url");
        if (url == null) {
            return null;
        }
        Endpoint ep = new Endpoint(url);
        String meta = getMappedString(nodes, CLS_PREFIX + "meta");
        if (meta != null) {
            ep.setMetaEndpoint(meta);
        }
        return ep;
    }


    private Container endpointToHtml(Endpoint endpoint) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + ENDPOINT);
        div.addChild(new TextContainer("h4", "Endpoint"));
        addPara("Data URL:", CLS_PREFIX + "url", endpoint.toString(), div);
        if (endpoint.getMetaEndpoint() != null) {
            addPara("Metadata URL (you can use a 'filehash=' query or a 'description=' query on this endpoint):", CLS_PREFIX + "meta", endpoint.getMetaEndpoint(), div);
        }
        return div;
    }

    private Constraints constraintsFromElement(Element root) {
        Constraints c = new Constraints();
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + CONSTRAINT);
        List<Element> els = getMappedElements(nodes, CLS_PREFIX + CONSTRAINT);
        for (Element el : els) {
            Constraint con = constraintFromElement(el);
            if (con != null) {
                c.addConstraint(con);
            }
        }
        return c;
    }

    private Container constraintsToHtml(Constraints cs) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + CONSTRAINTS);
        div.addChild(new TextContainer("h4", "Constraints"));
        List<Constraint> c = cs.getConstraints();
        for (Constraint constraint : c) {
            div.addChild(constraintToHtml(constraint));
        }
        return div;
    }

    private Constraint constraintFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + "type",
                CLS_PREFIX + "key", CLS_PREFIX + "value");
        String type = getMappedString(nodes, CLS_PREFIX + "type");
        String key = getMappedString(nodes, CLS_PREFIX + "key");
        String val = getMappedString(nodes, CLS_PREFIX + "value");
        if (key == null || val == null) {
            return null;
        }
        Constraint.Type t = null;
        if (type != null) {
            t = Constraint.Type.valueOf(type);
        }
        if (t == null) {
            t = Constraint.Type.String;
        }
        return new Constraint(t, key, val);
    }


    private Container constraintToHtml(Constraint c) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + CONSTRAINT);
        addPara("Constraint Type:", CLS_PREFIX + "type", c.getType().toString(), div);
        addPara("Constraint Key:", CLS_PREFIX + "key", c.getKey(), div);
        addPara("Constraint Value:", CLS_PREFIX + "value", c.getValue(), div);

        return div;
    }

    private PointerCollection pointersFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + POINTER);
        PointerCollection pc = new PointerCollection();
        List<Element> ps = getMappedElements(nodes, CLS_PREFIX + POINTER);
        for (Element p : ps) {
            DataPointer dp = pointerFromElement(p);
            if (dp != null) {
                pc.addDataPointer(dp);
            }
        }
        return pc;
    }


    private Container pointersToHtml(PointerCollection coll) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + POINTER_COLL);
        div.addChild(new TextContainer("h3", "Data Pointer Collection"));

        List<DataPointer> ps = coll.getDataPointers();
        for (DataPointer p : ps) {
            Container c = pointerToHtml(p);
            div.addChild(c);
        }
        return div;
    }

    private DataCollection descriptionsFromElement(Element root) {
        Map<String, List<Element>> nodes = findElements(root, CLS_PREFIX + DESC);
        DataCollection pc = new DataCollection();
        List<Element> ps = getMappedElements(nodes, CLS_PREFIX + DESC);
        for (Element p : ps) {
            DataDescription dd = descriptionFromElement(p);
            if (dd != null) {
                pc.addDataDescription(dd);
            }
        }
        return pc;
    }


    private Container descriptionsToHtml(DataCollection coll) {
        Div div = new Div();
        div.addClass(CLS_PREFIX + DATA_COLL);
        div.addChild(new TextContainer("h3", "Data Description Collection"));

        List<DataDescription> dds = coll.getDataDescriptions();
        for (DataDescription dd : dds) {
            Container c = descriptionToHtml(dd);
            div.addChild(c);
        }
        return div;
    }

    public String getMimeType() {
        return MIME_HTML;
    }

    public String getFileExtension() {
        return "attich";
    }

    protected boolean containsClass(String classname, String attribute) {
        if (attribute == null || attribute.trim().length() == 0) {
            return false;
        }
        String[] classes = attribute.split("\\s");
        for (String aClass : classes) {
            if (aClass.equals(classname)) {
                return true;
            }
        }
        return false;
    }

    private String getMappedString(Map<String, List<Element>> map, String classname) {
        List<Element> l = map.get(classname);
        if (l == null || l.size() < 1) {
            return null;
        }
        return l.get(0).getTextContent();
    }

    private Element getMappedElement(Map<String, List<Element>> map, String classname) {
        List<Element> l = map.get(classname);
        if (l == null || l.size() < 1) {
            return null;
        }
        return l.get(0);
    }

    private List<Element> getMappedElements(Map<String, List<Element>> map, String classname) {
        List<Element> l = map.get(classname);
        if (l == null || l.size() < 1) {
            return new ArrayList<Element>();
        }
        return l;
    }

    protected boolean containsClass(String classname, Element e) {
        String cls = e.getAttribute(ATTR_CLS);
        return containsClass(classname, cls);
    }

    protected String[] getClasses(String attribute) {
        if (attribute.trim().length() == 0) {
            return new String[0];
        }
        return attribute.split("\\s");
    }


    protected Element findElement(String classname, Element root) {
        if (containsClass(classname, root)) {
            return root;
        }
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Element c = (Element) n;
                return findElement(classname, c);
            }
        }
        return null;
    }

    /**
     * returns a map
     *
     * @param classnames
     * @param root
     * @return
     */
    protected Map<String, List<Element>> findElements(Element root, String... classnames) {
        Map<String, List<Element>> ret = new HashMap<String, List<Element>>();
        boolean found = false;
        for (String classname : classnames) {
            if (containsClass(classname, root)) {
                List<Element> curr = ret.get(classname);
                if (curr == null) {
                    curr = new ArrayList<Element>();
                }
                curr.add(root);
                ret.put(classname, curr);
                found = true;
            }
        }
        if (!found) {
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element c = (Element) n;
                    Map<String, List<Element>> curr = findElements(c, classnames);
                    Set<String> keys = curr.keySet();
                    for (String key : keys) {
                        List<Element> val = curr.get(key);
                        List<Element> existing = ret.get(key);
                        if (existing == null) {
                            ret.put(key, val);
                        } else {
                            existing.addAll(val);
                            ret.put(key, existing);
                        }
                    }
                }
            }
        }
        return ret;
    }

    protected String findElementValue(String classname, Element root) {
        Element e = findElement(classname, root);
        if (e != null) {
            return e.getTextContent();
        }
        return null;
    }

    private Document newDocument(InputStream stream) throws IOException {
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(stream);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXParseException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return doc;
    }


    public static void main(String[] args) throws Exception {
        DataDescription dd = new DataDescription();
        dd.setName("my name");
        dd.setProject("my project");
        dd.setDescription("my description");
        FileHash fh = new FileHash();
        fh.setHash("1234567890abcdef");
        fh.setSize(123456789);
        for (int i = 0; i < 10; i++) {
            FileSegmentHash fsh = new FileSegmentHash("1234" + i, i, i + 10);
            fh.addSegment(fsh);
        }
        dd.setHash(fh);
        XhtmlSerializer ser = new XhtmlSerializer();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ser.toStream(dd, bout);
        System.out.println(new String(bout.toByteArray()));

        ByteArrayInputStream bin = new ByteArrayInputStream(xhtml.getBytes());
        WireType wt = ser.fromStream(bin);
        System.out.println("read in " + wt);
        if (wt instanceof DataDescription) {
            DataDescription desc = (DataDescription) wt;
            System.out.println("id:" + desc.getId());
            System.out.println("name:" + desc.getName());
            System.out.println("project:" + desc.getProject());
            System.out.println("desc:" + desc.getDescription());
            fh = desc.getHash();
            System.out.println("FH hash:" + fh.getHash());
            System.out.println("FH size:" + fh.getSize());
            List<FileSegmentHash> segs = fh.getChunks();
            for (FileSegmentHash seg : segs) {
                System.out.println("Segment hash:" + seg.getHash());
                System.out.println("Segment start:" + seg.getStartOffset());
                System.out.println("Segment end:" + seg.getEndOffset());
            }

        }

    }

    private static String xhtml = "<html>\n" +
            "<head>\n" +
            "    <meta http-equiv=\"content-type\" content=\"text/xhtml; charset=iso-8859-1\"/>\n" +
            "    <title>no title</title>\n" +
            "    <link href=\"http://www.atticfs.org/styles.css\" rel=\"stylesheet\" type=\"text/css\"/>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div class=\"attic-datadescription\">\n" +
            "    <p>Data identifier<span class=\"attic-id\">9ebcb4b0-8ea4-4e60-b716-948fdd016271</span></p>\n" +
            "\n" +
            "    <p>Data name<span class=\"attic-name\">my name</span></p>\n" +
            "\n" +
            "    <p>Associated project<span class=\"attic-project\">my project</span></p>\n" +
            "\n" +
            "    <p>Data description<span class=\"attic-description\">my description</span></p>\n" +
            "\n" +
            "    <div class=\"attic-filehash\"><p>MD5 hash of data<span class=\"attic-hash\">1234567890abcdef</span></p>\n" +
            "\n" +
            "        <p>Data size<span class=\"attic-size\">123456789</span></p>\n" +
            "\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12340</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">0</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">10</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12341</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">1</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">11</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12342</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">2</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">12</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12343</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">3</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">13</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12344</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">4</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">14</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12345</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">5</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">15</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12346</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">6</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">16</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12347</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">7</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">17</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12348</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">8</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">18</span></p></div>\n" +
            "        <div class=\"attic-segment\"><p>MD5 hash of segment<span class=\"attic-hash\">12349</span></p>\n" +
            "\n" +
            "            <p>Segment start offset (bytes)<span class=\"attic-start\">9</span></p>\n" +
            "\n" +
            "            <p>Segment end offset (bytes)<span class=\"attic-end\">19</span></p></div>\n" +
            "    </div>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>\n" +
            "";


}
