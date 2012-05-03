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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Stack;

/**
 * Class Description Here...
 *
 * 
 */
public class JSerializer {

    public final static char[] HEX = "0123456789ABCDEF".toCharArray();

    private StringBuilder builder;
    private boolean prettyPrint = false;
    private int depth = 0;

    private Stack<String> stack = new Stack<String>();

    public JSerializer() {
        this(false);
    }

    public JSerializer(boolean prettyPrint) {
        builder = new StringBuilder();
        this.prettyPrint = prettyPrint;
    }

    public String write(WireType target) {
        beginObject();
        json(target);
        endObject();
        return builder.toString();
    }

    private void pop() {
        if (stack.size() > 0) {
            stack.pop();
        }
    }

    private void push(String type) {
        stack.push(type);
    }

    private boolean peek(WireType type) {
        return peek(type.getType().toString());
    }

    private void addAttribute(WireType type) {
        addAttribute(type.getType().toString());
    }

    private boolean peek(String type) {
        if (stack.size() == 0) {
            return true;
        }
        return !stack.peek().equals(type);
    }


    private void dataDescription(DataDescription dd) {
        if (peek(dd)) {
            addAttribute(dd);
        }
        beginObject();
        add("id", dd.getId(), true);
        add("name", dd.getName());
        add("project", dd.getProject());
        add("description", dd.getDescription());
        add("location", dd.getLocation());
        if (dd.getHash() != null) {
            addComma();
            fileHash(dd.getHash());
        }
        endObject();
    }

    private void pointer(DataPointer dp) {
        if (peek(dp)) {
            addAttribute(dp);
        }
        beginObject();
        if (dp.getDataDescription() != null) {
            addAttribute(WireType.Type.DataDescription.toString());
            push(WireType.Type.DataDescription.toString());
            dataDescription(dp.getDataDescription());
            pop();
        }

        if (dp.getEndpoints().size() > 0) {
            addComma();
            addAttribute(WireType.Type.Endpoint.toString());
            push(WireType.Type.Endpoint.toString());
            array(dp.getEndpoints().iterator());
            pop();
        }
        endObject();
    }

    private void constraint(Constraint c) {
        if (peek(c)) {
            addAttribute(c);
        }
        beginObject();
        add("type", c.getConstraintType(), true);
        add("key", c.getKey());
        add("value", c.getValue());
        endObject();
    }


    private void constraints(Constraints c) {
        if (peek(c)) {
            addAttribute(c);
        }
        beginObject();
        if (c.getConstraints().size() > 0) {
            addAttribute(WireType.Type.Constraint.toString());
            push(WireType.Type.Constraint.toString());
            array(c.getConstraints().iterator());
            pop();
        }
        endObject();
    }

    private void dataAdvert(DataAdvert da) {
        if (peek(da)) {
            addAttribute(da);
        }
        beginObject();
        if (da.getDataDescription() != null) {
            addAttribute(WireType.Type.DataDescription.toString());
            push(WireType.Type.DataDescription.toString());
            dataDescription(da.getDataDescription());
            pop();
        }
        if (da.getEndpoint() != null) {
            if (da.getDataDescription() != null) {
                addComma();
            }
            addAttribute(WireType.Type.Endpoint.toString());
            push(WireType.Type.Endpoint.toString());
            endpoint(da.getEndpoint());
            pop();
        }
        if (da.getConstraints().size() > 0) {
            if (da.getEndpoint() != null) {
                addComma();
            }
            addAttribute(WireType.Type.Constraints.toString());
            push(WireType.Type.Constraints.toString());
            constraints(da.getConstraints());
            pop();
        }
        endObject();
    }

    private void dataQuery(DataQuery dq) {
        if (peek(dq)) {
            addAttribute(dq);
        }
        beginObject();

        if (dq.getEndpoint() != null) {
            addAttribute(WireType.Type.Endpoint.toString());
            push(WireType.Type.Endpoint.toString());
            endpoint(dq.getEndpoint());
            pop();
        }
        if (dq.getConstraints().size() > 0) {
            if (dq.getEndpoint() != null) {
                addComma();
            }
            addAttribute(WireType.Type.Constraints.toString());
            push(WireType.Type.Constraints.toString());
            constraints(dq.getConstraints());
            pop();
        }
        endObject();
    }

    private void dataCollection(DataCollection dc) {
        if (peek(dc)) {
            addAttribute(dc);
        }
        beginObject();
        if (dc.getDataDescriptions().size() > 0) {
            addAttribute(WireType.Type.DataDescription.toString());
            push(WireType.Type.DataDescription.toString());
            array(dc.getDataDescriptions().iterator());
            pop();
        }
        endObject();
    }

    private void pointerCollection(PointerCollection pc) {
        if (peek(pc)) {
            addAttribute(pc);
        }
        beginObject();
        if (pc.getDataPointers().size() > 0) {
            addAttribute(WireType.Type.DataPointer.toString());
            push(WireType.Type.DataPointer.toString());
            array(pc.getDataPointers().iterator());
            pop();
        }
        endObject();
    }

    private void fileHash(FileHash fh) {
        if (peek(fh)) {
            addAttribute(fh);
        }
        beginObject();
        add("hash", fh.getHash(), true);
        add("size", fh.getSize());
        if (fh.getChunks().size() > 0) {
            addComma();
            addAttribute(WireType.Type.Segment.toString());
            push(WireType.Type.Segment.toString());
            array(fh.getChunks().iterator());
            pop();
        }
        endObject();
    }

    private void fileSegment(FileSegmentHash fsh) {
        if (peek(fsh)) {
            addAttribute(fsh);
        }
        beginObject();
        add("hash", fsh.getHash(), true);
        add("start", fsh.getStartOffset());
        add("end", fsh.getEndOffset());
        endObject();
    }

