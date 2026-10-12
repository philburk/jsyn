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

import com.jsyn.dsp.AllPassFractionalDelay;
import com.jsyn.dsp.FractionalDelay;
import com.jsyn.dsp.LagrangeFractionalDelay;
import com.jsyn.ports.UnitInputPort;

/**
 * Feedback comb filter, tuned to a frequency.
 * <p>
 * The output is the input plus a delayed copy of the output:
 *
 * <pre>
 * y(n) = x(n) + gain * y(n - frameRate / frequency)
 * </pre>
 *
 * It resonates at the frequency and all of its harmonics. The delay can be a fractional number of
 * frames, so the tuning is exact. The feedback loop runs one frame at a time inside this unit, so
 * the block size adds no latency.
 * <p>
 * The decay port sets the time in seconds for the resonance to decay by 60 dB. The gain is
 * calculated from the decay and the frequency.
 * <p>
 * The interpolation chooses how the fractional delay is calculated. Use
 * {@link Interpolation#LAGRANGE} if the frequency will change smoothly, for example with an LFO.
 * Use {@link Interpolation#ALL_PASS} for fixed frequencies, because it has a perfectly flat gain.
 *
 * @author Phil Burk (C) 2026 Mobileer Inc
 * @see StringResonator
 */
public class FilterComb extends UnitFilter implements SupportsAutoDisable {
    public static final double DEFAULT_MIN_FREQUENCY = 40.0;

    /** The delay line is allocated so that minFrequency works up to this frame rate. */
    public static final double MAX_FRAME_RATE = 96000.0;

    /** Highest frequency is frameRate / MIN_PERIOD. */
    public static final double MIN_PERIOD = 4.0;

    /** Keeps the loop out of the slow denormal range as it decays. Inaudible at about -360 dB. */
    private static final double DENORMAL_OFFSET = 1.0e-18;

    private static final double MAX_LOOP_GAIN = 0.99999;

    public enum Interpolation {
        /** First-order allpass. Flat gain, but changing the frequency can cause a small transient. */
        ALL_PASS,

        /** 3rd order Lagrange. Smooth when the frequency changes, with a little high frequency loss. */
        LAGRANGE
    }

    /** Fundamental frequency in Hz. */
    public final UnitInputPort frequency;

    /** Time in seconds for the fundamental to decay by 60 dB. */
    public final UnitInputPort decay;

    private final double minFrequency;
    private final Interpolation interpolation;
    private final FractionalDelay fractionalDelay;
    private double loopGain;
    private double denormalOffset = DENORMAL_OFFSET;

    /** Delay of the loop filter at the fundamental, in frames. */
    private double filterDelay;

    /** Range of frequencies that fit in the delay line at the current frame rate. */
    private double lowestFrequency;
    private double highestFrequency = 1000.0;

    // Parameters for the current block. Recalculate only when they change.
    private double blockFrequency = -1.0;
    private double blockDecay = -1.0;

    private UnitGenerator autoDisableTarget;
    private boolean autoDisableEnabled;
    private double autoDisableThreshold = 3.0e-5;
    private double autoDisableQuietTime = 0.01;
    private int quietFrames;

    public FilterComb() {
        this(DEFAULT_MIN_FREQUENCY, Interpolation.LAGRANGE);
    }

    /**
     * @param interpolation how the fractional delay is calculated
     */
    public FilterComb(Interpolation interpolation) {
        this(DEFAULT_MIN_FREQUENCY, interpolation);
    }

    /**
     * @param minFrequency lowest frequency supported, determines the size of the delay line
     */
    public FilterComb(double minFrequency) {
        this(minFrequency, Interpolation.LAGRANGE);
    }

