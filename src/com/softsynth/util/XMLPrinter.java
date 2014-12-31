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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Pretty print an XML file.
 * Indent each nested element.
 *
 * @author (C) 1997 Phil Burk
 * @see XMLReader
 * @see XMLListener
 */

/*********************************************************************************
 */
public class XMLPrinter extends IndentingWriter implements XMLListener {

    public XMLPrinter() {
        this(System.out);
    }

    public XMLPrinter(OutputStream stream) {
        super(stream);
    }

    /**
     * Print a file passed as a command line argument.
     */
    public static void main(String args[]) {
        String fileName;

        fileName = (args.length > 0) ? args[0] : "xmlpatch.txt";
        try {
            File file = new File(fileName);
            System.out.println("File: " + file.getAbsolutePath());
            InputStream stream = (new FileInputStream(file));
            com.softsynth.util.XMLReader xmlr = new XMLReader(stream);
            xmlr.setXMLListener(new XMLPrinter());
            xmlr.parse();
            xmlr.close();
            stream.close();
        } catch (IOException e) {
            System.out.println("Error = " + e);
        } catch (SecurityException e) {
            System.out.println("Error = " + e);
        }
    }

    @Override
    public void beginElement(String tag, Hashtable attributes, boolean ifEmpty) {
        print("<" + tag);
        indent();
        Enumeration e = attributes.keys();
        if (e.hasMoreElements())
            println();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = (String) attributes.get(key);
            println(key + "=\"" + value + "\"");
        }
        if (ifEmpty) {
            undent();
            println("/>");
        } else {
            println(">");
        }
    }

    @Override
    public void foundContent(String content) {
        if (content != null)
            println(content);
    }

    @Override
    public void endElement(String tag) {
        undent();
        println("</" + tag + ">");
    }
}
