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

package com.jsyn.data;

import junit.framework.TestCase;

/**
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TestShortSample extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBytes() {
        byte[] bar = {
                18, -3
        };
        short s = (short) ((bar[0] << 8) | (bar[1] & 0xFF));
        assertEquals("A", 0x12FD, s);
    }

    public void testReadWrite() {
        short[] data = {
                123, 456, -789, 111, 20000, -32768, 32767, 0, 9876
        };
        ShortSample sample = new ShortSample(data.length, 1);
        assertEquals("Sample numFrames", data.length, sample.getNumFrames());

        // Write and read entire sample.
        sample.write(data);
        short[] buffer = new short[data.length];
        sample.read(buffer);

        for (int i = 0; i < data.length; i++) {
            assertEquals("read = write", data[i], buffer[i]);
        }

        // Write and read part of an array.
        short[] partial = {
                333, 444, 555, 666, 777
        };

        sample.write(2, partial, 1, 3);
        sample.read(1, buffer, 1, 5);

        for (int i = 0; i < data.length; i++) {
            if ((i >= 2) && (i <= 4)) {
                assertEquals("partial", partial[i - 1], buffer[i]);
            } else {
                assertEquals("read = write", data[i], buffer[i]);
            }
        }

    }

}
