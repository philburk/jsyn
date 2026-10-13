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
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.util.PseudoRandom;

/**
 * A unit generator that generates a cloud of sound using multiple Grains. Special thanks to my
 * friend Ross Bencina for his excellent article on Granular Synthesis. Several of his ideas are
 * reflected in this architecture. "Implementing Real-Time Granular Synthesis" by Ross Bencina,
 * Audio Anecdotes III, 2001.
 *
 * <pre><code>
   synth.add( sampleGrainFarm = new GrainFarm() );
   grainFarm.allocate( NUM_GRAINS );
</code></pre>
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 * @see Grain
 * @see GrainSourceSine
 * @see RaisedCosineEnvelope
 */
public class GrainFarm extends UnitGenerator implements UnitSource {
    /**
     * Choose a new gap when the density rises above this multiple of the density the gap was
     * chosen at.
     */
    private static final double GAP_RECHOOSE_RATIO = 2.0;

    /** Longest gap in frames, so a gap chosen at a density near zero still fits in an int. */
    private static final double MAX_GAP_FRAMES = 1.0e9;

    /** Used before the farm is added to a synthesizer. */
    private static final int DEFAULT_FRAME_RATE = 44100;

    /** A scaler for playback rate. Nominally 1.0. */
    public UnitInputPort rate;
    public UnitInputPort rateRange;
    public UnitInputPort amplitude;
    public UnitInputPort amplitudeRange;
    public UnitInputPort density;
    public UnitInputPort duration;
    public UnitInputPort durationRange;
    public UnitOutputPort output;

    PseudoRandom randomizer;
    private GrainState[] states;
    private double countScaler = 1.0;
    private final GrainScheduler scheduler = new StochasticGrainScheduler();

    public GrainFarm() {
        randomizer = new PseudoRandom();
        addPort(rate = new UnitInputPort("Rate", 1.0));
        addPort(amplitude = new UnitInputPort("Amplitude", 1.0));
        addPort(duration = new UnitInputPort("Duration", 0.01));
        addPort(rateRange = new UnitInputPort("RateRange", 0.0));
        addPort(amplitudeRange = new UnitInputPort("AmplitudeRange", 0.0));
        addPort(durationRange = new UnitInputPort("DurationRange", 0.0));
        addPort(density = new UnitInputPort("Density", 0.1));
        addPort(output = new UnitOutputPort());
    }

    private class GrainState {
        Grain grain;
        int countdown;
        double lastDuration;
        final static int STATE_IDLE = 0;
        final static int STATE_GAP = 1;
        final static int STATE_RUNNING = 2;
        int state = STATE_IDLE;
        private double gapError;
        /** The density when the current gap was chosen. */
        private double gapDensity;

        public double next(int i) {
            double output = 0.0;
            if (state == STATE_RUNNING) {
                if (grain.hasMoreValues()) {
                    output = grain.next();
                } else {
                    startGap(i);
                }
            } else if (state == STATE_GAP) {
                // A gap chosen at a very low density can be hours long. If the density has
                // since risen a lot, for example an envelope coming back up from zero, choose
                // a new gap rather than waiting out the old one.
                if (density.getValues()[i] > gapDensity * GAP_RECHOOSE_RATIO) {
                    startGap(i);
                }
                if (countdown > 0) {
                    countdown -= 1;
                } else if (countdown == 0) {
                    state = STATE_RUNNING;
                    grain.reset();

                    // The frame rate can change after allocate(), for example if the synth is
                    // restarted at another rate. Set it before the grain's rate and duration,
                    // which depend on it.
                    double currentFrameRate = getFrameRate();
                    if (grain.getFrameRate() != currentFrameRate) {
                        grain.setFrameRate(currentFrameRate);
                    }

                    setupGrain(grain, i);

                    double dur = nextDuration(i);
                    grain.setDuration(dur);
                }
            } else if (state == STATE_IDLE) {
                nextDuration(i); // initialize lastDuration
                startGap(i);
            }
            return output;
        }

        private double nextDuration(int i) {
            double dur = duration.getValues()[i];
            dur = scheduler.nextDuration(dur);
            lastDuration = dur;
            return dur;
        }

        private void startGap(int i) {
            state = STATE_GAP;
            double dens = density.getValues()[i];
            gapDensity = dens;
            double gap = scheduler.nextGap(lastDuration, dens) * getFrameRate();
            gap += gapError;
            double wholeFrames = Math.floor(gap);
            countdown = (int) Math.min(wholeFrames, MAX_GAP_FRAMES);
            // Only the fraction of a frame carries over to the next gap. Carrying the part of a
            // huge gap that did not fit in countdown would make the next gap huge as well.
            gapError = gap - wholeFrames;
        }
    }

    /**
     * Use these grains. The farm can be added to a synthesizer before or after this is called.
     * Each grain is set to the current frame rate when it starts.
     */
    public void setGrainArray(Grain[] grains) {
        countScaler = 1.0 / grains.length;
        states = new GrainState[grains.length];
        int frameRate = (getSynthesisEngine() == null) ? DEFAULT_FRAME_RATE : getFrameRate();
        for (int i = 0; i < states.length; i++) {
            states[i] = new GrainState();
            states[i].grain = grains[i];
            grains[i].setFrameRate(frameRate);
        }
    }

    public void setupGrain(Grain grain, int i) {
        double temp = rate.getValues()[i] * calculateOctaveScaler(rateRange.getValues()[i]);
        grain.setRate(temp);

        // Scale the amplitude range so that we never go above
        // original amplitude.
        double base = amplitude.getValues()[i];
        double offset = base * Math.random() * amplitudeRange.getValues()[i];
        grain.setAmplitude(base - offset);
    }

    public void allocate(int numGrains) {
        Grain[] grainArray = new Grain[numGrains];
        for (int i = 0; i < numGrains; i++) {
            Grain grain = new Grain(new GrainSourceSine(), new RaisedCosineEnvelope());
            grainArray[i] = grain;
        }
        setGrainArray(grainArray);
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

    private double calculateOctaveScaler(double rangeValue) {
        double octaveRange = 0.5 * randomizer.nextRandomDouble() * rangeValue;
        return Math.pow(2.0, octaveRange);
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs = output.getValues();
        double[] amplitudes = amplitude.getValues();
        // double frp = getSynthesisEngine().getFramePeriod();
        for (int i = start; i < limit; i++) {
            double result = 0.0;

            // Mix the grains together.
            for (GrainState grainState : states) {
                result += grainState.next(i);
            }

            outputs[i] = result * amplitudes[i] * countScaler;
        }

    }
}
