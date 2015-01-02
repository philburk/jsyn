/*
 * Copyright 2012 Phil Burk, Mobileer Inc
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

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.util.AutoCorrelator;
import com.jsyn.util.SignalCorrelator;

/**
 * Estimate the fundamental frequency of a monophonic signal. Analyzes an input signal and outputs
 * an estimated period in frames and a frequency in Hertz. The frequency is frameRate/period. The
 * confidence tells you how accurate the estimate is. When the confidence is low, you should ignore
 * the period. You can use a CompareUnit and a LatchUnit to hold values that you are confident of.
 * <P>
 * Note that a stable monophonic signal is required for accurate pitch tracking.
 * 
 * @author (C) 2012 Phil Burk, Mobileer Inc
 */
public class PitchDetector extends UnitGenerator {
    public UnitInputPort input;

    public UnitOutputPort period;
    public UnitOutputPort confidence;
    public UnitOutputPort frequency;
    public UnitOutputPort updated;

    protected SignalCorrelator signalCorrelator;

    private double lastFrequency = 440.0;
    private double lastPeriod = 44100.0 / lastFrequency; // result of analysis TODO update for 48000
    private double lastConfidence = 0.0; // Measure of confidence in the result.

    private static final int LOWEST_FREQUENCY = 40;
    private static final int HIGHEST_RATE = 48000;
    private static final int CYCLES_NEEDED = 2;

    public PitchDetector() {
        super();
        addPort(input = new UnitInputPort("Input"));

        addPort(period = new UnitOutputPort("Period"));
        addPort(confidence = new UnitOutputPort("Confidence"));
        addPort(frequency = new UnitOutputPort("Frequency"));
        addPort(updated = new UnitOutputPort("Updated"));
        signalCorrelator = createSignalCorrelator();
    }

    public SignalCorrelator createSignalCorrelator() {
        int framesNeeded = HIGHEST_RATE * CYCLES_NEEDED / LOWEST_FREQUENCY;
        return new AutoCorrelator(framesNeeded);
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] periods = period.getValues();
        double[] confidences = confidence.getValues();
        double[] frequencies = frequency.getValues();
        double[] updateds = updated.getValues();

        for (int i = start; i < limit; i++) {
            double current = inputs[i];
            if (signalCorrelator.addSample(current)) {
                lastPeriod = signalCorrelator.getPeriod();
                if (lastPeriod < 0.1) {
                    System.out.println("ILLEGAL PERIOD");
                }
                double currentFrequency = getFrameRate() / (lastPeriod + 0);
                double confidence = signalCorrelator.getConfidence();
                if (confidence > 0.1) {
                    if (true) {
                        double coefficient = confidence * 0.2;
                        // Take weighted average with previous frequency.
                        lastFrequency = ((lastFrequency * (1.0 - coefficient)) + (currentFrequency * coefficient));
                    } else {
                        lastFrequency = ((lastFrequency * lastConfidence) + (currentFrequency * confidence))
                                / (lastConfidence + confidence);
                    }
                }
                lastConfidence = confidence;
                updateds[i] = 1.0;
            } else {
                updateds[i] = 0.0;
            }
            periods[i] = lastPeriod;
            confidences[i] = lastConfidence;
            frequencies[i] = lastFrequency;
        }
    }

    /**
     * For debugging only.
     * 
     * @return internal array of correlation results.
     */
    public float[] getDiffs() {
        return signalCorrelator.getDiffs();
    }

}