    private void endpoint(Endpoint endpoint) {
        if (peek(endpoint)) {
            addAttribute(endpoint);
        }
        beginObject();
        add("url", endpoint.toString(), true);
        if (endpoint.getMetaEndpoint() != null) {
            add("meta", endpoint.getMetaEndpoint());
        }
        endObject();
    }

    private void json(Object object) {
        if (object == null) add("null");
        else if (object instanceof String)
            string(object);
        else if (object instanceof Number)
            add(object);
        else if (object instanceof Boolean)
            string(object.toString());
        else if (object.getClass().isArray())
            array(object);
        else if (object instanceof Iterable)
            array(((Iterable) object).iterator());
        else if (object instanceof FileHash) {
            fileHash((FileHash) object);
        } else if (object instanceof FileSegmentHash) {
            fileSegment((FileSegmentHash) object);
        } else if (object instanceof DataDescription) {
            dataDescription((DataDescription) object);
        } else if (object instanceof DataCollection) {
            dataCollection((DataCollection) object);
        } else if (object instanceof DataPointer) {
            pointer((DataPointer) object);
        } else if (object instanceof PointerCollection) {
            pointerCollection((PointerCollection) object);
        } else if (object instanceof Constraints) {
            constraints((Constraints) object);
        } else if (object instanceof Constraint) {
            constraint((Constraint) object);
        } else if (object instanceof DataAdvert) {
            dataAdvert((DataAdvert) object);
        } else if (object instanceof DataQuery) {
            dataQuery((DataQuery) object);
        } else if (object instanceof Endpoint) {
            endpoint((Endpoint) object);
        }
    }

    private void array(Iterator it) {
        beginArray();
        while (it.hasNext()) {
            if (prettyPrint) {
                addNewline();
            }
            addArrayElement(it.next(), it.hasNext());
        }
        endArray();
    }

    private void array(Object object) {
        beginArray();
        int length = Array.getLength(object);
        for (int i = 0; i < length; ++i) {
            if (prettyPrint) {
                addNewline();
            }
            addArrayElement(Array.get(object, i), i < length - 1);
        }
        endArray();
    }

    private void addArrayElement(Object object, boolean notLast) {
        int len = builder.length();
        json(object);
        if (len < builder.length()) { // make sure we at least added an element.
            if (notLast) add(',');
        }
    }

    private void string(Object obj) {
        String value = "null";
        if (obj != null) {
            value = obj.toString();
        }

        add('\"');
        int last = 0;
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == '"') {
                last = add(value, last, i, "\\\"");
            } else if (c == '\\') {
                last = add(value, last, i, "\\\\");
            } else if (c == '\b') {
                last = add(value, last, i, "\\b");
            } else if (c == '\f') {
                last = add(value, last, i, "\\f");
            } else if (c == '\n') {
                last = add(value, last, i, "\\n");
            } else if (c == '\r') {
                last = add(value, last, i, "\\r");
            } else if (c == '\t') {
                last = add(value, last, i, "\\t");
            } else if (Character.isISOControl(c)) {
                last = add(value, last, i) + 1;
                unicode(c);
            }
        }
        if (last < value.length()) {
            add(value, last, value.length());
        }
        add('\"');
    }

    private int add(String value, int begin, int end) {
        builder.append(value, begin, end);
        return end;
    }

    private int add(String value, int begin, int end, String append) {
        builder.append(value, begin, end);
        builder.append(append);
        return end + 1;
    }


    protected void addComma() {
        add(',');
    }

    protected void beginObject() {
        if (prettyPrint) {
            indent(depth);
            depth++;
        }
        add('{');
    }

    protected void beginObject(String key) {
        if (prettyPrint) {
            indent(depth);
            depth++;
        }
        add('{');
        builder.append("\"");
        builder.append(key);
        builder.append("\"");
        builder.append(":");
        if (prettyPrint) {
            builder.append(" ");
        }
        add('{');

    }

    protected void beginArray(String key) {
        if (prettyPrint) {
            indent(depth);
            depth++;
        }

        builder.append("\"");
        builder.append(key);
        builder.append("\"");
        builder.append(":");
        if (prettyPrint) {
            builder.append(" ");
        }
        add('[');

    }

    protected void endObject() {
        if (prettyPrint) {
            addNewline();
            depth--;
            indent(depth);
        }
        add('}');
    }

    private void beginArray() {
        if (prettyPrint) {
            depth++;
        }
        add('[');

    }

    private void endArray() {
        if (prettyPrint) {
            addNewline();
            depth--;
            indent(depth);
        }
        add(']');
    }

    protected void add(char c) {
        builder.append(c);
    }

    private void indent(int depth) {
        for (int i = 0; i < depth; i++) {
            builder.append("    ");
        }
    }

    private void addNewline() {
        builder.append("\n");
    }

    protected void add(Object value) {
        builder.append(value);
    }

    protected void add(String key, Object value, boolean isFirst) {
        int start = builder.length();
        if (!isFirst) {
            addComma();
        }
        addAttribute(key);

        int len = builder.length();
        json(value);
        if (len == builder.length()) {
            builder.delete(start, len); // erase the attribute key we didn't output anything.
        }
    }

    protected void add(String key, Object value) {
        add(key, value, false);
    }

    private void addAttribute(Object key) {
        if (prettyPrint) {
            addNewline();
            indent(depth);
        }
        builder.append("\"");
        builder.append(key);
        builder.append("\"");
        builder.append(":");
        if (prettyPrint) {
            builder.append(" ");
        }
    }

    private void unicode(char c) {
        add("\\u");
        int n = c;
        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xf000) >> 12;
            add(HEX[digit]);
            n <<= 4;
        }
    }

}
