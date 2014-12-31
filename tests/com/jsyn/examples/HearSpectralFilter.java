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
import com.jsyn.data.Spectrum;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SpectralFilter;
import com.jsyn.unitgen.SpectralProcessor;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.WhiteNoise;
import com.jsyn.util.WaveRecorder;

/**
 * Play a sine sweep through an FFT/IFFT pair.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class HearSpectralFilter {
    private Synthesizer synth;
    private PassThrough center;
    private UnitOscillator osc;
    private UnitOscillator lfo;
    private PassThrough mixer;
    private SpectralFilter filter;
    private LineOut lineOut;
    private WaveRecorder recorder;
    private final static boolean useRecorder = true;
    private final static boolean useProcessor = true;
    private final static int NUM_FFTS = 4;
    private final static int SIZE_LOG_2 = 10;
    private final static int SIZE = 1 << SIZE_LOG_2;
    private SpectralProcessor[] processors;
    private WhiteNoise noise;
    private static int SAMPLE_RATE = 44100;

    private static class CustomSpectralProcessor extends SpectralProcessor {
        public CustomSpectralProcessor() {
            super(SIZE);
        }

        @Override
        public void processSpectrum(Spectrum inputSpectrum, Spectrum outputSpectrum) {
            // pitchUpOctave( inputSpectrum, outputSpectrum );
            lowPassFilter(inputSpectrum, outputSpectrum, 1500.0);
        }

        public void lowPassFilter(Spectrum inputSpectrum, Spectrum outputSpectrum, double frequency) {
            inputSpectrum.copyTo(outputSpectrum);
            double[] outReal = outputSpectrum.getReal();
            double[] outImag = outputSpectrum.getImaginary();
            // brickwall filter
            int size = outReal.length;
            int cutoff = (int) (frequency * size / SAMPLE_RATE);
            int nyquist = size / 2;
            for (int i = cutoff; i < nyquist; i++) {
                // Bins above nyquist are mirror of ones below.
                outReal[i] = outReal[size - i] = 0.0;
                outImag[i] = outImag[size - i] = 0.0;
            }
        }

        // TODO Figure out why this sounds bad.
        public void pitchUpOctave(Spectrum inputSpectrum, Spectrum outputSpectrum) {
            outputSpectrum.clear();
            double[] inReal = inputSpectrum.getReal();
            double[] inImag = inputSpectrum.getImaginary();
            double[] outReal = outputSpectrum.getReal();
            double[] outImag = outputSpectrum.getImaginary();
            int size = inReal.length;
            int nyquist = size / 2;
            // Octave doubling by shifting the spectrum.
            for (int i = nyquist - 2; i > 1; i--) {
                int h = i / 2;
                outReal[i] = inReal[h];
                outImag[i] = inImag[h];
                outReal[size - i] = inReal[size - h];
                outImag[size - i] = inImag[size - h];
            }
        }
    }

    private void test() throws IOException {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        synth.setRealTime(true);

        if (useRecorder) {
            File waveFile = new File("temp_recording.wav");
            // Default is stereo, 16 bits.
            recorder = new WaveRecorder(synth, waveFile);
            System.out.println("Writing to WAV file " + waveFile.getAbsolutePath());
        }

        if (useProcessor) {
            processors = new SpectralProcessor[NUM_FFTS];
            for (int i = 0; i < NUM_FFTS; i++) {
                processors[i] = new CustomSpectralProcessor();
            }
        }

        // Add a tone generator.
        synth.add(center = new PassThrough());
        synth.add(lfo = new SineOscillator());
        synth.add(noise = new WhiteNoise());
        synth.add(mixer = new PassThrough());

        synth.add(osc = new SawtoothOscillatorBL());
        // synth.add( osc = new SineOscillator() );

        synth.add(filter = new SpectralFilter(NUM_FFTS, SIZE_LOG_2));
        // Add a stereo audio output unit.
        synth.add(lineOut = new LineOut());

        center.output.connect(osc.frequency);
        lfo.output.connect(osc.frequency);
        osc.output.connect(mixer.input);
        noise.output.connect(mixer.input);
        mixer.output.connect(filter.input);
        if (useProcessor) {
            // Pass spectra through a custom processor.
            for (int i = 0; i < NUM_FFTS; i++) {
                filter.getSpectralOutput(i).connect(processors[i].input);
                processors[i].output.connect(filter.getSpectralInput(i));
            }
        } else {
            for (int i = 0; i < NUM_FFTS; i++) {
                // Connect FFTs directly to IFFTs for passthrough.
                filter.getSpectralOutput(i).connect(filter.getSpectralInput(i));
            }

        }
        mixer.output.connect(0, lineOut.input, 0);
        filter.output.connect(0, lineOut.input, 1);

        // Set the frequency and amplitude for the modulated sine wave.
        center.input.set(600.0);
        lfo.frequency.set(0.2);
        lfo.amplitude.set(400.0);
        osc.amplitude.set(0.2);
        noise.amplitude.set(0.2);

        synth.start(SAMPLE_RATE);

        if (useRecorder) {
            mixer.output.connect(0, recorder.getInput(), 0);
            filter.output.connect(0, recorder.getInput(), 1);
            // When we start the recorder it will pull data from the oscillator
            // and sweeper.
            recorder.start();
        }

        lineOut.start();

        System.out.println("You should now be hearing a clean oscillator on the left channel,");
        System.out.println("and the FFT->IFFT processed signal on the right channel.");

        // Sleep while the sound is generated in the background.
        try {
            double time = synth.getCurrentTime();
            // Sleep for a few seconds.
            synth.sleepUntil(time + 10.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (recorder != null) {
            recorder.stop();
            recorder.close();
        }

        System.out.println("Stop playing. -------------------");
        // Stop everything.
        synth.stop();
    }

    public static void main(String[] args) {
        try {
            new HearSpectralFilter().test();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
