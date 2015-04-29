/*
 * Copyright 2004 Phil Burk, Mobileer Inc
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

/**
 * Calculate period of a repeated waveform in an array. This algorithm is based on a normalized
 * auto-correlation function as dewscribed in: "A Smarter Way to Find Pitch" by Philip McLeod and
 * Geoff Wyvill
 * 
 * @author (C) 2004 Mobileer, PROPRIETARY and CONFIDENTIAL
 */
public class AutoCorrelator implements SignalCorrelator {
    // A higher number will reject suboctaves more.
    private static final float SUB_OCTAVE_REJECTION_FACTOR = 0.0005f;
    // We can focus our analysis on the maxima
    private static final int STATE_SEEKING_NEGATIVE = 0;
    private static final int STATE_SEEKING_POSITIVE = 1;
    private static final int STATE_SEEKING_MAXIMUM = 2;
    private static final int[] tauAdvanceByState = {
            4, 2, 1
    };
    private int state;

    private float[] buffer;
    // double buffer the diffs so we can view them
    private float[] diffs;
    private float[] diffs1;
    private float[] diffs2;
    private int cursor = -1;
    private int tau;

    private float sumProducts;
    private float sumSquares;
    private float localMaximum;
    private int localPosition;
    private float bestMaximum;
    private int bestPosition;
    private int peakCounter;
    // This factor was found empirically to reduce a systematic offset in the pitch.
    private float pitchCorrectionFactor = 0.99988f;

    // Results of analysis.
    private double period;
    private double confidence;
    private int minPeriod = 2;
    private boolean bufferValid;
    private double previousSample = 0.0;
    private int maxWindowSize;
    private float noiseThreshold = 0.001f;

    public AutoCorrelator(int numFrames) {
        buffer = new float[numFrames];
        maxWindowSize = buffer.length / 2;
        diffs1 = new float[2 + numFrames / 2];
        diffs2 = new float[diffs1.length];
        diffs = diffs1;
        period = minPeriod;
        reset();
    }

    // Scan assuming we will not wrap around the buffer.
    private void rawDeltaScan(int last1, int last2, int count, int stride) {
        for (int k = 0; k < count; k += stride) {
            float d1 = buffer[last1 - k];
            float d2 = buffer[last2 - k];
            sumProducts += d1 * d2;
            sumSquares += ((d1 * d1) + (d2 * d2));
        }
    }

    // Do correlation when we know the splitLast will wrap around.
    private void splitDeltaScan(int last1, int splitLast, int count, int stride) {
        int c1 = splitLast;
        rawDeltaScan(last1, splitLast, c1, stride);
        rawDeltaScan(last1 - c1, buffer.length - 1, count - c1, stride);
    }

    private void checkDeltaScan(int last1, int last2, int count, int stride) {
        if (count > last2) {
            int c1 = last2;
            // Use recursion with reverse indexes to handle a double split.
            checkDeltaScan(last2, last1, c1, stride);
            checkDeltaScan(buffer.length - 1, last1 - c1, count - c1, stride);
        } else if (count > last1) {
            splitDeltaScan(last2, last1, count, stride);
        } else {
            rawDeltaScan(last1, last2, count, stride);
        }
    }

    // Perform correlation. Handle circular buffer wrap around.
    // Normalized square difference function between -1.0 and +1.0.
    private float topScan(int last1, int tau, int count, int stride) {
        final float minimumResult = 0.00000001f;

        int last2 = last1 - tau;
        if (last2 < 0) {
            last2 += buffer.length;
        }
        sumProducts = 0.0f;
        sumSquares = 0.0f;
        checkDeltaScan(last1, last2, count, stride);
        // Prevent divide by zero.
        if (sumSquares < minimumResult) {
            return minimumResult;
        }
        float correction = (float) Math.pow(pitchCorrectionFactor, tau);
        float result = (float) (2.0 * sumProducts / sumSquares) * correction;

        return result;
    }

    // Prepare for a new calculation.
    private void reset() {
        switchDiffs();
        int i = 0;
        for (; i < minPeriod; i++) {
            diffs[i] = 1.0f;
        }
        for (; i < diffs.length; i++) {
            diffs[i] = 0.0f;
        }
        tau = minPeriod;
        state = STATE_SEEKING_NEGATIVE;
        peakCounter = 0;
        bestMaximum = -1.0f;
        bestPosition = -1;
    }

