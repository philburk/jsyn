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

package com.jsyn.data;

import com.jsyn.exceptions.ChannelMismatchException;

/**
 * Evaluate a Function by interpolating between the values in a table. This can be used for
 * wavetable lookup or waveshaping.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class DoubleTable implements Function {
    private double[] table;

    public DoubleTable(int numFrames) {
        allocate(numFrames);
    }

    public DoubleTable(double[] data) {
        allocate(data.length);
        write(data);
    }

    public DoubleTable(ShortSample shortSample) {
        if (shortSample.getChannelsPerFrame() != 1) {
            throw new ChannelMismatchException("DoubleTable can only be built from mono samples.");
        }
        short[] buffer = new short[256];
        int framesLeft = shortSample.getNumFrames();
        allocate(framesLeft);
        int cursor = 0;
        while (framesLeft > 0) {
            int numTransfer = framesLeft;
            if (numTransfer > buffer.length) {
                numTransfer = buffer.length;
            }
            shortSample.read(cursor, buffer, 0, numTransfer);
            write(cursor, buffer, 0, numTransfer);
            cursor += numTransfer;
            framesLeft -= numTransfer;
        }
    }

    public void allocate(int numFrames) {
        table = new double[numFrames];
    }

    public int length() {
        return table.length;
    }

    public void write(double[] data) {
        write(0, data, 0, data.length);
    }

    public void write(int startFrame, short[] data, int startIndex, int numFrames) {
        for (int i = 0; i < numFrames; i++) {
            table[startFrame + i] = data[startIndex + i] * (1.0 / 32768.0);
        }
    }

    public void write(int startFrame, double[] data, int startIndex, int numFrames) {
        for (int i = 0; i < numFrames; i++) {
            table[startFrame + i] = data[startIndex + i];
        }
    }

    /**
     * Treat the double array as a lookup table with a domain of -1.0 to 1.0. If the input is out of
     * range then the output will clip to the end values.
     *
     * @param input
     * @return interpolated value from table
     */
    @Override
    public double evaluate(double input) {
        double interp;
        if (input < -1.0) {
            interp = table[0];
        } else if (input < 1.0) {
            double fractionalIndex = (table.length - 1) * (input - (-1.0)) / 2.0;
            // We don't need floor() because fractionalIndex >= 0.0
            int index = (int) fractionalIndex;
            double fraction = fractionalIndex - index;

            double s1 = table[index];
            double s2 = table[index + 1];
            interp = ((s2 - s1) * fraction) + s1;
        } else {
            interp = table[table.length - 1];
        }
        return interp;
    }
}
