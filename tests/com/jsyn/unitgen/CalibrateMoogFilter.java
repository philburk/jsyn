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

package com.jsyn.unitgen;

import javax.swing.JApplet;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;

/**
 * Play a sawtooth through a 4-pole filter.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class CalibrateMoogFilter extends JApplet {
    private Synthesizer synth;
    private UnitOscillator oscillator;
    private SineOscillator reference;
    ZeroCrossingCounter zeroCounter;
    PitchDetector pitchDetector;
    ZeroCrossingCounter sineZeroCounter;
    PitchDetector sinePitchDetector;
    private FilterFourPoles filterMoog;
    private LineOut lineOut;

    @Override
    public void init() {
        synth = JSyn.createSynthesizer();
        synth.setRealTime(false);
        synth.add(oscillator = new SawtoothOscillatorBL());
        synth.add(reference = new SineOscillator());
        synth.add(filterMoog = new FilterFourPoles());
        synth.add(pitchDetector = new PitchDetector());
        synth.add(sinePitchDetector = new PitchDetector());
        synth.add(zeroCounter = new ZeroCrossingCounter());
        synth.add(sineZeroCounter = new ZeroCrossingCounter());
        synth.add(lineOut = new LineOut());

        oscillator.output.connect(filterMoog.input);
        filterMoog.output.connect(zeroCounter.input);
        zeroCounter.output.connect(pitchDetector.input);
        reference.output.connect(0, lineOut.input, 0);
        filterMoog.output.connect(0, lineOut.input, 1);

        reference.output.connect(sineZeroCounter.input);
        sineZeroCounter.output.connect(sinePitchDetector.input);

        oscillator.frequency.set(130.0);
        oscillator.amplitude.set(0.001);
        filterMoog.frequency.set(440.0);
        filterMoog.Q.set(4.1);
    }

    @Override
    public void start() {
        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        pitchDetector.start();
        sinePitchDetector.start();
        lineOut.start();
    }

    @Override
    public void stop() {
        pitchDetector.stop();
        sinePitchDetector.stop();
        lineOut.stop();
        synth.stop();
    }

    public void test() {
        init();
        start();
        try {
            calibrate();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
    }

    private void calibrate() throws InterruptedException {
        synth.sleepFor(2.0);
        double freq = 100.0;
        System.out
                .printf("freq, moogFreq, ratio, moogConf, sineFreq, sineConf, moogZRate, sineZRate\n");
        long startingFrameCount = synth.getFrameCount();
        long startingMoogZeroCount = zeroCounter.getCount();
        long startingSineZeroCount = sineZeroCounter.getCount();
        for (int i = 0; i < 50; i++) {
            reference.frequency.set(freq);
            filterMoog.frequency.set(freq);
            synth.sleepFor(2.0);

            long endingFrameCount = synth.getFrameCount();
            long elapsedFrames = endingFrameCount - startingFrameCount;
            startingFrameCount = endingFrameCount;

            long endingMoogZeroCount = zeroCounter.getCount();
            long elapsedMoogZeros = endingMoogZeroCount - startingMoogZeroCount;
            startingMoogZeroCount = endingMoogZeroCount;

            long endingSineZeroCount = sineZeroCounter.getCount();
            long elapsedSineZeros = endingSineZeroCount - startingSineZeroCount;
            startingSineZeroCount = endingSineZeroCount;

            double moogZeroRate = elapsedMoogZeros * (double) synth.getFrameRate() / elapsedFrames;
            double sineZeroRate = elapsedSineZeros * (double) synth.getFrameRate() / elapsedFrames;

            double moogMeasuredFreq = pitchDetector.frequency.get();
            double moogConfidence = pitchDetector.confidence.get();
            double sineMeasuredFreq = sinePitchDetector.frequency.get();
            double sineConfidence = sinePitchDetector.confidence.get();
            double ratio = freq / moogMeasuredFreq;
            System.out.printf("%7.2f, %8.5f, %7.5f, %4.2f, %8.5f, %4.2f, %8.4f, %8.4f\n", freq,
                    moogMeasuredFreq, ratio, moogConfidence, sineMeasuredFreq, sineConfidence,
                    moogZeroRate, sineZeroRate);

            freq *= 1.1;
        }
    }

    public static void main(String args[]) {
        new CalibrateMoogFilter().test();
    }

}
