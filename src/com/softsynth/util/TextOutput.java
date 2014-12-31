/*
 * Copyright 1999 Phil Burk, Mobileer Inc
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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * TextOutput sends text to System.out and/or a TextArea.
 * 
 * @author Phil Burk (C) 1999 SoftSynth.com
 */
public class TextOutput extends Frame {
    private static final long serialVersionUID = 1L;
    /** Maximum number of characters allowed in TextArea. */
    public int maxChars = (8 * 1024);
    int numChars = 0;
    static TextOutput staticTextOutput;
    static Logger logger;
    boolean isTextAreaEnabled = true;
    boolean isSystemOutEnabled = true;
    TextArea textArea;
    Button buttonClear;
    String lineTerminator = System.getProperty("line.separator", "\n");

    public TextOutput() {
        super("Text Output");
        setSize(620, 400);
        setLayout(new BorderLayout());
        textArea = new TextArea("", 30, 120, TextArea.SCROLLBARS_BOTH);
        textArea.setEditable(false);
        textArea.setFont(Font.getFont("Monospaced-18")); // FIXME - why doesn't this work???
        add("Center", textArea);
        add("South", buttonClear = new Button("Clear"));
        buttonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
                numChars = 0;
            }
        });

        /* If user tries to close window, hide dialog. */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hide();
            }
        });

    }

    public void setTextAreaEnable(boolean enabled) {
        isTextAreaEnabled = enabled;
    }

    public void setSystemOutEnable(boolean enabled) {
        isSystemOutEnabled = enabled;
    }

    /**
     * Append text to the display area. If the TextArea gets full, then remove 1/4 of the text.
     */
    private void append(String text) {
        int len = text.length();
        if ((numChars + len) > maxChars) {
            int numKill = maxChars / 4;
            /* Delete 1/4 the buffer. */
            textArea.replaceRange("", 0, numKill);
            numChars -= numKill;
        }
        textArea.append(text);
        numChars += len;
    }

    public void log(String msg) {
        if (isSystemOutEnabled)
            System.out.print(msg);
        if (isTextAreaEnabled)
            append(msg);
    }

    public void logln(String msg) {
        log(msg);
        logln();
    }

    /* Just output a new line. */
    public void logln() {
        if (isSystemOutEnabled)
            System.out.println();
        if (isTextAreaEnabled)
            append(lineTerminator);
    }

    /**
     * Set a Logger to be used for directing a copy of the output to a log file.
     */
    public static void setLogger(Logger lgr) {
        logger = lgr;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void print(String msg) {
        if (staticTextOutput != null)
            staticTextOutput.log(msg);
        else
            System.out.print(msg);
        if (logger != null)
            logger.log(msg);
    }

    public static void println(String msg) {
        if (staticTextOutput != null)
            staticTextOutput.logln(msg);
        else
            System.out.println(msg);
        if (logger != null)
            logger.logln(msg);
    }

    public static void println() {
        if (staticTextOutput != null)
            staticTextOutput.logln();
        else
            System.out.println();
        if (logger != null)
            logger.logln();
    }

    public static void error(String msg) {
        println("ERROR: " + msg);
        throw new RuntimeException(msg);
    }

    public static void setStaticLocation(int x, int y) {
        getStaticInstance().setLocation(x, y);
    }

    // TODO - why can't this be called from Wire?
    public static TextOutput getStaticInstance() {
        if (staticTextOutput == null) {
            staticTextOutput = new TextOutput();
            staticTextOutput.setLocation(30, 0);
        }
        return staticTextOutput;
    }

    public static void open() {
        getStaticInstance().setVisible(true);
    }

    public static void close() {
        if (staticTextOutput != null)
            staticTextOutput.setVisible(false);
    }

    public static void bringToFront() {
        if (staticTextOutput != null)
            staticTextOutput.toFront();
    }
}
