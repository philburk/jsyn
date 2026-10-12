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

package com.jsyn.dsp;

import java.util.Arrays;

/**
 * Allpass filter with a delay of length samples:
 *
 * <pre>
 * H(z) = (coefficient + z^-length) / (1 + coefficient * z^-length)
 * </pre>
 *
 * With a long length this is a Schroeder allpass used for reverb diffusion. With length = 1 it is
 * a first-order allpass that can be used for fractional delay tuning. Its phase delay at DC is
 * (1 - coefficient) / (1 + coefficient) samples.
 */
public class AllPassDelay {
    private float[] mBuffer;
    private int mCursor;
    private float mCoefficient = 0.65f;

    /**
     * @param length delay in samples, at least 1
     * @param coefficient should be between -1.0 and 1.0 for stability
     */
    public AllPassDelay(int length, float coefficient) {
        mBuffer = new float[length];
        mCoefficient = coefficient;
    }

    /**
     * Create an allpass with a coefficient of zero, which is a pure delay.
     *
     * @param length delay in samples, at least 1
     */
    public AllPassDelay(int length) {
        this(length, 0.0f);
    }

    public float process(float input) {
        float z = mBuffer[mCursor];
        float x = input - (z * mCoefficient);
        mBuffer[mCursor] = x;
        mCursor++;
        if (mCursor >= mBuffer.length) mCursor = 0;
        return z + (x * mCoefficient);
    }

    public float getCoefficient() {
        return mCoefficient;
    }

    /**
     * This can be changed at any time, for example to retune a fractional delay.
     *
     * @param coefficient should be between -1.0 and 1.0 for stability
     */
    public void setCoefficient(float coefficient) {
        mCoefficient = coefficient;
    }

    public int getLength() {
        return mBuffer.length;
    }

    /** Set the internal state to zero. */
    public void clear() {
        Arrays.fill(mBuffer, 0.0f);
    }
}
