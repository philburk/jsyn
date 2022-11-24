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

/**
 * Delay line based on a circular buffer.
 */
public class SimpleDelay {
    private float[] mBuffer;
    private int mCursor;

    public SimpleDelay(int length) {
        mBuffer = new float[length];
    }

    /**
     * Read a value from the delay line.
     * @param position positive delay in frames
     * @return delayed value
     */
    public float read(int position) {
        int index = mCursor - position;
        if (index < 0) {
            index += mBuffer.length;
        }
        return mBuffer[index];
    }

    /**
     * Add a new value to the head of the delay line.
     * @param input sample value
     */
    public void add(float input) {
        mBuffer[mCursor] = input;
        mCursor++;
        if (mCursor >= mBuffer.length) mCursor = 0;
    }

    /**
     * Add a new value and return the oldest value in the delay line.
     * @param input sample value
     * @return oldest value
     */
    public float process(float input) {
        float output = mBuffer[mCursor];
        add(input);
        return output;
    }
}
