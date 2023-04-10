/*
 * Copyright 2023 Phil Burk
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

import com.jsyn.dsp.SimpleDelay;
import com.jsyn.ports.UnitInputPort;

/**
 * Delay with multiple read positions and associated gains.
 */
public class MultiTapDelay extends UnitFilter {

    /** Pre-delay time in milliseconds. */
    public UnitInputPort preDelayMillis;
    private final int mMaxPreDelayFrames;
    private SimpleDelay mPreDelay;
    private SimpleDelay mDelay;
    private final int[] mPositions;
    private final float[] mGains;

    private int mPreDelayFrames = 0;

    /**
     * Construct a delay line with specified taps.
     * The allocated size of the delay line will be the maximum position plus the maxPreDelayFrames.
     * @param positions delay index, eg. 172 for Z(n-172)
     * @param gains multiplier for the corresponding position
     * @param maxPreDelayFrames extra allocated frames for pre-delay before the taps
     */
    public MultiTapDelay(final int[] positions,
                final float[] gains,
                final int maxPreDelayFrames) {
        mPositions = positions;
        mGains = gains;

        preDelayMillis = new UnitInputPort("PreDelayMillis");
        double maxMillis = maxPreDelayFrames * 1000.0 / 44100; // TODO handle unknown frame rate better
        preDelayMillis.setup(0.0, Math.min(10.0, maxMillis), maxMillis);
        addPort(preDelayMillis);
        mMaxPreDelayFrames = Math.max(1, maxPreDelayFrames);
        mPreDelay = new SimpleDelay(maxPreDelayFrames);

        int maxPosition = 0;
        for (int position : positions) {
            maxPosition = Math.max(maxPosition, position);
        }
        mDelay = new SimpleDelay(maxPosition);
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        double preDelayMS = preDelayMillis.getValues()[0];
        int preDelayFrames = (int)(preDelayMS * 0.001 * getFrameRate());
        preDelayFrames = Math.max(1, Math.min(mMaxPreDelayFrames, preDelayFrames));

        for (int i = start; i < limit; i++) {
            mPreDelay.write((float) inputs[i]);
            mDelay.write(mPreDelay.read(preDelayFrames));
            mPreDelay.advance();
            double sum = 0.0;
            for (int tap = 0; tap < mPositions.length; tap++) {
                sum += mDelay.read(mPositions[tap]) * mGains[tap];
            }
            mDelay.advance();
            outputs[i] = sum; // mix taps
        }
    }
}
