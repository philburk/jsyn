/*
 * Copyright 2026 Phil Burk
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

package com.jsyn.dsp;

/**
 * One-pole low pass filter with a gain of 1.0 at DC:
 *
 * <pre>
 * H(z) = (1 - pole) / (1 - pole * z^-1)
 * </pre>
 *
 * Its delay varies with frequency, so use {@link #phaseDelayAt(double)} when it is inside a
 * tuned loop.
 */
public class OnePoleLowPass {
    private float pole;
    private float y1;

    /**
     * @param pole between 0.0 (no filtering) and just below 1.0 (very dark)
     */
    public OnePoleLowPass(float pole) {
        this.pole = pole;
    }

    public OnePoleLowPass() {
        this(0.0f);
    }

    public float process(float input) {
        float p = pole;
        y1 = ((1.0f - p) * input) + (p * y1);
        return y1;
    }

    /** Set the internal state to zero. */
    public void clear() {
        y1 = 0.0f;
    }

    public float getPole() {
        return pole;
    }

    /**
     * This can be changed at any time.
     *
     * @param pole between 0.0 and just below 1.0
     */
    public void setPole(float pole) {
        this.pole = pole;
    }

    /**
     * Set the pole for an approximate -3 dB cutoff frequency.
     *
     * @param cutoff in Hz
     * @param frameRate in Hz
     */
    public void setCutoff(double cutoff, double frameRate) {
        pole = (float) Math.exp(-2.0 * Math.PI * cutoff / frameRate);
    }

    /**
     * @param omega radian frequency, 2 * PI * frequency / frameRate
     * @return magnitude of the frequency response, 1.0 at DC
     */
    public double gainAt(double omega) {
        double p = pole;
        return (1.0 - p) / Math.sqrt(1.0 - (2.0 * p * Math.cos(omega)) + (p * p));
    }

    /**
     * @param omega radian frequency, 2 * PI * frequency / frameRate, greater than zero
     * @return delay in frames of a sine wave at that frequency
     */
    public double phaseDelayAt(double omega) {
        double p = pole;
        return Math.atan(p * Math.sin(omega) / (1.0 - (p * Math.cos(omega)))) / omega;
    }
}
