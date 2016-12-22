/*
 * Copyright 2009 Phil Burk, Mobileer Inc
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

/**
 * A versatile filter described in Hal Chamberlain's "Musical Applications of MicroProcessors". It
 * is convenient because its frequency and resonance can each be controlled by a single value. The
 * "output" port of this filter is the "lowPass" output multiplied by the "amplitude"
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @see FilterLowPass
 * @see FilterHighPass
 * @see FilterFourPoles
 */
public class FilterStateVariable extends TunableFilter {
    /**
     * Amplitude of Output in the range of 0.0 to 1.0. SIGNAL_TYPE_RAW_SIGNED Defaults to 1.0
     * <P>
     * Note that the amplitude only affects the "output" port and not the lowPass, bandPass or
     * highPass signals. Use a Multiply unit if you need to scale those signals.
     */
    public UnitInputPort amplitude;

    /**
     * Controls feedback that causes self oscillation. Actually 1/Q - SIGNAL_TYPE_RAW_SIGNED in the
     * range of 0.0 to 1.0. Defaults to 0.125.
     */
    public UnitInputPort resonance;
    /**
     * Low pass filtered signal.
     * <P>
     * Note that this signal is not affected by the amplitude port.
     */
    public UnitOutputPort lowPass;
    /**
     * Band pass filtered signal.
     * <P>
     * Note that this signal is not affected by the amplitude port.
     */
    public UnitOutputPort bandPass;
    /**
     * High pass filtered signal.
     * <P>
     * Note that this signal is not affected by the amplitude port.
     */
    public UnitOutputPort highPass;

    private double freqInternal;
    private double previousFrequency = Double.MAX_VALUE; // So we trigger an immediate update.
    private double lowPassValue;
    private double bandPassValue;

    /**
     * No-argument constructor instantiates the Biquad common and adds an amplitude port to this
     * filter.
     */
    public FilterStateVariable() {
        frequency.set(440.0);
        addPort(resonance = new UnitInputPort("Resonance", 0.2));
        addPort(amplitude = new UnitInputPort("Amplitude", 1.0));
        addPort(lowPass = new UnitOutputPort("LowPass"));
        addPort(bandPass = new UnitOutputPort("BandPass"));
        addPort(highPass = new UnitOutputPort("HighPass"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();
        double[] frequencies = frequency.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] reses = resonance.getValues();
        double[] lows = lowPass.getValues();
        double[] highs = highPass.getValues();
        double[] bands = bandPass.getValues();

        double newFreq = frequencies[0];
        if (newFreq != previousFrequency) {
            previousFrequency = newFreq;
            freqInternal = 2.0 * Math.sin(Math.PI * newFreq * getFramePeriod());
        }

        for (int i = start; i < limit; i++) {
            lowPassValue = (freqInternal * bandPassValue) + lowPassValue;
            // Clip between -1 and +1 to prevent blowup.
            lowPassValue = (lowPassValue < -1.0) ? -1.0 : ((lowPassValue > 1.0) ? 1.0
                    : lowPassValue);
            lows[i] = lowPassValue;

            outputs[i] = lowPassValue * (amplitudes[i]);
            double highPassValue = inputs[i] - (reses[i] * bandPassValue) - lowPassValue;
            // System.out.println("low = " + lowPassValue + ", band = " + bandPassValue +
            // ", high = " + highPassValue );
            highs[i] = highPassValue;

            bandPassValue = (freqInternal * highPassValue) + bandPassValue;
            bands[i] = bandPassValue;
            // System.out.println("low = " + lowPassValue + ", band = " + bandPassValue +
            // ", high = " + highPassValue );
        }
    }

}
