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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Write to a file with indentation at the beginning of a line. One advantage of using a PrintWriter
 * is that it automatically handles line terminators properly on different hosts.
 * 
 * @author Phil Burk, (C) 2000 SoftSynth.com All Rights Reserved
 */

public class IndentingWriter extends PrintWriter {
    int spacesPerIndentation = 4;
    int indentation = 0;
    int position = 0;

    public IndentingWriter(OutputStream stream) {
        super(stream, true);
    }

    public IndentingWriter(Writer outputStreamWriter) {
        super(outputStreamWriter, true);
    }

    public void setIndentation(int level) {
        indentation = level;
    }

    public int getIndentation() {
        return indentation;
    }

    /**
     * Increase level of indentation by one.
     */
    public void indent() {
        indentation++;
    }

    /**
     * Decrease level of indentation by one. Don't let level go below zero.
     */
    public void undent() {
        indentation--;
        if (indentation < 0)
            indentation = 0;
    }

    /**
     * Print string. If at left margin, add spaces for current level of indentation.
     */
    @Override
    public void print(String s) {
        if (position == 0) {
            int numSpaces = indentation * spacesPerIndentation;
            for (int i = 0; i < numSpaces; i++)
                print(' ');
            position += numSpaces;
        }
        super.print(s);
        // System.out.print(s);
        position += s.length();
    }

    @Override
    public void println() {
        super.println();
        position = 0;
    }

    @Override
    public void println(String s) {
        print(s);
        println();
    }
}
