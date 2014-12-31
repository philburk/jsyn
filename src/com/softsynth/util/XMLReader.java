/*
 * Copyright 1997 Phil Burk, Mobileer Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.softsynth.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.StreamCorruptedException;
import java.util.Hashtable;

/**
 * Parse an XML stream using a simple State Machine XMLReader does not buffer the input stream so
 * you may want to do that yourself using a BufferedInputStream.
 * 
 * @author (C) 1997 Phil Burk
 * @see XMLListener
 * @see XMLPrinter
 */

public class XMLReader extends PushbackInputStream {
    XMLListener listener;
    final static int IDLE = 0;
    final static int INTAG = 0;
    static int depth = 0;

    final static int STATE_TOP = 0;
    final static int STATE_TAG_NAME = 1;
    final static int STATE_TAG_FIND_ANGLE = 2;
    final static int STATE_TAG_ATTR_NAME = 3;
    final static int STATE_TAG_FIND_EQUAL = 4;
    final static int STATE_TAG_FIND_QUOTE = 5;
    final static int STATE_TAG_ATTR_VALUE = 6;
    final static int STATE_CONTENT = 7;
    final static int STATE_CHECK_END = 8;
    final static int STATE_TAG_SKIP = 9;

    public void setXMLListener(XMLListener listener) {
        this.listener = listener;
    }

    public XMLReader(InputStream stream) {
        super(stream);
    }

    /**
     * Read a unicode character from a UTF8 stream.
     * 
     * @throws IOException
     */
    private int readChar() throws IOException {
        int c = read();
        if (c < 0)
            return c; // EOF
        else if (c < 128)
            return c; // regular ASCII char
        // We are probably starting a multi-byte character.
        {
            byte[] bar = null;
            if (c < 0xE0)
                bar = new byte[2];
            else if (c < 0xF0)
                bar = new byte[3];
            else if (c < 0xF8)
                bar = new byte[4];
            else if (c < 0xFC)
                bar = new byte[5];
            else if (c < 0xFE)
                bar = new byte[6];
            bar[0] = (byte) c;
            // Gather the rest of the bytes used to encode this character.
            for (int i = 1; i < bar.length; i++) {
                c = read();
                if ((c & 0xc0) != 0x80)
                    throw new IOException("invalid UTF8 continuation " + Integer.toHexString(c));
                bar[i] = (byte) c;
            }
            return new String(bar, "UTF8").charAt(0);
        }
    }

    /*****************************************************************
 */
    public void parse() throws IOException {
        int i;
        char c;

        while (true) {
            i = readChar();
            if (i < 0)
                break; // got end of file
            c = (char) i;

            if (c == '<') {
                parseElement();
            } else if (!Character.isWhitespace(c)) {
                throw new StreamCorruptedException(
                        "Unexpected character. This doesn't look like an XML file!");
            }
        }
    }

    String charToString(char c) {
        String s;
        switch (c) {
            case '\r':
                s = "\\r";
                break;
            case '\n':
                s = "\\n";
                break;
            case '\t':
                s = "\\t";
                break;
            default:
                if (Character.isWhitespace(c))
                    s = " ";
                else
                    s = "" + c;
                break;
        }
        return s;
    }

