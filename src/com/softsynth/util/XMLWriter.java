/*
 * Copyright 2000 Phil Burk, Mobileer Inc
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Stack;

/**********************************************************************
 * Write XML formatted file.
 * 
 * @author (C) 2000 Phil Burk, SoftSynth.com
 */

public class XMLWriter extends IndentingWriter {
    Stack tagStack = new Stack();
    boolean hasContent = false;

    public XMLWriter(OutputStream stream) {
        super(stream);
    }

    public XMLWriter(Writer outputStreamWriter) {
        super(outputStreamWriter);
    }

    public void writeAttribute(String name, String value) {
        print(" " + name + "=\"" + XMLTools.escapeText(value) + "\"");
    }

    public void writeAttribute(String name, int value) {
        writeAttribute(name, Integer.toString(value));
    }

    public void writeAttribute(String name, long value) {
        writeAttribute(name, Long.toString(value));
    }

    public void writeAttribute(String name, double value) {
        writeAttribute(name, Double.toString(value));
    }

    public void writeAttribute(String name, boolean value) {
        writeAttribute(name, (value ? "1" : "0"));
    }

    public void writeHeader() {
        println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    }

    public void startTag(String name) {
        beginTag(name);
    }

    public void beginTag(String name) {
        if (!hasContent && (tagStack.size() > 0)) {
            beginContent();
            println();
        }
        print("<" + name);
        tagStack.push(name);
        hasContent = false;
        indent();
    }

    public void endTag() {
        undent();
        String name = (String) tagStack.pop();
        if (hasContent) {
            println("</" + name + ">");
        } else {
            println(" />");
        }
        // If there are tags on the stack, then they obviously had content
        // because we are ending a nested tag.
        hasContent = !tagStack.isEmpty();
    }

    public void beginContent() {
        print(">");
        hasContent = true;
    }

    public void endContent() {
    }

    public void writeComment(String text) throws IOException {
        if (!hasContent && (tagStack.size() > 0)) {
            beginContent();
            println();
        }
        println("<!-- " + XMLTools.escapeText(text) + "-->");
    }

    public void writeContent(String string) {
        beginContent();
        print(XMLTools.escapeText(string));
        endContent();
    }

    public void writeTag(String tag, String content) {
        beginTag(tag);
        writeContent(content);
        endTag();
    }

}
