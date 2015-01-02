/*
 * Copyright 2013 Phil Burk, Mobileer Inc
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
/**
 * 
 */

package com.jsyn.benchmarks;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.PitchDetector;
import com.jsyn.unitgen.SawtoothOscillator;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.SawtoothOscillatorDPW;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.SquareOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;
import com.softsynth.math.FourierMath;

/**
 * @author Phil Burk (C) 2013 Mobileer Inc
 */
public class BenchJSyn {
    private Synthesizer synth;
    private long startTime;
    private long endTime;
    private PassThrough pass;

    public void run() {
        try {
            // Run multiple times to see if HotSpot compiler or cache makes a difference.
            for (int i = 0; i < 4; i++) {
                benchmark();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void benchmark() throws InstantiationException, IllegalAccessException,
            InterruptedException {
        double realTime = 10.0;
        int count = 40;

        // benchFFTDouble();
        // benchFFTFloat();
        /*
         * realTime = 20.0; benchmarkOscillator(SawtoothOscillator.class, count, realTime);
         * benchmarkOscillator(SawtoothOscillatorDPW.class, count, realTime);
         * benchmarkOscillator(SawtoothOscillatorBL.class, count, realTime);
         */
        benchmarkOscillator(SquareOscillator.class, count, realTime);
        benchmarkOscillator(SquareOscillatorBL.class, count, realTime);

        benchmarkOscillator(SineOscillator.class, count, realTime);
        benchmarkPitchDetector(count, realTime);

    }

    public void benchFFTDouble() {
        int size = 2048;
        int bin = 5;
        int count = 20000;
        double[] ar = new double[size];
        double[] ai = new double[size];
        double[] magnitudes = new double[size];

        double amplitude = 1.0;
        addSineWave(size, bin, ar, amplitude);
        System.out.println("Bench double FFT");
        startTiming();
        for (int i = 0; i < count; i++) {
            FourierMath.transform(1, size, ar, ai);
        }

        endTiming(FourierMath.class, count, size / (2.0 * 44100));
        FourierMath.calculateMagnitudes(ar, ai, magnitudes);

        assert (magnitudes[bin - 1] < 0.001);
        assert (magnitudes[bin] > 0.5);
        assert (magnitudes[bin + 1] < 0.001);

    }

    public void benchFFTFloat() {
        int size = 2048;
        int bin = 5;
        int count = 20000;
        float[] ar = new float[size];
        float[] ai = new float[size];
        float[] magnitudes = new float[size];

        float amplitude = 1.0f;
        addSineWave(size, bin, ar, amplitude);

        System.out.println("Bench float FFT");
        startTiming();
        for (int i = 0; i < count; i++) {
            FourierMath.transform(1, size, ar, ai);
        }

        endTiming(FourierMath.class, count, size / (2.0 * 44100));
        FourierMath.calculateMagnitudes(ar, ai, magnitudes);

        assert (magnitudes[bin - 1] < 0.001);
        assert (magnitudes[bin] > 0.5);
        assert (magnitudes[bin + 1] < 0.001);

    }

    private void addSineWave(int size, int bin, double[] ar, double amplitude) {
        double phase = 0.0;
        double phaseIncrement = 2.0 * Math.PI * bin / size;
        for (int i = 0; i < size; i++) {
            ar[i] += Math.sin(phase) * amplitude;
            // System.out.println( i + " = " + ar[i] );
            phase += phaseIncrement;
        }
    }

    private void addSineWave(int size, int bin, float[] ar, float amplitude) {
        float phase = 0.0f;
        float phaseIncrement = (float) (2.0 * Math.PI * bin / size);
        for (int i = 0; i < size; i++) {
            ar[i] += (float) Math.sin(phase) * amplitude;
            // System.out.println( i + " = " + ar[i] );
            phase += phaseIncrement;
        }
    }

    private void stopSynth() {
        synth.stop();
    }

    private void startSynth() {
        synth = JSyn.createSynthesizer(); // Mac
        // synth = JSyn.createSynthesizer( new JSynAndroidAudioDevice() ); // Android
        synth.setRealTime(false);
        pass = new PassThrough();
        synth.add(pass);
        synth.start();
        pass.start();
    }

    private void benchmarkOscillator(Class<?> clazz, int count, double realTime)
            throws InstantiationException, IllegalAccessException, InterruptedException {
        startSynth();
        for (int i = 0; i < count; i++) {
            UnitOscillator osc = (UnitOscillator) clazz.newInstance();
            osc.output.connect(pass.input);
            synth.add(osc);
        }
        startTiming();
        synth.sleepFor(realTime);
        endTiming(clazz, count, realTime);
        stopSynth();
    }

    private void benchmarkPitchDetector(int count, double realTime) throws InstantiationException,
            IllegalAccessException, InterruptedException {
        startSynth();

        PitchDetector detector = new PitchDetector();
        synth.add(detector);
        double frequency = 198.0;
        double period = synth.getFrameRate() / frequency;
        // simple harmonic synthesis
        for (int i = 0; i < count; i++) {
            SineOscillator osc = new SineOscillator();
            synth.add(osc);
            osc.frequency.set(frequency * (i + 1));
            osc.amplitude.set(0.5 * (1.0 - (i * 0.2)));
            osc.output.connect(detector.input);
        }
        detector.start();
        startTiming();
        synth.sleepFor(realTime);
        endTiming(PitchDetector.class, count, realTime);

        double measuredPeriod = detector.period.getValue();
        double confidence = detector.confidence.getValue();
        System.out.println("period = " + period + ", measured = " + measuredPeriod
                + ", confidence = " + confidence);
        if (confidence > 0.1) {
            assert (Math.abs(measuredPeriod - period) < 0.1);
        }
        stopSynth();
    }

    private void endTiming(Class<?> clazz, int count, double realTime) {
        endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) * 1E-9;
        double percent = 100.0 * elapsedTime / (realTime * count);
        System.out.printf("%32s took %5.3f/%d seconds to process %5.4f of audio = %6.3f%c.\n",
                clazz.getSimpleName(), elapsedTime, count, realTime, percent, '%');
    }

    private void startTiming() {
        startTime = System.nanoTime();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new BenchJSyn().run();
    }

}
