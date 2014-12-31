/*
 * Copyright 2002 Phil Burk, Mobileer Inc
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

/**
 * Tools for reading and writing XML files.
 * 
 * @author Phil Burk, (C) 2002 Mobileer Inc, PROPRIETARY and CONFIDENTIAL
 * @see XMLListener
 * @see XMLPrinter
 */

public class XMLTools {
    static public String replaceCharacters(String text, int ch, String newText) {
        // just return same string if character does not occur
        int index = text.indexOf(ch);
        if (index < 0)
            return text;

        StringBuffer buffer = new StringBuffer();
        index = 0;
        while (index < text.length()) {
            char cs = text.charAt(index);
            if (cs == ch) {
                buffer.append(newText);
            } else {
                buffer.append(cs);
            }
            index++;
        }
        return buffer.toString();
    }

/** Convert a human readable string into an XML valid string with proper escape sequences.
 *  Character like '<' must be converted to &lt;
 *
 * <pre>
 *	&  => &amp;
 *	< => &lt;
 *	> => &gt;
 *	" => &quot;
 *	' => &apos;
 * </pre>
 */
    public static String escapeText(String text) {
        text = replaceCharacters(text, '&', "&amp;");
        text = replaceCharacters(text, '<', "&lt;");
        text = replaceCharacters(text, '>', "&gt;");
        text = replaceCharacters(text, '"', "&quot;");
        text = replaceCharacters(text, '\'', "&apos;");
        return text;
    }

    public static String unescapeText(String text) {
        int newchar;
        // just return same string if ampersand does not occur
        int index = text.indexOf('&');
        if (index < 0)
            return text;

        StringBuffer buffer = new StringBuffer();
        index = 0;
        while (index < text.length()) {
            char cs = text.charAt(index);
            if (cs == '&') {
                // find ending semicolon
                int indexSemiColon = text.indexOf(';', index);
                if (indexSemiColon >= 0) {
                    // figure out replacement string
                    String repStr = null;
                    String escape = text.substring(index + 1, indexSemiColon);
                    if (escape.equals("amp"))
                        repStr = "&";
                    else if (escape.equals("lt"))
                        repStr = "<";
                    else if (escape.equals("gt"))
                        repStr = ">";
                    else if (escape.equals("quot"))
                        repStr = "\"";
                    else if (escape.equals("apos"))
                        repStr = "'";
                    if (repStr != null)
                        buffer.append(repStr);
                } else
                    break; // FIXME - throw exception?
                index = indexSemiColon;
            } else {
                buffer.append(cs);
            }
            index++;
        }
        return buffer.toString();
    }

    public static void testText(String text1) {
        String text2, text3;
        text2 = escapeText(text1);
        text3 = unescapeText(text2);
        System.out.println("Convert     \"" + text1 + "\"");
        System.out.println("to          \"" + text2 + "\"");
        System.out.println("and back to \"" + text3 + "\"");
    }

    public static void main(String argv[]) {
        testText("Is 2 < 3 or is 2 > 3 ?");
        testText("Joe's & Fred's <<<wow>>> use quotes \"hey bob\"");
    }
}