    // Analyze new diff result. Incremental peak detection.
    private void nextPeakAnalysis(int index) {
        // Scale low frequency correlation down to reduce suboctave matching.
        // Note that this has a side effect of reducing confidence value for low frequency sounds.
        float value = diffs[index] * (1.0f - (index * SUB_OCTAVE_REJECTION_FACTOR));
        switch (state) {
            case STATE_SEEKING_NEGATIVE:
                if (value < -0.01f) {
                    state = STATE_SEEKING_POSITIVE;
                }
                break;
            case STATE_SEEKING_POSITIVE:
                if (value > 0.2f) {
                    state = STATE_SEEKING_MAXIMUM;
                    localMaximum = value;
                    localPosition = index;
                }
                break;
            case STATE_SEEKING_MAXIMUM:
                if (value > localMaximum) {
                    localMaximum = value;
                    localPosition = index;
                } else if (value < -0.1f) {
                    peakCounter += 1;
                    if (localMaximum > bestMaximum) {
                        bestMaximum = localMaximum;
                        bestPosition = localPosition;
                    }
                    state = STATE_SEEKING_POSITIVE;
                }
                break;
        }
    }

    /**
     * Generate interpolated maximum from index of absolute maximum using three point analysis.
     */
    private double findPreciseMaximum(int indexMax) {
        if (indexMax < 3) {
            return 3.0;
        }
        if (indexMax == (diffs.length - 1)) {
            return indexMax;
        }
        // Get 3 adjacent values.
        double d1 = diffs[indexMax - 1];
        double d2 = diffs[indexMax];
        double d3 = diffs[indexMax + 1];

        return interpolatePeak(d1, d2, d3) + indexMax;
    }

    // Use quadratic fit to return offset between -0.5 and +0.5 from center.
    protected static double interpolatePeak(double d1, double d2, double d3) {
        return 0.5 * (d1 - d3) / (d1 - (2.0 * d2) + d3);
    }

    // Calculate a little more for each sample.
    // This spreads the CPU load out more evenly.
    private boolean incrementalAnalysis() {
        boolean updated = false;
        if (bufferValid) {
            // int windowSize = maxWindowSize;
            // Interpolate between tau and maxWindowsSize based on confidence.
            // If confidence is low then use bigger window.
            int windowSize = (int) ((tau * confidence) + (maxWindowSize * (1.0 - confidence)));

            int stride = 1;
            // int stride = (windowSize / 32) + 1;

            diffs[tau] = topScan(cursor, tau, windowSize, stride);

            // Check to see if the signal is strong enough to analyze.
            // Look at sumPeriods on first correlation.
            if ((tau == minPeriod) && (sumProducts < noiseThreshold)) {
                // Update if we are dropping to zero confidence.
                boolean result = (confidence > 0.0);
                confidence = 0.0;
                return result;
            }

            nextPeakAnalysis(tau);

            // Reuse calculated values if we are not near a peak.
            tau += 1;
            int advance = tauAdvanceByState[state] - 1;
            while ((advance > 0) && (tau < diffs.length)) {
                diffs[tau] = diffs[tau - 1];
                tau++;
                advance--;
            }

            if ((peakCounter >= 4) || (tau >= maxWindowSize)) {
                if (bestMaximum > 0.0) {
                    period = findPreciseMaximum(bestPosition);
                    // clip into range 0.0 to 1.0, low values are really bogus
                    confidence = (bestMaximum < 0.0) ? 0.0 : bestMaximum;
                } else {
                    confidence = 0.0;
                }
                updated = true;
                reset();
            }
        }
        return updated;
    }

    @Override
    public float[] getDiffs() {
        // Return diffs that are not currently being used
        return (diffs == diffs1) ? diffs2 : diffs1;
    }

    private void switchDiffs() {
        diffs = (diffs == diffs1) ? diffs2 : diffs1;
    }

    @Override
    public boolean addSample(double value) {
        double average = (value + previousSample) * 0.5;
        previousSample = value;

        cursor += 1;
        if (cursor == buffer.length) {
            cursor = 0;
            bufferValid = true;
        }
        buffer[cursor] = (float) average;

        return incrementalAnalysis();
    }

    @Override
    public double getPeriod() {
        return period;
    }

    @Override
    public double getConfidence() {
        return confidence;
    }

    public float getPitchCorrectionFactor() {
        return pitchCorrectionFactor;
    }

    public void setPitchCorrectionFactor(float pitchCorrectionFactor) {
        this.pitchCorrectionFactor = pitchCorrectionFactor;
    }
}
