/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jsonKVStore.json;

/**
 * A builder for Json and Jsop strings. It encodes string values, and knows when
 * a comma is needed. A comma is appended before '{', '[', a value, or a key;
 * but only if the last appended token was '}', ']', or a value. There is no
 * limit to the number of nesting levels.
 */
public class JsonBuilder {

    private static final boolean JSON_NEWLINES = false;
    
    /**
     * The token type that signals the end of the stream.
     */
    public static final int END = 0;

    /**
     * The token type of a string value.
     */
    public static final int STRING = 1;

    /**
     * The token type of a number value.
     */
    public static final int NUMBER = 2;

    /**
     * The token type of the value "true".
     */
    public static final int TRUE = 3;

    /**
     * The token type of the value "false".
     */
    public static final int FALSE = 4;

    /**
     * The token type of "null".
     */
    public static final int NULL = 5;

    /**
     * The token type of a parse error.
     */
    public static final int ERROR = 6;

    private StringBuilder buff = new StringBuilder();
    private boolean needComma;
    private int lineLength, previous;

    /**
     * Resets this instance.
     */
    public void resetWriter() {
        needComma = false;
        buff.setLength(0);
    }

    public void setLineLength(int length) {
        lineLength = length;
    }

    /**
     * Append all entries of the given buffer.
     *
     * @param buffer the buffer
     * @return this
     */
    public JsonBuilder append(JsonTokenizer buffer) {
        appendTag(buffer.toString());
        return this;
    }

    /**
     * Append a Jsop tag character.
     *
     * @param tag the string to append
     * @return this
     */
    public JsonBuilder tag(char tag) {
        buff.append(tag);
        needComma = false;
        return this;
    }

    /**
     * Append an (already formatted) Jsop tag. This will allow to append
     * non-Json data. This method resets the comma flag, so no comma is added
     * before the next key or value.
     *
     * @param string the string to append
     * @return this
     */
    private JsonBuilder appendTag(String string) {
        buff.append(string);
        needComma = false;
        return this;
    }

    /**
     * Append a newline character.
     *
     * @return this
     */
    public JsonBuilder newline() {
        buff.append('\n');
        return this;
    }

    /**
     * Append '{'. A comma is appended first if needed.
     *
     * @return this
     */
    public JsonBuilder object() {
        optionalCommaAndNewline(1);
        buff.append('{');
        needComma = false;
        return this;
    }

    /**
     * Append '}'.
     *
     * @return this
     */
    public JsonBuilder endObject() {
        if (JSON_NEWLINES) {
            buff.append("\n}");
        } else {
            buff.append('}');
        }
        needComma = true;
        return this;
    }

    /**
     * Append '['. A comma is appended first if needed.
     *
     * @return this
     */
    public JsonBuilder array() {
        optionalCommaAndNewline(1);
        buff.append('[');
        needComma = false;
        return this;
    }

    /**
     * Append ']'.
     *
     * @return this
     */
    public JsonBuilder endArray() {
        buff.append(']');
        needComma = true;
        return this;
    }

    /**
     * Append the key (in quotes) plus a colon. A comma is appended first if
     * needed.
     *
     * @param name the key
     * @return this
     */
    public JsonBuilder key(String name) {
        optionalCommaAndNewline(name.length());
        if (JSON_NEWLINES) {
            buff.append('\n');
        }
        buff.append(encode(name)).append(':');
        needComma = false;
        return this;
    }

    /**
     * Append a number. A comma is appended first if needed.
     *
     * @param value the value
     * @return this
     */
    public JsonBuilder value(long value) {
        return encodedValue(Long.toString(value));
    }

    /**
     * Append the boolean value 'true' or 'false'. A comma is appended first if
     * needed.
     *
     * @param value the value
     * @return this
     */
    public JsonBuilder value(boolean value) {
        return encodedValue(Boolean.toString(value));
    }

    /**
     * Append a string or null. A comma is appended first if needed.
     *
     * @param value the value
     * @return this
     */
    public JsonBuilder value(String value) {
        return encodedValue(encode(value));
    }

    /**
     * Append an already encoded value. A comma is appended first if needed.
     *
     * @param value the value
     * @return this
     */
    public JsonBuilder encodedValue(String value) {
        optionalCommaAndNewline(value.length());
        buff.append(value);
        needComma = true;
        return this;
    }

    private void optionalCommaAndNewline(int add) {
        if (needComma) {
            buff.append(',');
        }
        if (lineLength > 0) {
            int len = buff.length();
            if (len > lineLength / 4 && len + add - previous > lineLength) {
                buff.append('\n');
                previous = len;
            }
        }
    }

