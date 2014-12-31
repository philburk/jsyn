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

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.unitgen.LineIn;
import com.jsyn.unitgen.LineOut;

/**
 * Pass audio input to audio output.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class AudioPassThrough {
    Synthesizer synth;
    LineIn lineIn;
    LineOut lineOut;

    private void test() {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        // Add an audio input.
        synth.add(lineIn = new LineIn());
        // Add an audio output.
        synth.add(lineOut = new LineOut());
        // Connect the input to the output.
        lineIn.output.connect(0, lineOut.input, 0);
        lineIn.output.connect(1, lineOut.input, 1);

        // Both stereo.
        int numInputChannels = 2;
        int numOutputChannels = 2;
        synth.start(44100, AudioDeviceManager.USE_DEFAULT_DEVICE, numInputChannels,
                AudioDeviceManager.USE_DEFAULT_DEVICE, numOutputChannels);

        // We only need to start the LineOut. It will pull data from the LineIn.
        lineOut.start();
        System.out.println("Audio passthrough started.");
        // Sleep a while.
        try {
            double time = synth.getCurrentTime();
            // Sleep for a few seconds.
            synth.sleepUntil(time + 8.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Stop everything.
        synth.stop();
        System.out.println("All done.");
    }

    public static void main(String[] args) {
        new AudioPassThrough().test();
    }
}
