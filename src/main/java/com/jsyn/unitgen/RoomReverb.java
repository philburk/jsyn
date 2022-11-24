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
    // TODO better set of positions and gains, maybe 12
    // Add Predelay as a single Delay Line
    private static final int[] kPositions = {400, 737, 1329, 5921, 9823};
    private static final float[] kGains = {0.4f, -0.3f, 0.2f, -0.1f, 0.06f};
    private final PlateReverb mPlateReverb;
    private final MultiTapDelay mMultiTapDelay;
    private final MultiplyAdd mMAC;

    public UnitInputPort input;
    public UnitOutputPort output;

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
     * At larger values it will sound more echoey.
     *
     * The size value will be clipped between 0.05 and 5.0.
     *
     * @param size adjust internal delay sizes
     */
    public RoomReverb(double size) {
        size = Math.max(0.05, Math.min(5.0, size));

        int[] positions = new int[kPositions.length];
        for (int tap = 0; tap < kPositions.length; tap++) {
            positions[tap] = (int) (kPositions[tap] * size);
        }
        add(mMultiTapDelay = new MultiTapDelay(positions, kGains));
        add(mPlateReverb = new PlateReverb(1.0));
        add(mMAC = new MultiplyAdd());

        mMultiTapDelay.output.connect(mPlateReverb.input);
        mMultiTapDelay.output.connect(mMAC.inputA);
        mMAC.inputB.set(1.0);
        mPlateReverb.output.connect(mMAC.inputC);

        addPort(input = mMultiTapDelay.input);
        addPort(output = mMAC.output);
    }

}
