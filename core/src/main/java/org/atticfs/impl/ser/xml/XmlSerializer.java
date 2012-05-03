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

package org.atticfs.impl.ser.xml;

import org.atticfs.ser.Serializer;
import org.atticfs.types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class XmlSerializer implements Serializer {

    static Logger log = Logger.getLogger("org.atticfs.impl.ser.xml.XmlSerializer");

    public static final String NS_ATTIC = "http://atticfs.org";

    private boolean indent = false;

    public void setFeature(String feature, String value) {
        if (feature.equals(PRETTY_PRINT) && value.equalsIgnoreCase("true")) {
            this.indent = true;
        }
    }

    public void toStream(WireType wt, OutputStream out) throws Exception {

        Document doc = newDocument();
        if (wt instanceof DataDescription) {
            doc.appendChild(descriptionToElement((DataDescription) wt, doc));
        } else if (wt instanceof DataCollection) {
            doc.appendChild(collectionToElement((DataCollection) wt, doc));
        } else if (wt instanceof PointerCollection) {
            doc.appendChild(pointersToElement((PointerCollection) wt, doc));
        } else if (wt instanceof DataPointer) {
            doc.appendChild(pointerToElement((DataPointer) wt, doc));
        } else if (wt instanceof FileHash) {
            doc.appendChild(fileHashToElement((FileHash) wt, doc));
        } else if (wt instanceof FileSegmentHash) {
            doc.appendChild(segmentToElement((FileSegmentHash) wt, doc));
        } else if (wt instanceof Endpoint) {
            doc.appendChild(endpointToElement((Endpoint) wt, doc));
        } else if (wt instanceof DataAdvert) {
            doc.appendChild(dataAdvertToElement((DataAdvert) wt, doc));
        } else if (wt instanceof DataQuery) {
            doc.appendChild(dataQueryToElement((DataQuery) wt, doc));
        } else if (wt instanceof Constraints) {
            doc.appendChild(constraintsToElement((Constraints) wt, doc));
        }
        transform(doc, out, indent);
    }

    public WireType fromStream(InputStream in) throws Exception {
        Document doc = newDocument(in);
        return fromDocument(doc);
    }

    public String getMimeType() {
        return "text/xml";
    }

    public String getFileExtension() {
        return "atticx";
    }

    private WireType fromDocument(Document doc) throws IOException {
        Element root = doc.getDocumentElement();
        if (root.getNamespaceURI().equals(NS_ATTIC)) {
            String nc = root.getLocalName();
            if (nc.equals(WireType.Type.DataDescription.toString())) {
                return descriptionFromElement(root);
            } else if (nc.equals(WireType.Type.DataCollection.toString())) {
                return collectionFromElement(root);
            } else if (nc.equals(WireType.Type.PointerCollection.toString())) {
                return pointersFromElement(root);
            } else if (nc.equals(WireType.Type.FileHash.toString())) {
                return fileHashFromElement(root);
            } else if (nc.equals(WireType.Type.Segment.toString())) {
                return segmentFromElement(root);
            } else if (nc.equals(WireType.Type.DataPointer.toString())) {
                return pointerFromElement(root);
            } else if (nc.equals(WireType.Type.DataAdvert.toString())) {
                return dataAdvertFromElement(root);
            } else if (nc.equals(WireType.Type.DataQuery.toString())) {
                return dataQueryFromElement(root);
            } else if (nc.equals(WireType.Type.Endpoint.toString())) {
                return endpointFromElement(root);
            } else if (nc.equals(WireType.Type.Constraints.toString())) {
                return constraintsFromElement(root);
            } else {
                throw new IOException("unknown document type:" + nc);
            }
        } else {
            throw new IOException("unknown document type:" + root.getNamespaceURI());
        }
    }

    private DataCollection collectionFromElement(Element el) throws IOException {
        DataCollection dd = new DataCollection();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals(WireType.Type.DataDescription.toString())) {
                dd.addDataDescription(descriptionFromElement(e));
            }
        }
        return dd;
    }


    private Element collectionToElement(DataCollection dd, Document doc) throws IOException {

        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.DataCollection.toString());
        List<DataDescription> datas = dd.getDataDescriptions();
        for (DataDescription data : datas) {
            root.appendChild(descriptionToElement(data, doc));
        }
        return root;
    }

    private PointerCollection pointersFromElement(Element el) throws IOException {
        PointerCollection pc = new PointerCollection();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals(WireType.Type.DataPointer.toString())) {
                pc.addDataPointer(pointerFromElement(e));
            }
        }
        return pc;
    }


    private Element pointersToElement(PointerCollection pc, Document doc) throws IOException {
        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.PointerCollection.toString());
        List<DataPointer> dps = pc.getDataPointers();
        for (DataPointer dp : dps) {
            root.appendChild(pointerToElement(dp, doc));
        }
        return root;
    }

    private DataDescription descriptionFromElement(Element el) throws IOException {
        DataDescription dd = new DataDescription();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals("id")) {
                dd.setId(e.getTextContent().trim());
            } else if (nc.equals("name")) {
                dd.setName(e.getTextContent().trim());
            } else if (nc.equals("project")) {
                dd.setProject(e.getTextContent().trim());
            } else if (nc.equals("description")) {
                dd.setDescription(e.getTextContent().trim());
            } else if (nc.equals(WireType.Type.FileHash.toString())) {
                dd.setHash(fileHashFromElement(e));
            }
        }
        return dd;
    }

    private Element descriptionToElement(DataDescription dd, Document doc) throws IOException {

        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.DataDescription.toString());

        Element curr = doc.createElementNS(NS_ATTIC, "id");
        curr.appendChild(doc.createTextNode(dd.getId()));
        root.appendChild(curr);
        if (dd.getName() != null) {
            curr = doc.createElementNS(NS_ATTIC, "name");
            curr.appendChild(doc.createTextNode(dd.getName()));
            root.appendChild(curr);
        }
        if (dd.getProject() != null) {
            curr = doc.createElementNS(NS_ATTIC, "project");
            curr.appendChild(doc.createTextNode(dd.getProject()));
            root.appendChild(curr);
        }
        if (dd.getDescription() != null) {
            curr = doc.createElementNS(NS_ATTIC, "description");
            curr.appendChild(doc.createTextNode(dd.getDescription()));
            root.appendChild(curr);
        }


        if (dd.getHash() != null) {
            root.appendChild(fileHashToElement(dd.getHash(), doc));
        }
        return root;
    }

    private FileSegmentHash segmentFromElement(Element el) throws IOException {
        FileSegmentHash seg = new FileSegmentHash();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals("start")) {
                seg.setStartOffset(Long.parseLong(e.getTextContent().trim()));
            } else if (nc.equals("end")) {
                seg.setEndOffset(Long.parseLong(e.getTextContent().trim()));
            } else if (nc.equals("hash")) {
                seg.setHash(e.getTextContent().trim());
            }
        }
        return seg;
    }

    private Element segmentToElement(FileSegmentHash seg, Document doc) throws IOException {

        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.Segment.toString());
        if (seg.getHash() != null) {
            Element curr = doc.createElementNS(NS_ATTIC, "hash");
            curr.appendChild(doc.createTextNode(seg.getHash()));
            root.appendChild(curr);
        }
        if (seg.getStartOffset() != -1) {
            Element curr = doc.createElementNS(NS_ATTIC, "start");
            curr.appendChild(doc.createTextNode(seg.getStartOffset() + ""));
            root.appendChild(curr);
        }
        if (seg.getEndOffset() != -1) {
            Element curr = doc.createElementNS(NS_ATTIC, "end");
            curr.appendChild(doc.createTextNode(seg.getEndOffset() + ""));
            root.appendChild(curr);
        }
        return root;
    }

    private Element endpointToElement(Endpoint endpoint, Document doc) throws IOException {
        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.Endpoint.toString());
        Element curr = doc.createElementNS(NS_ATTIC, "url");
        curr.appendChild(doc.createTextNode(endpoint.toString()));
        root.appendChild(curr);
        if (endpoint.getMetaEndpoint() != null) {
            curr = doc.createElementNS(NS_ATTIC, "meta");
            curr.appendChild(doc.createTextNode(endpoint.getMetaEndpoint()));
            root.appendChild(curr);
        }
        return root;
    }

    private Endpoint endpointFromElement(Element el) throws IOException {
        String ep = null;
        String meta = null;
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals("url")) {
                ep = e.getTextContent().trim();
            } else if (nc.equals("meta")) {
                meta = e.getTextContent().trim();
            }
        }
        if (ep != null) {
            Endpoint end = new Endpoint(ep);
            if (meta != null) {
                end.setMetaEndpoint(meta);
            }
            return end;
        }
        return null;
    }

    private Element constraintToElement(Constraint c, Document doc) throws IOException {
        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.Constraint.toString());
        root.setAttribute("type", c.getConstraintType().toString());

        Element curr = doc.createElementNS(NS_ATTIC, c.getKey());
        curr.appendChild(doc.createTextNode(c.getValue()));
        root.appendChild(curr);
        return root;
    }

    private Constraint constraintFromElement(Element el) throws IOException {
        NodeList nl = el.getChildNodes();
        String key = null;
        String value = null;
        Constraint.Type type = Constraint.Type.String;
        String typeVal = el.getAttribute("type");
        if (typeVal != null) {
            Constraint.Type currType = Constraint.Type.valueOf(typeVal);
            if (currType != null) {
                type = currType;
            }
        }

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            key = e.getLocalName();
            value = e.getTextContent().trim();

        }
        Constraint c = new Constraint(type, key, value);

        return c;
    }

    private Element constraintsToElement(Constraints c, Document doc) throws IOException {
        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.Constraints.toString());
        List<Constraint> cs = c.getConstraints();
        for (Constraint constraint : cs) {
            root.appendChild(constraintToElement(constraint, doc));
        }
        return root;
    }

    private Constraints constraintsFromElement(Element el) throws IOException {
        Constraints c = new Constraints();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals(WireType.Type.Constraint.toString())) {
                c.addConstraint(constraintFromElement(e));
            }
        }
        return c;
    }

    private Element dataAdvertToElement(DataAdvert advert, Document doc) throws IOException {
        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.DataAdvert.toString());
        if (advert.getDataDescription() != null) {
            root.appendChild(descriptionToElement(advert.getDataDescription(), doc));
        }
        if (advert.getEndpoint() != null) {
            root.appendChild(endpointToElement(advert.getEndpoint(), doc));
        }
        Constraints cs = advert.getConstraints();
        if (cs != null && cs.getConstraints().size() > 0) {
            root.appendChild(constraintsToElement(cs, doc));
        }
        return root;
    }

    private DataAdvert dataAdvertFromElement(Element el) throws IOException {
        DataAdvert advert = new DataAdvert();

        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals(WireType.Type.Constraints.toString())) {
                Constraints cs = constraintsFromElement(e);
                if (cs != null) {
                    advert.setConstraints(cs);
                }
            } else if (nc.equals(WireType.Type.DataDescription.toString())) {
                advert.setDataDescription(descriptionFromElement(e));
            } else if (nc.equals(WireType.Type.Endpoint.toString())) {
                advert.setEndpoint(endpointFromElement(e));
            }
        }
        return advert;
    }

    private Element dataQueryToElement(DataQuery query, Document doc) throws IOException {
        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.DataQuery.toString());
        if (query.getEndpoint() != null) {
            root.appendChild(endpointToElement(query.getEndpoint(), doc));
        }

        Constraints cs = query.getConstraints();
        if (cs != null && cs.getConstraints().size() > 0) {
            root.appendChild(constraintsToElement(cs, doc));
        }
        return root;
    }

    private DataQuery dataQueryFromElement(Element el) throws IOException {
        DataQuery query = new DataQuery();

        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals(WireType.Type.Constraints.toString())) {
                Constraints cs = constraintsFromElement(e);
                if (cs != null) {
                    query.setConstraints(cs);
                }
            } else if (nc.equals(WireType.Type.Endpoint.toString())) {
                query.setEndpoint(endpointFromElement(e));
            }
        }
        return query;
    }

    private FileHash fileHashFromElement(Element el) throws IOException {
        FileHash seg = new FileHash();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals("defaultChunkSize")) {
                seg.setDefaultChunkSize(Long.parseLong(e.getTextContent().trim()));
            } else if (nc.equals("hash")) {
                seg.setHash(e.getTextContent().trim());
            } else if (nc.equals("size")) {
                seg.setSize(Long.parseLong(e.getTextContent().trim()));
            } else if (nc.equals(WireType.Type.Segment.toString())) {
                seg.addSegment(segmentFromElement(e));
            }
        }
        return seg;
    }

    private Element fileHashToElement(FileHash seg, Document doc) throws IOException {

        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.FileHash.toString());
        if (seg.getHash() != null) {
            Element curr = doc.createElementNS(NS_ATTIC, "hash");
            curr.appendChild(doc.createTextNode(seg.getHash()));
            root.appendChild(curr);
        }
        if (seg.getDefaultChunkSize() != -1) {
            Element curr = doc.createElementNS(NS_ATTIC, "defaultChunkSize");
            curr.appendChild(doc.createTextNode(seg.getDefaultChunkSize() + ""));
            root.appendChild(curr);
        }
        if (seg.getSize() > 0) {
            Element curr = doc.createElementNS(NS_ATTIC, "size");
            curr.appendChild(doc.createTextNode(seg.getSize() + ""));
            root.appendChild(curr);
        }
        if (seg.getNumChunks() > 0) {
            List<FileSegmentHash> hashes = seg.getChunks();
            for (FileSegmentHash hash : hashes) {
                root.appendChild(segmentToElement(hash, doc));
            }

        }
        return root;
    }


    private DataPointer pointerFromElement(Element el) throws IOException {
        DataPointer dp = new DataPointer();
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            String ns = e.getNamespaceURI();
            String nc = e.getLocalName();
            if (nc.equals(WireType.Type.DataDescription.toString())) {
                dp.setDataDescription(descriptionFromElement(e));
            } else if (nc.equals(WireType.Type.Endpoint.toString())) {

                dp.addEndpoint(endpointFromElement(e));
                log.fine("XmlSerializer.pointerFromElement ADDING ENDPOINT:" + dp.getEndpoints());
            } else {
                log.fine("XmlSerializer.pointerFromElement local name is:" + nc);
            }
        }
        return dp;
    }

    private Element pointerToElement(DataPointer dp, Document doc) throws IOException {
        Element root = doc.createElementNS(NS_ATTIC, WireType.Type.DataPointer.toString());
        if (dp.getDataDescription() != null) {
            root.appendChild(descriptionToElement(dp.getDataDescription(), doc));
        }
        if (dp.getEndpoints().size() > 0) {
            List<Endpoint> endpoints = dp.getEndpoints();
            for (Endpoint endpoint : endpoints) {
                log.fine("XmlSerializer.pointerToElement ADDED ENDPOINT " + endpoint);
                root.appendChild(endpointToElement(endpoint, doc));
            }
        }
        return root;
    }

    private static Document newDocument() throws Exception {
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
        return doc;
    }

    private static Document newDocument(InputStream stream) throws IOException {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }


    private StreamResult transform(Document doc, OutputStream out, boolean indent) throws IOException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        try {
            t = tf.newTransformer();
            if (indent)
                t.setOutputProperty(OutputKeys.INDENT, "yes");
            else
                t.setOutputProperty(OutputKeys.INDENT, "no");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        } catch (TransformerConfigurationException tce) {
            assert (false);
        }
        DOMSource doms = new DOMSource(doc);
        StreamResult sr = new StreamResult(out);
        try {
            t.transform(doms, sr);
        } catch (TransformerException te) {
            throw new IOException(te.getMessage());
        }
        return sr;
    }

    public static void main(String[] args) throws Exception {
        DataDescription desc = new DataDescription("1234");
        desc.setName("desc name");
        desc.setDescription("a short description of data");
        FileHash fh = new FileHash();
        fh.setSize(100);

        fh.setHash("fh-hash");
        for (int i = 0; i < 10; i++) {
            long offset = i * 10;
            FileSegmentHash fsh = new FileSegmentHash("hash" + 1, offset, offset + 9);
            fh.addSegment(fsh);
        }

        desc.setHash(fh);

        DataPointer dp = new DataPointer();
        dp.setDataDescription(desc);
        dp.addEndpoint(new Endpoint("http://198.162.0.1:8080"));
        dp.addEndpoint(new Endpoint("http://198.162.0.2:9090"));
        XmlSerializer ser = new XmlSerializer();
        ser.toStream(dp, System.out);
    }

}