    /**
     * @param minFrequency lowest frequency supported, determines the size of the delay line
     * @param interpolation how the fractional delay is calculated
     */
    public FilterComb(double minFrequency, Interpolation interpolation) {
        this.minFrequency = minFrequency;
        this.interpolation = interpolation;
        lowestFrequency = minFrequency;
        int maxDelay = (int) Math.ceil(MAX_FRAME_RATE / minFrequency);
        switch (interpolation) {
            case ALL_PASS:
                fractionalDelay = new AllPassFractionalDelay(maxDelay);
                break;
            case LAGRANGE:
            default:
                fractionalDelay = new LagrangeFractionalDelay(maxDelay);
                break;
        }
        addPort(frequency = new UnitInputPort("Frequency", 220.0));
        addPort(decay = new UnitInputPort("Decay", 2.0));
        frequency.setup(minFrequency, 220.0, 2000.0);
        decay.setup(0.01, 2.0, 30.0);
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    /**
     * Called at the start of every block so a subclass can update a filter inside the loop.
     *
     * @param frequency fundamental, already limited to the supported range
     * @param omega radian frequency of the fundamental, 2 * PI * frequency / frameRate
     * @return true if the filter changed, so that the tuning and gain are recalculated
     */
    protected boolean updateLoopFilter(double frequency, double omega) {
        return false;
    }

    /** Filter one frame inside the loop. The default does nothing. */
    protected float filterLoop(float input) {
        return input;
    }

    /** @return delay of the loop filter in frames at this radian frequency */
    protected double loopFilterDelayAt(double omega) {
        return 0.0;
    }

    /** @return gain of the loop filter at this radian frequency */
    protected double loopFilterGainAt(double omega) {
        return 1.0;
    }

    /** Set the state of the loop filter to zero. */
    protected void clearLoopFilter() {
    }

    /**
     * @return the delay in frames that the delay line must provide so that the whole loop is
     *     tuned to the frequency
     */
    private double delayFor(double frequency) {
        // The filter delay is calculated once per block, at the fundamental.
        return (getFrameRate() / frequency) - filterDelay;
    }

    private double limitFrequency(double frequency) {
        return Math.max(lowestFrequency, Math.min(frequency, highestFrequency));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();
        double[] frequencies = frequency.getValues();

        updateLoop(frequencies[start]);

        // Alternate the sign so the offset has no DC to build up in the loop.
        denormalOffset = -denormalOffset;
        boolean followFrequency = fractionalDelay.isSmooth();
        double peak = 0.0;
        for (int i = start; i < limit; i++) {
            if (followFrequency) {
                fractionalDelay.setDelay(delayFor(limitFrequency(frequencies[i])), 0.0);
            }
            double feedback = loopGain * filterLoop(fractionalDelay.read());
            double excitation = inputs[i];
            double x = excitation + feedback + denormalOffset;
            fractionalDelay.write((float) x);
            outputs[i] = x;
            peak = Math.max(peak, Math.max(Math.abs(x), Math.abs(excitation)));
        }

        if (autoDisableEnabled) {
            checkAutoDisable(peak, limit - start);
        }
    }

    private void updateLoop(double rawFrequency) {
        double frameRate = getFrameRate();
        lowestFrequency = Math.max(minFrequency, frameRate / fractionalDelay.getMaxDelay());
        highestFrequency = frameRate / MIN_PERIOD;
        double currentFrequency = limitFrequency(rawFrequency);
        double currentDecay = decay.getValues()[0];
        double omega = 2.0 * Math.PI * currentFrequency / frameRate;

        boolean filterChanged = updateLoopFilter(currentFrequency, omega);
        boolean toneChanged = filterChanged || (currentFrequency != blockFrequency);
        if (toneChanged) {
            filterDelay = loopFilterDelayAt(omega);
            fractionalDelay.setDelay(delayFor(currentFrequency), omega);
        }
        if (toneChanged || (currentDecay != blockDecay)) {
            if (currentDecay <= 0.0) {
                loopGain = 0.0;
            } else {
                // Loss per trip so the fundamental decays 60 dB in currentDecay seconds.
                double loss = Math.pow(10.0, -3.0 / (currentDecay * currentFrequency));
                // Compensate for the filter loss at the fundamental, but never let the loop gain
                // reach 1.0.
                loopGain = Math.min(MAX_LOOP_GAIN, loss / loopFilterGainAt(omega));
            }
        }
        blockFrequency = currentFrequency;
        blockDecay = currentDecay;
    }

    private void checkAutoDisable(double peak, int numFrames) {
        if (peak < autoDisableThreshold) {
            quietFrames += numFrames;
            // Wait at least one period so that a pulse traveling around the loop is not missed.
            double frameRate = getFrameRate();
            double period = frameRate / Math.max(blockFrequency, lowestFrequency);
            double framesNeeded = Math.max(period, autoDisableQuietTime * frameRate);
            if (quietFrames >= framesNeeded) {
                clear();
                ((autoDisableTarget == null) ? this : autoDisableTarget).setEnabled(false);
            }
        } else {
            quietFrames = 0;
        }
    }

    /**
     * Disable the unit when the loop becomes quiet. This can be used to reduce CPU load. The unit
     * starts off disabled.
     * <p>
     * A disabled unit is not run, so it cannot notice a new input signal. Whatever starts the
     * input must enable the unit again, for example in noteOn().
     *
     * @param unit the unit to disable, for example a Circuit containing this unit
     */
    @Override
    public void setupAutoDisable(UnitGenerator unit) {
        autoDisableTarget = unit;
        autoDisableEnabled = true;
        quietFrames = 0;
        unit.setEnabled(false);
    }

    @Override
    public boolean isAutoDisableEnabled() {
        return autoDisableEnabled;
    }

    @Override
    public void setAutoDisableEnabled(boolean enabled) {
        boolean wasEnabled = autoDisableEnabled;
        quietFrames = 0;
        autoDisableEnabled = enabled;
        if (wasEnabled && !enabled) {
            // Nothing would enable it again, so let it run.
            ((autoDisableTarget == null) ? this : autoDisableTarget).setEnabled(true);
        }
    }

    public double getAutoDisableThreshold() {
        return autoDisableThreshold;
    }

    /**
     * @param threshold the unit is disabled when the signal in the loop stays below this level.
     *     Default is -90 dB.
     */
    public void setAutoDisableThreshold(double threshold) {
        this.autoDisableThreshold = threshold;
    }

    public double getAutoDisableQuietTime() {
        return autoDisableQuietTime;
    }

    /**
     * @param seconds minimum time that the loop must be quiet before it is disabled
     */
    public void setAutoDisableQuietTime(double seconds) {
        this.autoDisableQuietTime = seconds;
    }

    /** Silence the resonance by clearing the delay line and filters. */
    public void clear() {
        fractionalDelay.clear();
        clearLoopFilter();
        quietFrames = 0;
    }
}