    boolean isStringWhite(String s) {
        if (s == null)
            return true;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /*****************************************************************
 */
    void parseElement() throws IOException {
        int state = STATE_TAG_NAME;
        int i;
        char c;
        String tagName = "";
        String name = null, value = null;
        boolean ifEmpty = false;
        boolean endTag = false;
        boolean done = false;
        boolean skipWhiteSpace = true;
        char endQuote = '"'; // may also be single quote
        String content = null;
        Hashtable attributes = new Hashtable();

        // System.out.println("\nparseElement() ---------- " + depth++);
        while (!done) {
            do {
                i = readChar();
                if (i < 0)
                    throw new EOFException("EOF inside element!");
                c = (char) i;
            } while (skipWhiteSpace && Character.isWhitespace(c));
            skipWhiteSpace = false;

            // System.out.print("(" + charToString(c) + "," + state + ")" );

            switch (state) {

                case STATE_TAG_NAME:
                    if (Character.isWhitespace(c)) {
                        skipWhiteSpace = true;
                        state = STATE_TAG_FIND_ANGLE;
                    } else if (c == '/') // this tag has no matching end tag
                    {
                        ifEmpty = true;
                        state = STATE_TAG_FIND_ANGLE;
                    } else if (c == '>') // end of tag
                    {
                        if (endTag) {
                            listener.endElement(tagName);
                            done = true;
                        } else {
                            listener.beginElement(tagName, attributes, ifEmpty);
                            state = STATE_CONTENT;
                        }
                    } else if (c == '?') {
                        state = STATE_TAG_SKIP; // got version stuff so skip to end
                    } else if (c == '!') // FIXME - parse for "--"
                    {
                        state = STATE_TAG_SKIP; // got comment
                    } else {
                        tagName += c;
                    }
                    break;

                case STATE_TAG_SKIP:
                    if (c == '>') {
                        done = true;
                    }
                    break;

                case STATE_TAG_FIND_ANGLE:
                    if (c == '/') // this tag has no matching end tag
                    {
                        ifEmpty = true;
                    } else if (c == '>') {
                        if (endTag) {
                            listener.endElement(tagName);
                            done = true;
                        } else {
                            listener.beginElement(tagName, attributes, ifEmpty);
                            state = STATE_CONTENT;
                            done = ifEmpty;
                        }
                    } else {
                        state = STATE_TAG_ATTR_NAME;
                        name = "" + c;
                    }
                    break;

                case STATE_TAG_ATTR_NAME:
                    if (Character.isWhitespace(c)) {
                        skipWhiteSpace = true;
                        state = STATE_TAG_FIND_EQUAL;
                    } else if (c == '=') {
                        skipWhiteSpace = true;
                        state = STATE_TAG_FIND_QUOTE;
                    } else {
                        name += c;
                    }
                    break;

                case STATE_TAG_FIND_EQUAL:
                    if (c == '=') {
                        skipWhiteSpace = true;
                        state = STATE_TAG_FIND_QUOTE;
                    } else {
                        throw new StreamCorruptedException("Found " + charToString(c)
                                + ", expected =.");
                    }
                    break;

                case STATE_TAG_FIND_QUOTE:
                    if (c == '"') {
                        state = STATE_TAG_ATTR_VALUE;
                        value = "";
                        endQuote = '"';
                    } else if (c == '\'') {
                        state = STATE_TAG_ATTR_VALUE;
                        value = "";
                        endQuote = '\'';
                    } else {
                        throw new StreamCorruptedException("Found " + charToString(c)
                                + ", expected '\"'.");
                    }
                    break;

                case STATE_TAG_ATTR_VALUE:
                    if (c == endQuote) {
                        attributes.put(name, value);
                        // System.out.println("\ngot " + name + " = " + value );
                        skipWhiteSpace = true;
                        state = STATE_TAG_FIND_ANGLE;
                    } else {
                        value += c;
                    }
                    break;

                case STATE_CONTENT:
                    if (c == '<') {
                        state = STATE_CHECK_END;
                        if (!isStringWhite(content)) {
                            String unescaped = com.softsynth.util.XMLTools.unescapeText(content);
                            listener.foundContent(unescaped);
                        }
                        content = null;
                    } else {
                        if (content == null)
                            content = "";
                        content += c;
                    }
                    break;

                case STATE_CHECK_END:
                    if (c == '/') {
                        endTag = true;
                        state = STATE_TAG_NAME;
                        tagName = "";
                    } else {
                        unread(c);
                        parseElement();
                        state = STATE_CONTENT;
                    }
                    break;
            }
        }
        // System.out.println("\nparseElement: returns, " + --depth );
    }

    /**
     * Get a single attribute from the Hashtable. Use the default if not found.
     */
    public static int getAttribute(Hashtable attributes, String key, int defaultValue) {
        String s = (String) attributes.get(key);
        return (s == null) ? defaultValue : Integer.parseInt(s);
    }

    /**
     * Get a single attribute from the Hashtable. Use the default if not found.
     */
    public static double getAttribute(Hashtable attributes, String key, double defaultValue) {
        String s = (String) attributes.get(key);
        return (s == null) ? defaultValue : Double.valueOf(s).doubleValue();
    }

}
