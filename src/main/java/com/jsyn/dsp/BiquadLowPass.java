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
 * Two-pole low pass filter with a gain of 1.0 at DC. The default Q of 0.7071 gives a Butterworth
 * response, which is as flat as possible below the cutoff.
 * <p>
 * The state is kept in double precision because the poles get very close to 1.0 when the cutoff
 * is low. Its delay varies with frequency, so use {@link #phaseDelayAt(double)} when it is inside
 * a tuned loop.
 */
public class BiquadLowPass {
    public static final double BUTTERWORTH_Q = 0.7071067811865476;

    /** The cutoff is limited to this fraction of the frame rate. */
    public static final double MAX_CUTOFF_RATIO = 0.45;

    private double b0 = 1.0;
    private double b1;
    private double b2;
    private double a1;
    private double a2;

    private double x1;
    private double x2;
    private double y1;
    private double y2;

    public float process(float input) {
        double x0 = input;
        double y0 = (b0 * x0) + (b1 * x1) + (b2 * x2) - (a1 * y1) - (a2 * y2);
        x2 = x1;
        x1 = x0;
        y2 = y1;
        y1 = y0;
        return (float) y0;
    }

    /** Set the internal state to zero. */
    public void clear() {
        x1 = 0.0;
        x2 = 0.0;
        y1 = 0.0;
        y2 = 0.0;
    }

    /**
     * Calculate a Butterworth response.
     *
     * @param cutoff in Hz, limited to MAX_CUTOFF_RATIO * frameRate
     * @param frameRate in Hz
     */
    public void setCutoff(double cutoff, double frameRate) {
        setCutoff(cutoff, frameRate, BUTTERWORTH_Q);
    }

    /**
     * Calculate coefficients using the Audio EQ Cookbook by Robert Bristow-Johnson.
     *
     * @param cutoff in Hz, limited to MAX_CUTOFF_RATIO * frameRate
     * @param frameRate in Hz
     * @param q resonance, 0.7071 for Butterworth
     */
    public void setCutoff(double cutoff, double frameRate, double q) {
        double limitedCutoff = Math.min(cutoff, MAX_CUTOFF_RATIO * frameRate);
        double omega = 2.0 * Math.PI * limitedCutoff / frameRate;
        double cosOmega = Math.cos(omega);
        double alpha = Math.sin(omega) / (2.0 * q);
        double a0 = 1.0 + alpha;
        b0 = ((1.0 - cosOmega) / 2.0) / a0;
        b1 = (1.0 - cosOmega) / a0;
        b2 = b0;
        a1 = (-2.0 * cosOmega) / a0;
        a2 = (1.0 - alpha) / a0;
    }

    /**
     * @param omega radian frequency, 2 * PI * frequency / frameRate
     * @return magnitude of the frequency response, 1.0 at DC
     */
    public double gainAt(double omega) {
        double[] numerator = evaluate(b0, b1, b2, omega);
        double[] denominator = evaluate(1.0, a1, a2, omega);
        return Math.hypot(numerator[0], numerator[1]) / Math.hypot(denominator[0], denominator[1]);
    }

    /**
     * @param omega radian frequency, 2 * PI * frequency / frameRate, greater than zero
     * @return delay in frames of a sine wave at that frequency
     */
    public double phaseDelayAt(double omega) {
        double[] numerator = evaluate(b0, b1, b2, omega);
        double[] denominator = evaluate(1.0, a1, a2, omega);
        double phase = Math.atan2(numerator[1], numerator[0])
                - Math.atan2(denominator[1], denominator[0]);
        // A two-pole low pass lags by 0 to 180 degrees.
        while (phase > 0.0) phase -= 2.0 * Math.PI;
        while (phase <= -2.0 * Math.PI) phase += 2.0 * Math.PI;
        return -phase / omega;
    }

    /** @return real and imaginary parts of c0 + c1 * e^-jw + c2 * e^-2jw */
    private static double[] evaluate(double c0, double c1, double c2, double omega) {
        double real = c0 + (c1 * Math.cos(omega)) + (c2 * Math.cos(2.0 * omega));
        double imaginary = -(c1 * Math.sin(omega)) - (c2 * Math.sin(2.0 * omega));
        return new double[] {
                real, imaginary
        };
    }
}
