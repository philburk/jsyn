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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Logger logs output to a file if enabled.
 * 
 * @author Phil Burk (C) 1999 SoftSynth.com
 */
public class Logger {
    FileOutputStream fileStream = null;
    BufferedOutputStream stream = null;
    int column = 0;
    boolean enabled;
    String lineTerminator;

    public Logger() {
        lineTerminator = System.getProperty("line.separator", "\n");
        // System.out.println("lineTerminator.length = " + lineTerminator.length() );
        // for( int i=0; i<lineTerminator.length(); i++ )
        // {
        // System.out.println("lineTerminator[" + i + "] = " + ((int)lineTerminator.charAt(i)) );
        // }
    }

    public void setEnable(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnable() {
        return enabled;
    }

    public void log(String msg) {
        if (!enabled)
            return;

        if (stream == null)
            System.out.print(msg);
        else {
            try {
                stream.write(msg.getBytes());
            } catch (IOException e) {
                System.err.println("Log File: " + e);
            }
        }
        column += msg.length();
    }

    public void logln(String msg) {
        log(msg);
        logln();
    }

    public void logln() {
        log(lineTerminator);
        column = 0;
    }

    /**
     * Output spaces if needed to position output at desired column. Nothing will be output if
     * already past that column.
     */
    public void advanceToColumn(int toColumn) {
        if (!enabled)
            return;

        try {
            for (; column < toColumn; column++) {
                stream.write(' ');
            }
        } catch (IOException e) {
            System.err.println("Log File: " + e);
        }
    }

    /**
     * Start a new line if there is already text on current line.
     */
    public void newLine() {
        if (column > 0)
            logln("");
    }

    /**
     * Open a log file.
     */
    public void open(String logFileName) throws IOException, SecurityException {
        open(new File(logFileName));
    }

    /**
     * Open a log file by name.
     */
    public void open(File logFile) throws IOException, SecurityException {
        // Close any existing log file.
        close();
        // Open a new log file.
        fileStream = new FileOutputStream(logFile);
        // Buffer it so it isn't awfully slow.
        stream = new BufferedOutputStream(fileStream);
        column = 0;
    }

    public void close() throws IOException {
        if (stream != null) {
            stream.flush();
            stream.close();
        }
        if (fileStream != null)
            fileStream.close();
        stream = null;
    }
}
