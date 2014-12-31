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
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SpectralFFT;
import com.jsyn.unitgen.SpectralIFFT;
import com.jsyn.unitgen.UnitOscillator;

/**
 * Play a sine sweep through an FFT/IFFT pair.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class FFTPassthrough {
    private Synthesizer synth;
    private PassThrough center;
    private UnitOscillator osc;
    private UnitOscillator lfo;
    private SpectralFFT fft;
    private SpectralIFFT ifft1;
    private LineOut lineOut;
    private SpectralIFFT ifft2;

    private void test() {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        // Add a tone generator.
        synth.add(center = new PassThrough());
        // synth.add( osc = new SawtoothOscillatorBL() );
        synth.add(osc = new SineOscillator());
        synth.add(lfo = new SineOscillator());
        synth.add(fft = new SpectralFFT());
        synth.add(ifft1 = new SpectralIFFT());
        synth.add(ifft2 = new SpectralIFFT());
        // Add a stereo audio output unit.
        synth.add(lineOut = new LineOut());

        // Connect the oscillator to both channels of the output.
        center.output.connect(osc.frequency);
        lfo.output.connect(osc.frequency);
        osc.output.connect(fft.input);
        fft.output.connect(ifft1.input);
        fft.output.connect(ifft2.input);
        ifft1.output.connect(0, lineOut.input, 0);
        ifft2.output.connect(0, lineOut.input, 1);

        // Set the frequency and amplitude for the modulated sine wave.
        center.input.set(600.0);
        lfo.frequency.set(0.2);
        lfo.amplitude.set(400.0);
        osc.amplitude.set(0.6);

        // We only need to start the LineOut. It will pull data through the
        // chain.
        lineOut.start();

        System.out.println("You should now be hearing a clean oscillator on the left channel,");
        System.out.println("and the FFT->IFFT processed signal on the right channel.");

        // Sleep while the sound is generated in the background.
        try {
            double time = synth.getCurrentTime();
            // Sleep for a few seconds.
            synth.sleepUntil(time + 20.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stop playing. -------------------");
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        new FFTPassthrough().test();
    }
}
