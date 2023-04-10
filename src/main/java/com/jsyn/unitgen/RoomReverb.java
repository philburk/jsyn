/*
 * Copyright 2022 Phil Burk
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
 * Simulate reverberation in a room using a MultiTapDelay to model the early reflections
 * and a PlateReverb to provide diffusion.
 *
 * @author (C) 2022 Phil Burk, Mobileer Inc
 * @see MultiTapDelay
 * @see PlateReverb
 */
public class RoomReverb extends Circuit {
    private static final double SIZE_SCALER_MIN = 0.05;
    private static final double SIZE_SCALER_MAX = 5.0;
    private static final int[] kPositions = {
        10, 197, 401,
        521, 733, 1117,
        1481, 2731, 4177,
        6073, 7927, 9463};
    // Gains based on attenuation in air after a pre-delay.
    // See spreadsheet MiscSynthCalculations
    private static final float[] kGains = {
        0.1840f, -0.1543f, -0.1311f,
        0.1205f, -0.1054f, -0.0859f,
        -0.0731f, -0.0484f, 0.0347f,
        0.0254f, 0.0201f, -0.0171f};

    /**
     * Mono input.
     */
    public UnitInputPort input;

    /** Pre-delay time in milliseconds. */
    public UnitInputPort preDelayMillis;

    /**
     * Approximate time in seconds to decay by -60 dB.
     */
    public UnitInputPort time;

    /**
     * Damping factor for the feedback filters.
     * Must be <= 1.0. Default is 0.5.
     */
    public UnitInputPort damping;

    /**
     * Amount of multi-tap delay in the output mix.
     * Must be between 0.0 and 1.0.
     */
    public UnitInputPort multiTap;

    /**
     * Amount of diffusion in the output mix.
     * Must be between 0.0 and 1.0.
     */
    public UnitInputPort diffusion;

    /**
     * Stereo output.
     */
    public UnitOutputPort output;

    private final PlateReverb mPlateReverb;
    private final MultiTapDelay mMultiTapDelay;
    private final RoomReverbMixer mRoomReverbMixer;

    /**
     * Construct a RoomReverb with a default size of 1.0.
     */
    public RoomReverb() {
        this(1.0);
    }

    /**
     * The size parameter scales the allocated size.
     * A value of 1.0 is the default.
     * At low values the reverb will sound more metallic, like a comb filter.
     * At larger values it will have longer echos.
     *
     * The size value will be clipped between 0.05 and 5.0.
     *
     * @param size adjust internal delay sizes
     */
    public RoomReverb(double size) {
        size = Math.max(SIZE_SCALER_MIN, Math.min(SIZE_SCALER_MAX, size));

        int[] positions = new int[kPositions.length];
        for (int tap = 0; tap < kPositions.length; tap++) {
            positions[tap] = (int) (kPositions[tap] * size);
        }
        add(mMultiTapDelay = new MultiTapDelay(positions, kGains,
            (int)(4000 * size) /* preDelayFrames */)); // roughly 80 msec max
        add(mPlateReverb = new PlateReverb(1.0));
        add(mRoomReverbMixer = new RoomReverbMixer());

        mMultiTapDelay.output.connect(mPlateReverb.input);
        mMultiTapDelay.output.connect(mRoomReverbMixer.multiTapInput);
        mPlateReverb.output.connect(0, mRoomReverbMixer.diffusionInput, 0);
        mPlateReverb.output.connect(1, mRoomReverbMixer.diffusionInput, 1);

        // Assign ports
        input = mMultiTapDelay.input;
        addPort(input);
        preDelayMillis = mMultiTapDelay.preDelayMillis;
        addPort(preDelayMillis);
        time = mPlateReverb.time;
        addPort(time);
        damping = mPlateReverb.damping;
        addPort(damping);
        multiTap = mRoomReverbMixer.multiTapGain;
        addPort(multiTap);
        diffusion = mRoomReverbMixer.diffusionGain;
        addPort(diffusion);
        output = mRoomReverbMixer.output;
        addPort(output);
    }

    // Custom mixer for room reverb.
    // This is faster than multiple small unit generators.
    static class RoomReverbMixer extends UnitGenerator {
        public UnitInputPort multiTapInput;
        public UnitInputPort diffusionInput;

        public UnitInputPort multiTapGain;
        public UnitInputPort diffusionGain;
        public UnitOutputPort output;

        /* Define Unit Ports used by connect() and set(). */
        public RoomReverbMixer() {
            addPort(multiTapInput = new UnitInputPort("MultiTapInput"));
            addPort(diffusionInput = new UnitInputPort(2,"DiffusionInput"));
            addPort(multiTapGain = new UnitInputPort("MultiTap"));
            addPort(diffusionGain = new UnitInputPort(2,"Diffusion"));
            multiTapGain.setup(0.0, 1.0, 1.0);
            diffusionGain.setup(0.0, 1.0, 1.0);
            addPort(output = new UnitOutputPort(2,"Output"));
        }

        @Override
        public void generate(int start, int limit) {
            double[] multiTapInputs = multiTapInput.getValues();
            double[] diffusionInputs0 = diffusionInput.getValues(0);
            double[] diffusionInputs1 = diffusionInput.getValues(1);
            double multiTapGainValue = multiTapGain.getValues()[start];
            double diffusionGainValue = diffusionGain.getValues()[start];
            double[] outputs0 = output.getValues(0);
            double[] outputs1 = output.getValues(1);

            for (int i = start; i < limit; i++) {
                double multiTapScaled = multiTapInputs[i] * multiTapGainValue;
                outputs0[i] = multiTapScaled + (diffusionInputs0[i] * diffusionGainValue);
                outputs1[i] = multiTapScaled + (diffusionInputs1[i] * diffusionGainValue);
            }
        }
    }
}
