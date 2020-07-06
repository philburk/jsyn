/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

/**
 * Base class for a set of IIR filters.
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 * @see FilterBandStop
 * @see FilterBandPass
 * @see FilterLowPass
 * @see FilterHighPass
 * @see FilterTwoPolesTwoZeros
 */
public abstract class FilterBiquad extends TunableFilter {
    public UnitInputPort amplitude;

    protected static final double MINIMUM_FREQUENCY = 0.00001;
    protected static final double MINIMUM_GAIN = 0.00001;
    protected static final double RATIO_MINIMUM = 0.499;
    protected double a0;
    protected double a1;
    protected double a2;
    protected double b1;
    protected double b2;
    private double x1;
    private double x2;
    private double y1;
    private double y2;
    protected double previousFrequency;
    protected double omega;
    protected double sin_omega;
    protected double cos_omega;

    public FilterBiquad() {
        addPort(amplitude = new UnitInputPort("Amplitude", 1.0));
    }

    /**
     * Generic generate(int start, int limit) method calls this filter's recalculate() and
     * performBiquadFilter(int, int) methods.
     */
    @Override
    public void generate(int start, int limit) {
        recalculate();
        performBiquadFilter(start, limit);
    }

    protected abstract void recalculate();

    /**
     * Each filter calls performBiquadFilter() through the generate(int, int) method. This method
     * has converted Robert Bristow-Johnson's coefficients for the Direct I form in this way: Here
     * is the equation that JSyn uses for this filter:
     *
     * <pre>
     * y(n) = A0*x(n) + A1*x(n-1) + A2*x(n-2) -vB1*y(n-1) - B2*y(n-2)
     * </pre>
     *
     * Here is the equation that Robert Bristow-Johnson uses:
     *
     * <pre>
     * y[n] = (b0/a0)*x[n] + (b1/a0)*x[n-1] + (b2/a0)*x[n-2] - (a1/a0)*y[n-1] - (a2/a0)*y[n-2]
     * </pre>
     *
     * So to translate between JSyn coefficients and RBJ coefficients:
     *
     * <pre>
     * JSyn =&gt; RBJ
     * A0 =&gt; b0/a0
     * A1 =&gt; b1/a0
     * A2 =&gt; b2/a0
     * B1 =&gt; a1/a0
     * B2 =&gt; a2/a0
     * </pre>
     *
     * @param start
     * @param limit
     */
    public void performBiquadFilter(int start, int limit) {
        double[] inputs = input.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();
        double a0_jsyn, a1_jsyn, a2_jsyn, b1_jsyn, b2_jsyn;
        double x0_jsyn, x1_jsyn, x2_jsyn, y1_jsyn, y2_jsyn;

        x1_jsyn = this.x1;
        x2_jsyn = this.x2;

        y1_jsyn = this.y1;
        y2_jsyn = this.y2;

        a0_jsyn = this.a0;
        a1_jsyn = this.a1;
        a2_jsyn = this.a2;

        b1_jsyn = this.b1;
        b2_jsyn = this.b2;

        // Permute filter operations to reduce data movement.
        for (int i = start; i < limit; i += 2)

        {
            x0_jsyn = inputs[i];
            y2_jsyn = (a0_jsyn * x0_jsyn) + (a1_jsyn * x1_jsyn) + (a2_jsyn * x2_jsyn)
                    - (b1_jsyn * y1_jsyn) - (b2_jsyn * y2_jsyn);

            outputs[i] = amplitudes[i] * y2_jsyn;

            x2_jsyn = inputs[i + 1];
            y1_jsyn = (a0_jsyn * x2_jsyn) + (a1_jsyn * x0_jsyn) + (a2_jsyn * x1_jsyn)
                    - (b1_jsyn * y2_jsyn) - (b2_jsyn * y1_jsyn);

            outputs[i + 1] = amplitudes[i + 1] * y1_jsyn;

            x1_jsyn = x2_jsyn;
            x2_jsyn = x0_jsyn;
        }

        this.x1 = x1_jsyn; // save filter state for next time
        this.x2 = x2_jsyn;

        // apply small bipolar impulse to prevent arithmetic underflow
        this.y1 = y1_jsyn + VERY_SMALL_FLOAT;
        this.y2 = y2_jsyn - VERY_SMALL_FLOAT;
    }

    protected void calculateOmega(double ratio) {
        if (ratio >= FilterBiquad.RATIO_MINIMUM) // keep a minimum
        // distance from Nyquist
        {
            ratio = FilterBiquad.RATIO_MINIMUM;
        }

        omega = 2.0 * Math.PI * ratio;
        cos_omega = Math.cos(omega); // compute cosine
        sin_omega = Math.sin(omega); // compute sine
    }

}
