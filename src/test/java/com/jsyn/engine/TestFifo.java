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

import com.jsyn.io.AudioFifo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class TestFifo {

    @Test
    public void testBasic() {
        Thread watchdog = startWatchdog(600);

        AudioFifo fifo = new AudioFifo();
        fifo.setReadWaitEnabled(false);
        fifo.allocate(8);
        assertEquals(0, fifo.available(), "start empty");

        assertEquals(Double.NaN, fifo.read(), "read back Nan when emopty");

        fifo.write(1.0);
        assertEquals(1, fifo.available(), "added one value");
        assertEquals(1.0, fifo.read(), "read back same value");
        assertEquals(0, fifo.available(), "back to empty");

        for (int i = 0; i < fifo.size(); i++) {
            assertEquals(i, fifo.available(), "adding data");
            fifo.write(100.0 + i);
        }
        for (int i = 0; i < fifo.size(); i++) {
            assertEquals(fifo.size() - i, fifo.available(), "removing data");
            assertEquals(100.0 + i, fifo.read(), "reading back data");
        }
        watchdog.interrupt();
    }

    /**
     * Wrap around several times to test masking.
     */
    @Test
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
            assertEquals(i, fifo.available(), "adding data");
            fifo.write(value + i);
        }
        for (int i = 0; i < chunk; i++) {
            assertEquals(chunk - i, fifo.available(), "removing data");
            assertEquals(value + i, fifo.read(), "reading back data");
        }
        return value + chunk;
    }

    @Test
    public void testBadSize() {
        try {
            AudioFifo fifo = new AudioFifo();
            fifo.allocate(20); // not power of 2
            fail("should not get here");
        } catch (IllegalArgumentException ignored) {
            return;
        }

        fail("should have caught size exception");
    }

    @Test
    public void testSingleReadWait() {
        final int chunk = 5;
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(false);
        fifo.setReadWaitEnabled(true);
        final double value = 50.0;

        // Schedule a delayed write in another thread.
        new Thread(() -> {
            try {
                Thread.sleep(200);
                for (int i = 0; i < chunk; i++) {
                    fifo.write(value + i);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Thread watchdog = startWatchdog(500);
        for (int i = 0; i < chunk; i++) {
            assertEquals(value + i, fifo.read(), "reading back data");
        }
        watchdog.interrupt();
    }

    private Thread startWatchdog(final int msec) {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(msec);
                fail("test must still be waiting");
            } catch (InterruptedException ignored) {
            }
        });
        watchdog.start();
        return watchdog;
    }

    @Test
    public void testSingleWriteWait() {
        final int chunk = 13;
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(true);
        fifo.setReadWaitEnabled(true);
        final double value = 50.0;

        // Schedule a delayed read in another thread.
        Thread readThread = new Thread(() -> {
            try {
                Thread.sleep(200);
                for (int i = 0; i < chunk; i++) {
                    // LOGGER.debug( "testSingleWriteWait: try to read" );
                    double got = fifo.read();
                    assertEquals(value + i, got, "adding data");
                    // LOGGER.debug( "testSingleWriteWait: read " + got );
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
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
        assertFalse(readThread.isAlive(), "readThread should be done.");
    }

    @Test
    public void testBlockReadWait() {
        final int chunk = 50;
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(false);
        fifo.setReadWaitEnabled(true);
        final double value = 300.0;
        double[] readBuffer = new double[chunk];

        // Schedule delayed writes in another thread.
        new Thread(() -> {
            int numWritten = 0;
            double[] writeBuffer = new double[4];
            try {
                while (numWritten < chunk) {
                    Thread.sleep(30);
                    for (int i = 0; i < writeBuffer.length; i++) {
                        writeBuffer[i] = value + numWritten;
                        numWritten += 1;
                    }

                    fifo.write(writeBuffer);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Thread watchdog = startWatchdog(600);
        fifo.read(readBuffer);
        for (int i = 0; i < chunk; i++) {
            assertEquals(value + i, readBuffer[i], "reading back data");
        }
        watchdog.interrupt();

    }

    @Test
    public void testBlockReadAndWriteWaitStress() {
        final int chunk = 3000000;
        final AudioFifo fifo = new AudioFifo();
        fifo.allocate(8);

        fifo.setWriteWaitEnabled(true);
        fifo.setReadWaitEnabled(true);
        final double value = 50.0;

        // Schedule a delayed write in another thread.
        new Thread(() -> {
            try {
                Thread.sleep(200);
                for (int i = 0; i < chunk; i++) {
                    fifo.write(value + i);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // TODO Watchdog is apparently not working.
        // I set the watchdog to be very short and it did not trigger.
        Thread watchdog = startWatchdog(10 * 1000);
        for (int i = 0; i < chunk; i++) {
            assertEquals(value + i, fifo.read(), "reading back data");
        }
        watchdog.interrupt();
    }
}
