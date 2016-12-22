/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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

package com.jsyn.io;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * FIFO that implements AudioInputStream, AudioOutputStream interfaces. This can be used to send
 * audio data between different threads. The reads or writes may or may not wait based on flags.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class AudioFifo implements AudioInputStream, AudioOutputStream {
    // These indices run double the FIFO size so that we can tell empty from full.
    private volatile int readIndex;
    private volatile int writeIndex;
    private volatile double[] buffer;
    // Used to mask the index into range when accessing the buffer array.
    private int accessMask;
    // Used to mask the index so it wraps around.
    private int sizeMask;
    private boolean writeWaitEnabled = true;
    private boolean readWaitEnabled = true;
    final Lock lock = new ReentrantLock();
    final Condition notFull  = lock.newCondition();
    final Condition notEmpty = lock.newCondition();

    /**
     * @param size Number of doubles in the FIFO. Must be a power of 2. Eg. 1024.
     */
    public void allocate(int size) {
        if (!isPowerOfTwo(size)) {
            throw new IllegalArgumentException("Size must be a power of two.");
        }
        buffer = new double[size];
        accessMask = size - 1;
        sizeMask = (size * 2) - 1;
    }

    public int size() {
        return buffer.length;
    }

    public static boolean isPowerOfTwo(int size) {
        return ((size & (size - 1)) == 0);
    }

    /** How many samples are available for reading without blocking? */
    @Override
    public int available() {
        return (writeIndex - readIndex) & sizeMask;
    }

    @Override
    public void close() {
        // TODO Maybe we should tell any thread that is waiting that the FIFO is closed.
    }

    @Override
    public double read() {
        double value = Double.NaN;
        if (readWaitEnabled) {
            lock.lock();
            try {
              while (available() < 1) {
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {
                    return Double.NaN;
                }
              }
              value = readOneInternal();
            } finally {
              lock.unlock();
            }

        } else {
            if (readIndex != writeIndex) {
                value = readOneInternal();
            }
        }

        if (writeWaitEnabled) {
            lock.lock();
            notFull.signal();
            lock.unlock();
        }

        return value;
    }

    private double readOneInternal() {
        double value = buffer[readIndex & accessMask];
        readIndex = (readIndex + 1) & sizeMask;
        return value;
    }

    @Override
    public void write(double value) {
        if (writeWaitEnabled) {
            lock.lock();
            try {
                while (available() == buffer.length)
                {
                    try {
                        notFull.await();
                    } catch (InterruptedException e) {
                        return; // Silently fail
                    }
                }
                writeOneInternal(value);
            } finally {
                lock.unlock();
            }

        } else {
            if (available() != buffer.length) {
                writeOneInternal(value);
            }
        }

        if (readWaitEnabled) {
            lock.lock();
            notEmpty.signal();
            lock.unlock();
        }
    }

    private void writeOneInternal(double value) {
        buffer[writeIndex & accessMask] = value;
        writeIndex = (writeIndex + 1) & sizeMask;
    }

    @Override
    public int read(double[] buffer) {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(double[] buffer, int start, int count) {
        if (readWaitEnabled) {
            for (int i = 0; i < count; i++) {
                buffer[i + start] = read();
            }
        } else {
            if (available() < count) {
                count = available();
            } else {
                for (int i = 0; i < count; i++) {
                    buffer[i + start] = read();
                }
            }
        }
        return count;
    }

    @Override
    public void write(double[] buffer) {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(double[] buffer, int start, int count) {
        for (int i = 0; i < count; i++) {
            write(buffer[i + start]);
        }
    }

    /** If true then a subsequent write call will wait if there is no room to write. */
    public void setWriteWaitEnabled(boolean enabled) {
        writeWaitEnabled = enabled;

    }

    /** If true then a subsequent read call will wait if there is no data to read. */
    public void setReadWaitEnabled(boolean enabled) {
        readWaitEnabled = enabled;

    }

    public boolean isWriteWaitEnabled() {
        return writeWaitEnabled;
    }

    public boolean isReadWaitEnabled() {
        return readWaitEnabled;
    }
}
