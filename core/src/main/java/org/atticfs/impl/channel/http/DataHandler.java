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

package org.atticfs.impl.channel.http;

import org.atticfs.ser.Serializer;
import org.atticfs.ser.SerializerFactory;
import org.atticfs.types.WireType;
import org.atticfs.util.FileUtils;
import org.w3c.dom.Document;
import org.wspeer.http.RequestContext;
import org.wspeer.http.util.MimeHandler;
import org.wspeer.http.util.TempFileManager;
import org.wspeer.streamable.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class Description Here...
 *
 * 
 */

public class DataHandler {

    static Logger log = Logger.getLogger("org.atticfs.impl.channel.http.DataHandler");


    private static Serializer ser = null;

    protected static void setSerializer(Serializer serial) {
        ser = serial;
    }

    public static Streamable getStreamableForData(Object data, String mime, RequestContext context) throws Exception {
        if (ser == null) {
            throw new RuntimeException("No serializer defined for me!");
        }
        if (mime == null) {
            mime = "text/plain";
        }

        Streamable s = null;
        if (data instanceof WireType) {
            // for receiving
            context.setAcceptTypes(ser.getMimeType(), "*/*");

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            String[] mimes = SerializerFactory.getRegisteredMimeTypes();
            // for sending
            String chosen = context.getBestMimeType(mimes);
            log.fine("chose  mime type:" + chosen);
            Serializer serial = ser;
            if (chosen != null && chosen.length() > 0) {
                serial = SerializerFactory.getSerializerForMime(chosen);
            }
            serial.toStream((WireType) data, bout);
            s = new StreamableData(bout.toByteArray(), serial.getMimeType());
        } else if (data instanceof byte[]) {
            s = new StreamableData((byte[]) data, mime);
        } else if (data instanceof InputStream) {
            s = new StreamableStream((InputStream) data, mime);
        } else if (data instanceof String) {
            s = new StreamableString((String) data, mime);
        } else if (data instanceof File) {
            s = new StreamableRandomAccessFile((File) data, mime);
        } else if (data instanceof Streamable) {
            s = (Streamable) data;
        } else if (data instanceof Serializable) {
            s = new StreamableObject((Serializable) data);
        }
        return s;
    }

    public static Object getResponseObject(Class type, Streamable s) throws Exception {
        if (s == null) {
            return null;
        }
        if (type == null || type.equals(Object.class)) {
            return s.getContent();
        }
        if (WireType.class.isAssignableFrom(type) && s.getMimeType() != null) {
            Serializer serial = SerializerFactory.getSerializerForMime(s.getMimeType());
            if (serial != null) {
                return serial.fromStream(s.getInputStream());
            }
        }
        if (byte[].class.equals(type)) {
            if (s instanceof StreamableData) {
                return s.getContent();
            } else {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                s.writeTo(bout);
                return bout.toByteArray();
            }
        } else if (InputStream.class.isAssignableFrom(type)) {
            return s.getInputStream();
        } else if (String.class.isAssignableFrom(type)) {
            if (s instanceof StreamableString) {
                return s.getContent();
            } else if (s instanceof StreamableData) {
                return new String((byte[]) s.getContent());
            } else {
                // todo file

                return s.getContent();
            }
        } else if (Streamable.class.isAssignableFrom(type)) {
            return s;
        } else if (File.class.isAssignableFrom(type)) {
            if (s instanceof StreamableFile) {
                return s.getContent();
            } else {
                List<String> exts = MimeHandler.getExtensions(s.getMimeType());
                String ext = "dat";
                if (exts != null && exts.size() > 0) {
                    ext = exts.get(0);
                }
                File f = TempFileManager.createTempFile("download", ext);
                s.writeTo(new FileOutputStream(f));
                return f;
            }
        } else if (Serializable.class.isAssignableFrom(type)) {
            if (s instanceof StreamableObject) {
                return s.getContent();
            } else {
                StreamableObject so = new StreamableObject(null);
                so.readFrom(s);
                return so.getContent();
            }
        }
        return s.getContent();
    }

    public static Object getObjectFromStreamable(Streamable s) {

        String mime = s.getMimeType();
        if (mime == null) {
            mime = "text/plain";
        }
        log.fine("got mime type of streamable:" + mime);
        Serializer serial = SerializerFactory.getSerializerForMime(mime);
        if (serial != null) {
            Streamable temp;
            temp = new StreamableData(s.getMimeType());
            try {
                temp.readFrom(s);
            } catch (IOException e) {
                return null;
            }
            temp = s;
            try {
                log.fine("DataHandler.getObjectFromStreamable about to deserialize");
                return serial.fromStream(s.getInputStream());
            } catch (Exception e) {
                log.fine("exception thrown during de serialization:" + FileUtils.formatThrowable(e));
            }
        }
        if (mime.equals(StreamableData.BYTES_MIME)) {
            return s.getContent();
        } else if (mime.equals("text/xml")) {
            try {
                return newDocument(s.getInputStream());
            } catch (IOException e) {
                log.fine("exception thrown during de serialization:" + FileUtils.formatThrowable(e));
                return null;
            }
        } else if (mime.equals("text/plain") || mime.equals("text/html") || mime.equals("application/json")) {
            if (s instanceof StreamableString) {
                return s.getContent();
            } else if (s instanceof StreamableData) {
                return new String((byte[]) s.getContent());
            } else {
                return s.getContent();
            }
        } else if (mime.equals(StreamableObject.SERIALIZABLE_MIME)) {
            if (s instanceof StreamableObject) {
                return s.getContent();
            } else {
                StreamableObject so = new StreamableObject();
                try {
                    so.readFrom(s);
                    return so.getContent();
                } catch (IOException e) {
                    return null;
                }
            }
        } else {
            return s.getContent();
        }
    }

    public static Document newDocument(InputStream stream) throws IOException {
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

}
