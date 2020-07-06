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

package com.jsyn.util;

import com.jsyn.Synthesizer;
import com.jsyn.io.AudioFifo;
import com.jsyn.io.AudioInputStream;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.unitgen.MonoStreamWriter;
import com.jsyn.unitgen.StereoStreamWriter;
import com.jsyn.unitgen.UnitStreamWriter;

/**
 * Reads audio signals from the background engine to a foreground application through an AudioFifo.
 * Connect to the input port returned by getInput().
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class AudioStreamReader implements AudioInputStream {
    private UnitStreamWriter streamWriter;
    private AudioFifo fifo;

    public AudioStreamReader(Synthesizer synth, int samplesPerFrame) {
        if (samplesPerFrame == 1) {
            streamWriter = new MonoStreamWriter();
        } else if (samplesPerFrame == 2) {
            streamWriter = new StereoStreamWriter();
        } else {
            throw new IllegalArgumentException("Only 1 or 2 samplesPerFrame supported.");
        }
        synth.add(streamWriter);

        fifo = new AudioFifo();
        fifo.setWriteWaitEnabled(!synth.isRealTime());
        fifo.setReadWaitEnabled(true);
        fifo.allocate(32 * 1024);
        streamWriter.setOutputStream(fifo);
        streamWriter.start();
    }

    public UnitInputPort getInput() {
        return streamWriter.input;
    }

    /** How many values are available to read without blocking? */
    @Override
    public int available() {
        return fifo.available();
    }

    @Override
    public void close() {
        fifo.close();
    }

    @Override
    public double read() {
        return fifo.read();
    }

    @Override
    public int read(double[] buffer) {
        return fifo.read(buffer);
    }

    @Override
    public int read(double[] buffer, int start, int count) {
        return fifo.read(buffer, start, count);
    }

}
