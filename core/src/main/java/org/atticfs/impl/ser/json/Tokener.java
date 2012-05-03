/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.atticfs.impl.ser.json;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class Description Here...
 *
 * 
 */

public class Tokener {


    private int index;
    private Reader reader;
    private char lastChar;
    private boolean useLastChar;


    /**
     * Construct a JSONTokener from a string.
     *
     * @param reader A reader.
     */
    public Tokener(Reader reader) {
        this.reader = reader.markSupported() ?
                reader : new BufferedReader(reader);
        this.useLastChar = false;
        this.index = 0;
    }


    /**
     * Construct a JSONTokener from a string.
     *
     * @param s A source string.
     */
    public Tokener(String s) {
        this(new StringReader(s));
    }


    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can roleservices for a digit or letter before attempting to parse
     * the next number or identifier.
     *
     * @throws IOException if you try and step back twice it will throw this exception
     */
    public void back() throws IOException {
        if (useLastChar || index <= 0) {
            throw new IOException("Stepping back two steps is not supported");
        }
        index -= 1;
        useLastChar = true;
    }


    /**
     * Get the hex value of a character (base16).
     *
     * @param c A character between '0' and '9' or between 'A' and 'F' or
     *          between 'a' and 'f'.
     * @return An int between 0 and 15, or -1 if c was not a hex digit.
     */
    public static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if (c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }


    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     *
     * @return true if not yet at the end of the source.
     * @throws IOException thrown if underlying IOException is thrown.
     */
    public boolean more() throws IOException {
        char nextChar = next();
        if (nextChar == 0) {
            return false;
        }
        back();
        return true;
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     * @throws IOException if underlying IOException is thrown.
     */
    public char next() throws IOException {
        if (this.useLastChar) {
            this.useLastChar = false;
            if (this.lastChar != 0) {
                this.index += 1;
            }
            return this.lastChar;
        }
        int c;
        c = this.reader.read();

        if (c <= 0) { // End of stream
            this.lastChar = 0;
            return 0;
        }
        this.index += 1;
        this.lastChar = (char) c;
        return this.lastChar;
    }


    /**
     * Consume the next character, and check that it matches a specified
     * character.
     *
     * @param c The character to match.
     * @return The character.
     * @throws IOException if the character does not match.
     */
    public char next(char c) throws IOException {
        char n = next();
        if (n != c) {
            throw syntaxError("Expected '" + c + "' and instead saw '" +
                    n + "'");
        }
        return n;
    }


    /**
     * Get the next n characters.
     *
     * @param n The number of characters to take.
     * @return A string of n characters.
     * @throws IOException Substring bounds error if there are not
     *                     n characters remaining in the source string.
     */
    public String next(int n) throws IOException {
        if (n == 0) {
            return "";
        }

        char[] buffer = new char[n];
        int pos = 0;

        if (this.useLastChar) {
            this.useLastChar = false;
            buffer[0] = this.lastChar;
            pos = 1;
        }

        int len;
        while ((pos < n) && ((len = reader.read(buffer, pos, n - pos)) != -1)) {
            pos += len;
        }
        this.index += pos;

        if (pos < n) {
            throw syntaxError("Substring bounds error");
        }

        this.lastChar = buffer[n - 1];
        return new String(buffer);
    }


    /**
     * Get the next char in the string, skipping whitespace.
     *
     * @return A character, or 0 if there are no more characters.
     * @throws IOException if the syntax of the JSON stream is not correct.
     */
    public char nextClean() throws IOException {
        for (; ;) {
            char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     *
     * @param quote The quoting character, either
     *              <code>"</code>&nbsp;<small>(double quote)</small> or
     *              <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return A String.
     * @throws IOException Unterminated string.
     */
    public String nextString(char quote) throws IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (; ;) {
            c = next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw syntaxError("Unterminated string");
                case '\\':
                    c = next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            sb.append((char) Integer.parseInt(next(4), 16));
                            break;
                        case 'x':
                            sb.append((char) Integer.parseInt(next(2), 16));
                            break;
                        default:
                            sb.append(c);
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }


    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     *
     * @param d A delimiter character.
     * @return A string.
     * @throws IOException if the underlying JSON stream syntax is incorrect
     */
    public String nextTo(char d) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (; ;) {
            char c = next();
            if (c == d || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the text up but not including one of the specified delimiter
     * characters or the end of line, whichever comes first.
     *
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     */
    public String nextTo(String delimiters) throws IOException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (; ;) {
            c = next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     *
     * @return An object.
     * @throws IOException If syntax error.
     */
    public Object nextValue() throws IOException {
        char c = nextClean();
        String s;

        switch (c) {
            case '"':
            case '\'':
                return nextString(c);
            case '{':
                back();
                return parseObject();
            case '[':
            case '(':
                back();
                return parseArray();
        }

        /*
        * Handle unquoted text. This could be the values true, false, or
        * null, or it can be a number. An implementation (such as this one)
        * is allowed to also accept non-standard forms.
        *
        * Accumulate characters until we reach the end of the text or a
        * formatting character.
        */

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        back();

        s = sb.toString().trim();
        if (s.equals("")) {
            throw syntaxError("Missing value");
        }
        return stringToValue(s);
    }


    /**
     * Skip characters until the next character is the requested character.
     * If the requested character is not found, no characters are skipped.
     *
     * @param to A character to skip to.
     * @return The requested character, or zero if the requested character
     *         is not found.
     * @throws IOException thrown if there is a problem parsing the JSON stream (i.e IOException).
     */
    public char skipTo(char to) throws IOException {
        char c;
        int startIndex = this.index;
        reader.mark(Integer.MAX_VALUE);
        do {
            c = next();
            if (c == 0) {
                reader.reset();
                this.index = startIndex;
                return c;
            }
        } while (c != to);

        back();
        return c;
    }

    /**
     * Make a IOException to signal a syntax error.
     *
     * @param message The error message.
     * @return A IOException object, suitable for throwing
     */
    public IOException syntaxError(String message) {
        return new IOException(message + toString());
    }


    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at character [this.index]"
     */
    public String toString() {
        return " at character " + index;
    }

    private Map<String, Object> parseObject() throws IOException {
        char c;
        String key;

        Map<String, Object> jsonObject = new HashMap<String, Object>();

        if (nextClean() != '{') {
            throw syntaxError("A JSONObject text must begin with '{'");
        }
        for (; ;) {
            c = nextClean();
            switch (c) {
                case 0:
                    throw syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return jsonObject;
                default:
                    back();
                    key = nextValue().toString();
            }

            /*
            * The key is followed by ':'. We will also tolerate '=' or '=>'.
            */

            c = nextClean();
            if (c == '=') {
                if (next() != '>') {
                    back();
                }
            } else if (c != ':') {
                throw syntaxError("Expected a ':' after a key");
            }
            putOnce(jsonObject, key, nextValue());

            /*
            * Pairs are separated by ','. We will also tolerate ';'.
            */

            switch (nextClean()) {
                case ';':
                case ',':
                    if (nextClean() == '}') {
                        return jsonObject;
                    }
                    back();
                    break;
                case '}':
                    return jsonObject;
                default:
                    throw syntaxError("Expected a ',' or '}'");
            }
        }
    }

    private void putOnce(Map<String, Object> jsonObject, String key, Object value) throws IOException {
        if (key != null && value != null) {
            if (!jsonObject.containsKey(key)) {
                jsonObject.put(key, value);
            } else {
                throw new IOException("Duplicate key \"" + key + "\"");
            }
        }
    }

    public List<Object> parseArray() throws IOException {
        List<Object> list = new ArrayList<Object>();

        char c = nextClean();
        char q;
        if (c == '[') {
            q = ']';
        } else if (c == '(') {
            q = ')';
        } else {
            throw syntaxError("A JSONArray text must start with '['");
        }
        if (nextClean() == ']') {
            return list;
        }
        back();
        for (; ;) {
            if (nextClean() == ',') {
                back();
                list.add(null);
            } else {
                back();
                list.add(nextValue());
            }
            c = nextClean();
            switch (c) {
                case ';':
                case ',':
                    if (nextClean() == ']') {
                        return list;
                    }
                    back();
                    break;
                case ']':
                case ')':
                    if (q != c) {
                        throw syntaxError("Expected a '" + q + "'");
                    }
                    return list;
                default:
                    throw syntaxError("Expected a ',' or ']'");
            }
        }
    }

    private Object stringToValue(String s) {
        if (s.equals("")) {
            return s;
        }
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("null")) {
            return null;
        }

        /*
        * If it might be a number, try converting it. We support the 0- and 0x-
        * conventions. If a number cannot be produced, then the value will just
        * be a string. Note that the 0-, 0x-, plus, and implied string
        * conventions are non-standard. A JSON parser is free to accept
        * non-JSON forms as long as it accepts all correct JSON forms.
        */

        char b = s.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            if (b == '0') {

                if (s.length() > 2 &&
                        (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                    try {
                        return Long.parseLong(s.substring(2),
                                16);
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                } else {
                    try {
                        return Long.parseLong(s, 8);
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                }
            }
            try {
                return new Long(s);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    return new Integer(s);
                } catch (Exception f) {
                    try {
                        return new Double(s);
                    } catch (Exception g) {
                        /* Ignore the error */
                    }
                }
            }
        }
        return s;
    }


    private static void printMap(Map map, int indent) {
        for (Object o : map.keySet()) {
            System.out.println(indent(indent) + "map key:" + o);
            Object v = map.get(o);
            if (v instanceof Map) {
                printMap((Map) v, indent + 2);
            } else if (v instanceof List) {
                printList((List) v, indent + 2);
            } else {
                System.out.println(indent(indent) + "map value:" + map.get(o));
            }
        }
    }

    private static void printList(List list, int indent) {
        for (Object v : list) {
            if (v instanceof Map) {
                printMap((Map) v, indent + 2);
            } else if (v instanceof List) {
                printList((List) v, indent + 2);
            } else {
                System.out.println(indent(indent) + "list value:" + v);
            }
        }
    }

    private static String indent(int val) {
        String ret = "";
        for (int i = 0; i < val; i++) {
            ret += " ";
        }
        return ret;
    }
}

