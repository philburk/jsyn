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

package com.jsyn.examples;

import java.io.File;
import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.ChannelIn;
import com.jsyn.unitgen.ChannelOut;
import com.jsyn.unitgen.FixedRateMonoReader;
import com.jsyn.unitgen.FixedRateMonoWriter;
import com.jsyn.unitgen.Maximum;
import com.jsyn.unitgen.Minimum;
import com.jsyn.util.WaveFileWriter;

/**
 * Echo the input using a circular buffer in a sample.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class LongEcho {
    final static int DELAY_SECONDS = 4;
    Synthesizer synth;
    ChannelIn channelIn;
    ChannelOut channelOut;
    FloatSample sample;
    FixedRateMonoReader reader;
    FixedRateMonoWriter writer;
    Minimum minner;
    Maximum maxxer;

    private void test() throws IOException {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        synth.add(channelIn = new ChannelIn());
        synth.add(channelOut = new ChannelOut());

        synth.add(minner = new Minimum());
        synth.add(maxxer = new Maximum());
        synth.add(reader = new FixedRateMonoReader());
        synth.add(writer = new FixedRateMonoWriter());

        sample = new FloatSample(44100 * DELAY_SECONDS, 1);

        maxxer.inputB.set(-0.98); // clip
        minner.inputB.set(0.98);

        // Connect the input to the output.
        channelIn.output.connect(minner.inputA);
        minner.output.connect(maxxer.inputA);
        maxxer.output.connect(writer.input);

        reader.output.connect(channelOut.input);

        // Both stereo.
        int numInputChannels = 2;
        int numOutputChannels = 2;
        synth.start(44100, AudioDeviceManager.USE_DEFAULT_DEVICE, numInputChannels,
                AudioDeviceManager.USE_DEFAULT_DEVICE, numOutputChannels);

        writer.start();
        channelOut.start();

        // For a long echo, read cursor should be just in front of the write cursor.
        reader.dataQueue.queue(sample, 1000, sample.getNumFrames() - 1000);
        // Loop both forever.
        reader.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
        writer.dataQueue.queueLoop(sample, 0, sample.getNumFrames());
        System.out.println("Start talking. You should hear an echo after " + DELAY_SECONDS
                + " seconds.");
        // Sleep a while.
        try {
            double time = synth.getCurrentTime();
            // Sleep for a while
            synth.sleepUntil(time + 30.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        saveEcho(new File("saved_echo.wav"));
        // Stop everything.
        synth.stop();
    }

    private void saveEcho(File file) throws IOException {
        WaveFileWriter writer = new WaveFileWriter(file);
        writer.setFrameRate(44100);
        writer.setSamplesPerFrame(1);
        writer.setBitsPerSample(16);
        float[] buffer = new float[sample.getNumFrames()];
        sample.read(buffer);
        for (float v : buffer) {
            writer.write(v);
        }
        writer.close();
    }

    public static void main(String[] args) {
        try {
            new LongEcho().test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
