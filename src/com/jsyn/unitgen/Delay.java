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

/**
 * Simple non-interpolating delay. The delay line must be allocated by calling allocate(n).
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @version 016
 * @see InterpolatingDelay
 */

public class Delay extends UnitFilter {
    private float[] buffer;
    private int cursor;
    private int numSamples;

    /**
     * Allocate an internal array for the delay line.
     * 
     * @param numSamples
     */
    public void allocate(int numSamples) {
        this.numSamples = numSamples;
        buffer = new float[numSamples];
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            outputs[i] = buffer[cursor];
            buffer[cursor] = (float) inputs[i];
            cursor += 1;
            if (cursor >= numSamples) {
                cursor = 0;
            }
        }
    }

}
