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

package com.jsyn.unitgen;

import com.jsyn.dsp.BiquadLowPass;
import com.jsyn.ports.UnitInputPort;

/**
 * Base class for Karplus-Strong style string resonators.
 * <p>
 * This is a {@link FilterComb} with a two-pole Butterworth low pass filter in the loop. Use any
 * signal on the input port as the excitation, for example a burst of noise, an impulse, a sample
 * or live audio.
 * <p>
 * The frequency port sets the fundamental. All of the internal delays are included in the tuning.
 * The decay port sets the time in seconds for the fundamental to decay by 60 dB. Lowering the
 * decay while the string rings acts like a damper.
 * <p>
 * The brightness port controls how quickly the upper harmonics decay relative to the fundamental.
 * It sets the cutoff of the loop filter relative to the fundamental, so the tone is similar at
 * every pitch. The filter is nearly flat up to the fundamental, so decay and brightness are mostly
 * independent. The loop gain must stay below 1.0, so a very long decay with a dark setting may
 * decay sooner than requested, especially at high pitches.
 *
 * @author Phil Burk (C) 2026 Mobileer Inc
 * @see FixedStringResonator
 * @see VariableStringResonator
 */
public abstract class StringResonator extends FilterComb {
    /** Loop filter cutoff, relative to the fundamental, at brightness 0.0 and 1.0. */
    private static final double MIN_CUTOFF_RATIO = 3.0;
    private static final double MAX_CUTOFF_RATIO = 256.0;

    /**
     * 0.0 is dark, like a nylon string, with the loop filter cutoff at 3 times the fundamental.
     * 1.0 is bright, like a steel string, with the cutoff at 256 times the fundamental.
     */
    public final UnitInputPort brightness;

    private final BiquadLowPass loopFilter = new BiquadLowPass();
    private double filterFrequency = -1.0;
    private double filterBrightness = -1.0;

    /**
     * @param minFrequency lowest frequency supported, determines the size of the delay line
     * @param interpolation how the fractional delay is calculated
     */
    protected StringResonator(double minFrequency, Interpolation interpolation) {
        super(minFrequency, interpolation);
        addPort(brightness = new UnitInputPort("Brightness", 0.5));
        brightness.setup(0.0, 0.5, 1.0);
    }

    @Override
    protected boolean updateLoopFilter(double frequency, double omega) {
        double currentBrightness = brightness.getValues()[0];
        if (frequency == filterFrequency && currentBrightness == filterBrightness) {
            return false;
        }
        double clipped = Math.max(0.0, Math.min(currentBrightness, 1.0));
        double ratio = MIN_CUTOFF_RATIO * Math.pow(MAX_CUTOFF_RATIO / MIN_CUTOFF_RATIO, clipped);
        loopFilter.setCutoff(frequency * ratio, getFrameRate());
        filterFrequency = frequency;
        filterBrightness = currentBrightness;
        return true;
    }

    @Override
    protected float filterLoop(float input) {
        return loopFilter.process(input);
    }

    @Override
    protected double loopFilterDelayAt(double omega) {
        return loopFilter.phaseDelayAt(omega);
    }

    @Override
    protected double loopFilterGainAt(double omega) {
        return loopFilter.gainAt(omega);
    }

    @Override
    protected void clearLoopFilter() {
        loopFilter.clear();
    }
}
