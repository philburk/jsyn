/*
 * Copyright 2009 Phil Burk, Mobileer Inc
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
import java.io.RandomAccessFile;

/**
 * An OutputStream wrapper for a RandomAccessFile Needed by routines that output to an OutputStream.
 * <p>
 * Note that if you are overwriting a RandomAccessFile then you should clear it before you start.
 * This will prevent having the remainder of a longer file stuck at the end of a short file. In Java
 * 1.2 you can call setLength(0).
 */
public class RandomOutputStream extends OutputStream {
    RandomAccessFile randomFile;

    public RandomOutputStream(RandomAccessFile randomFile) {
        this.randomFile = randomFile;
    }

    @Override
    public void write(int b) throws IOException {
        randomFile.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        randomFile.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        randomFile.write(b, off, len);
    }

    public void seek(long position) throws IOException {
        randomFile.seek(position);
    }

    public long getFilePointer() throws IOException {
        return randomFile.getFilePointer();
    }

}
