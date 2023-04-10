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

public class AllPassDelay {
    private float[] mBuffer;
    private int mCursor;
    private float mCoefficient = 0.65f;

    public AllPassDelay(int length, float coefficient) {
        mBuffer = new float[length];
        mCoefficient = coefficient;
    }

    public float process(float input) {
        float z = mBuffer[mCursor];
        float x = input - (z * mCoefficient);
        mBuffer[mCursor] = x;
        mCursor++;
        if (mCursor >= mBuffer.length) mCursor = 0;
        return z + (x * mCoefficient);
    }
}
