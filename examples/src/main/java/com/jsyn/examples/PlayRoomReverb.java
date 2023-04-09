/*
 * Copyright 2022 Phil Burk, Mobileer Inc
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
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PinkNoise;
import com.jsyn.unitgen.RoomReverb;
import com.jsyn.unitgen.SquareOscillator;

/**
 * Play a noise pulse through a RoomReverb.
 * The pulse will be on the left channel and the reverb on the right.
 *
 * @author Phil Burk (C) 2022 Mobileer Inc
 */
public class PlayRoomReverb {

    private void test() {

        // Create a context for the synthesizer.
        Synthesizer synth = JSyn.createSynthesizer();

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        // Add a tone generator.
        PinkNoise source = new PinkNoise();
        //PlateReverb reverb = new PlateReverb();
        RoomReverb reverb = new RoomReverb();
        // Use a square wave to trigger the envelope.
        SquareOscillator gatingOsc = new SquareOscillator();
        EnvelopeDAHDSR dahdsr = new EnvelopeDAHDSR();
        LineOut lineOut = new LineOut();

        synth.add(source);
        synth.add(gatingOsc);
        synth.add(dahdsr);
        synth.add(reverb);
        synth.add(lineOut);

        // Connect the oscillator to both channels of the output.
        gatingOsc.output.connect(dahdsr.input);
        gatingOsc.frequency.set(0.5);
        dahdsr.output.connect(source.amplitude);
        dahdsr.attack.set(0.01);
        dahdsr.decay.set(0.05);
        dahdsr.sustain.set(0.00);

        source.output.connect(reverb.input);
        source.output.connect(0, lineOut.input, 0);
        reverb.output.connect(0, lineOut.input, 1);

        lineOut.start();

        // Sleep while the sound is generated in the background.
        try {
            double time = synth.getCurrentTime();
            // Sleep for a few seconds.
            synth.sleepUntil(time + 8.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        new PlayRoomReverb().test();
        System.exit(0);
    }
}
