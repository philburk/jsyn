/*
 * Copyright 2014 Phil Burk, Mobileer Inc
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
 * Resonant filter in the style of the Moog ladder filter. This implementation is loosely based on:
 * http://www.musicdsp.org/archive.php?classid=3#26
 * More interesting reading:
 * http://dafx04.na.infn.it/WebProc/Proc/P_061.pdf
 * http://www.acoustics.ed.ac.uk/wp-content/uploads/AMT_MSc_FinalProjects
 * /2012__Daly__AMT_MSc_FinalProject_MoogVCF.pdf
 * http://www.music.mcgill.ca/~ich/research/misc/papers/cr1071.pdf
 *
 * @author Phil Burk (C) 2014 Mobileer Inc
 * @see FilterLowPass
 */
public class FilterFourPoles extends TunableFilter {
    public UnitInputPort Q;
    public UnitInputPort gain;

    private static final double MINIMUM_FREQUENCY = 1.0; // blows up if near 0.01
    private static final double MINIMUM_Q = 0.00001;

    //private static final double SATURATION_COEFFICIENT = 0.1666667;
    private static final double SATURATION_COEFFICIENT = 0.2;
    // Inflection point where slope is zero.
    private static final double SATURATION_UPPER_INPUT = 1.0 / Math.sqrt(3.0 * SATURATION_COEFFICIENT);
    private static final double SATURATION_LOWER_INPUT = 0.0 - SATURATION_UPPER_INPUT;
    private static final double SATURATION_UPPER_OUTPUT = cubicPolynomial(SATURATION_UPPER_INPUT);
    private static final double SATURATION_LOWER_OUTPUT = cubicPolynomial(SATURATION_LOWER_INPUT);

    private double x1;
    private double x2;
    private double x3;
    private double x4;
    private double y1;
    private double y2;
    private double y3;
    private double y4;

    private double previousFrequency;
    private double previousQ;
    // filter coefficients
    private double f;
    private double fTo4th;
    private double feedback;

    private boolean oversampled = true;

    public FilterFourPoles() {
        addPort(Q = new UnitInputPort("Q"));
        frequency.setup(40.0, DEFAULT_FREQUENCY, 4000.0);
        Q.setup(0.1, 2.0, 10.0);
    }

    /**
     * The recalculate() method checks and ensures that the frequency and Q values are at a minimum.
     * It also only updates the coefficients if either frequency or Q have changed.
     */
    public void recalculate() {
        double frequencyValue = frequency.getValues()[0];
        double qValue = Q.getValues()[0];

        if (frequencyValue < MINIMUM_FREQUENCY) // ensure a minimum frequency
        {
            frequencyValue = MINIMUM_FREQUENCY;
        }
        if (qValue < MINIMUM_Q) // ensure a minimum Q
        {
            qValue = MINIMUM_Q;
        }

        // Only recompute coefficients if changed.
        if ((frequencyValue != previousFrequency) || (qValue != previousQ)) {
            previousFrequency = frequencyValue;
            previousQ = qValue;
            computeCoefficients();
        }
    }

    private void computeCoefficients() {
        double normalizedFrequency = previousFrequency * getFramePeriod();
        double fudge = 4.9 - 0.27 * previousQ;
        if (fudge < 3.0)
            fudge = 3.0;
        f = normalizedFrequency * (oversampled ? 1.0 : 2.0) * fudge;

        double fSquared = f * f;
        fTo4th = fSquared * fSquared;
        feedback = 0.5 * previousQ * (1.0 - 0.15 * fSquared);
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        recalculate();

        for (int i = start; i < limit; i++) {
            double x0 = inputs[i];

            if (oversampled) {
                oneSample(0.0);
            }
            oneSample(x0);
            outputs[i] = y4;
        }

        // apply small bipolar impulse to prevent arithmetic underflow
        y1 += VERY_SMALL_FLOAT;
        y2 -= VERY_SMALL_FLOAT;
    }

    private void oneSample(double x0) {
        final double coeff = 0.3;
        x0 -= y4 * feedback; // feedback
        x0 *= 0.35013 * fTo4th;
        y1 = x0 + coeff * x1 + (1 - f) * y1; // pole 1
        x1 = x0;
        y2 = y1 + coeff * x2 + (1 - f) * y2; // pole 2
        x2 = y1;
        y3 = y2 + coeff * x3 + (1 - f) * y3; // pole 3
        x3 = y2;
        y4 = y3 + coeff * x4 + (1 - f) * y4; // pole 4
        y4 = clip(y4);
        x4 = y3;
    }

    public boolean isOversampled() {
        return oversampled;
    }

    public void setOversampled(boolean oversampled) {
        this.oversampled = oversampled;
    }

    // Soft saturation. This used to blow up the filter!
    private static double cubicPolynomial(double x) {
        return x - (x * x * x * SATURATION_COEFFICIENT);
    }

    private static double clip(double x) {
        if (x > SATURATION_UPPER_INPUT) {
            return SATURATION_UPPER_OUTPUT;
        } else if (x < SATURATION_LOWER_INPUT) {
            return SATURATION_LOWER_OUTPUT;
        } else {
            return cubicPolynomial(x);
        }
    }

    public void reset() {
        x1 = 0.0;
        x2 = 0.0;
        x3 = 0.0;
        x4 = 0.0;
        y1 = 0.0;
        y2 = 0.0;
        y3 = 0.0;
        y4 = 0.0;

        previousFrequency = 0.0;
        previousQ = 0.0;
        f = 0.0;
        fTo4th = 0.0;
        feedback = 0.0;
    }
}