    /**
     * Get the generated string.
     */
    @Override
    public String toString() {
        return buff.toString();
    }

    /**
     * Convert a string to a quoted Json literal using the correct escape
     * sequences. The literal is enclosed in double quotes. Characters outside
     * the range 32..127 are encoded using
     * {@link #escape(String, StringBuilder)}). Null is encoded as "null"
     * (without quotes).
     *
     * @param s the text to convert
     * @return the Json representation (including double quotes)
     */
    public static String encode(String s) {
        if (s == null) {
            return "null";
        }
        int length = s.length();
        if (length == 0) {
            return "\"\"";
        }
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '\"' || c == '\\' || c < ' ' || (c >= 0xd800 && c <= 0xdbff)) {
                StringBuilder buff = new StringBuilder(length + 2 + length / 8);
                buff.append('\"');
                escape(s, length, buff);
                return buff.append('\"').toString();
            }
        }
        StringBuilder buff = new StringBuilder(length + 2);
        return buff.append('\"').append(s).append('\"').toString();
    }

    /**
     * Escape a string into the target buffer.
     *
     * @param s      the string to escape
     * @param buff   the target buffer
     */
    public static void escape(String s, StringBuilder buff) {
        escape(s, s.length(), buff);
    }

    /**
     * Escape a string for JSON into the target buffer.
     * <p>
     * Characters are only escaped if required by RFC 7159 (thus, controls,
     * backslash, and double quotes), or if they are part of a malformed
     * surrogate pair (which wouldn't round-trip through UTF-8 otherwise).
     *
     * @param s      the string to escape
     * @param length the number of characters.
     * @param buff   the target buffer
     */
    private static void escape(String s, int length, StringBuilder buff) {
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '"':
                // quotation mark
                buff.append("\\\"");
                break;
            case '\\':
                // backslash
                buff.append("\\\\");
                break;
            case '\b':
                // backspace
                buff.append("\\b");
                break;
            case '\f':
                // formfeed
                buff.append("\\f");
                break;
            case '\n':
                // newline
                buff.append("\\n");
                break;
            case '\r':
                // carriage return
                buff.append("\\r");
                break;
            case '\t':
                // horizontal tab
                buff.append("\\t");
                break;
            default:
                if (c < ' ') {
                    buff.append(String.format("\\u%04x", (int) c));
                } else if (c >= 0xd800 && c <= 0xdbff) {
                    // isSurrogate(), only available in Java 7
                    if (i < length - 1 && Character.isSurrogatePair(c, s.charAt(i + 1))) {
                        // ok surrogate
                        buff.append(c);
                        buff.append(s.charAt(i + 1));
                        i += 1;
                    } else {
                        // broken surrogate -> escape
                        buff.append(String.format("\\u%04x", (int) c));
                    }
                } else {
                    buff.append(c);
                }
            }
        }
    }

    /**
     * Get the buffer length.
     *
     * @return the length
     */
    public int length() {
        return buff.length();
    }

    /**
     * Beautify (format) the json / jsop string.
     *
     * @param jsop the jsop string
     * @return the formatted string
     */
    public static String prettyPrint(String jsop) {
        StringBuilder buff = new StringBuilder();
        JsonTokenizer t = new JsonTokenizer(jsop);
        while (true) {
            prettyPrint(buff, t, "  ");
            if (t.getTokenType() == END) {
                return buff.toString();
            }
        }
    }

    static String prettyPrint(StringBuilder buff, JsonTokenizer t, String ident) {
        String space = "";
        boolean inArray = false;
        while (true) {
            int token = t.read();
            switch (token) {
                case END:
                    return buff.toString();
                case STRING:
                    buff.append('\"').append(t.getEscapedToken()).append('\"');
                    break;
                case NUMBER:
                case TRUE:
                case FALSE:
                case NULL:
                case ERROR:
                    buff.append(t.getEscapedToken());
                    break;
                case '{':
                    if (t.matches('}')) {
                        buff.append("{}");
                    } else {
                        buff.append("{\n").append(space += ident);
                    }
                    break;
                case '}':
                    space = space.substring(0, space.length() - ident.length());
                    buff.append('\n').append(space).append("}");
                    break;
                case '[':
                    inArray = true;
                    buff.append("[");
                    break;
                case ']':
                    inArray = false;
                    buff.append("]");
                    break;
                case ',':
                    if (!inArray) {
                        buff.append(",\n").append(space);
                    } else {
                        buff.append(", ");
                    }
                    break;
                default:
                    buff.append((char) token).append(' ');
                    break;
            }
        }
    }

}
