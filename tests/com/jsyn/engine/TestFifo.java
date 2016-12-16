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

package com.jsyn.engine;

import junit.framework.TestCase;

import com.jsyn.io.AudioFifo;

public class TestFifo extends TestCase {

    public void testBasic() {
        Thread watchdog = startWatchdog(600);

        AudioFifo fifo = new AudioFifo();
        fifo.setReadWaitEnabled(false);
        fifo.allocate(8);
        assertEquals("start empty", 0, fifo.available());

        assertEquals("read back Nan when emopty", Double.NaN, fifo.read());

        fifo.write(1.0);
        assertEquals("added one value", 1, fifo.available());
        assertEquals("read back same value", 1.0, fifo.read());
        assertEquals("back to empty", 0, fifo.available());

        for (int i = 0; i < fifo.size(); i++) {
            assertEquals("adding data", i, fifo.available());
            fifo.write(100.0 + i);
        }
        for (int i = 0; i < fifo.size(); i++) {
            assertEquals("removing data", fifo.size() - i, fifo.available());
            assertEquals("reading back data", 100.0 + i, fifo.read());
        }
        watchdog.interrupt();
    }

    /**
     * Wrap around several times to test masking.
     */
    public void testWrapping() {

        final int chunk = 5;
        AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);
        double value = 1000.0;
        for (int i = 0; i < (fifo.size() * chunk); i++) {
            value = checkFifoChunk(fifo, value, chunk);
        }

    }

    private double checkFifoChunk(AudioFifo fifo, double value, int chunk) {
        for (int i = 0; i < chunk; i++) {
            assertEquals("adding data", i, fifo.available());
            fifo.write(value + i);
        }
        for (int i = 0; i < chunk; i++) {
            assertEquals("removing data", chunk - i, fifo.available());
            assertEquals("reading back data", value + i, fifo.read());
        }
        return value + chunk;
    }

    public void testBadSize() {
        boolean caught = false;
        try {
            AudioFifo fifo = new AudioFifo();
            fifo.allocate(20); // not power of 2
            assertTrue("should not get here", false);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue("should have caught size exception", caught);
    }

    public void testSingleReadWait() {
        final int chunk = 5;
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(false);
        fifo.setReadWaitEnabled(true);
        final double value = 50.0;

        // Schedule a delayed write in another thread.
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(200);
                    for (int i = 0; i < chunk; i++) {
                        fifo.write(value + i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Thread watchdog = startWatchdog(500);
        for (int i = 0; i < chunk; i++) {
            assertEquals("reading back data", value + i, fifo.read());
        }
        watchdog.interrupt();
    }

    private Thread startWatchdog(final int msec) {
        Thread watchdog = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(msec);
                    assertTrue("test must still be waiting", false);
                } catch (InterruptedException e) {
                }
            }
        };
        watchdog.start();
        return watchdog;
    }

    public void testSingleWriteWait() {
        final int chunk = 13;
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(true);
        fifo.setReadWaitEnabled(true);
        final double value = 50.0;

        // Schedule a delayed read in another thread.
        Thread readThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(200);
                    for (int i = 0; i < chunk; i++) {
                        // System.out.println( "testSingleWriteWait: try to read" );
                        double got = fifo.read();
                        assertEquals("adding data", value + i, got);
                        // System.out.println( "testSingleWriteWait: read " + got );
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        readThread.start();

        Thread watchdog = startWatchdog(500);
        // Try to write more than will fit so we will hang.
        for (int i = 0; i < chunk; i++) {
            fifo.write(value + i);
        }
        watchdog.interrupt();

        try {
            readThread.join(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals("readThread should be done.", false, readThread.isAlive());
    }

    public void testBlockReadWait() {
        final int chunk = 50;
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(false);
        fifo.setReadWaitEnabled(true);
        final double value = 300.0;
        double[] readBuffer = new double[chunk];

        // Schedule delayed writes in another thread.
        new Thread() {
            @Override
            public void run() {
                int numWritten = 0;
                double[] writeBuffer = new double[4];
                try {
                    while (numWritten < chunk) {
                        sleep(30);
                        for (int i = 0; i < writeBuffer.length; i++) {
                            writeBuffer[i] = value + numWritten;
                            numWritten += 1;
                        }

                        fifo.write(writeBuffer);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Thread watchdog = startWatchdog(600);
        fifo.read(readBuffer);
        for (int i = 0; i < chunk; i++) {
            assertEquals("reading back data", value + i, readBuffer[i]);
        }
        watchdog.interrupt();

    }

    public void testBlockReadAndWriteWaitStress() {
        final int chunk = 10000000; // 10 Megabytes
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(true);
        fifo.setReadWaitEnabled(true);
        final double value = 50.0;

        // Schedule a delayed write in another thread.
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(200);
                    for (int i = 0; i < chunk; i++) {
                        fifo.write(value + i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Thread watchdog = startWatchdog(10000);
        for (int i = 0; i < chunk; i++) {
            assertEquals("reading back data", value + i, fifo.read());
        }
        watchdog.interrupt();
    }
}
