/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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

package com.jsyn.examples;

import com.jsyn.unitgen.UnitFilter;

/**
 * Custom unit generator that can be used with other JSyn units. Cube the input value and write it
 * to output port.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class CustomCubeUnit extends UnitFilter {

    @Override
    /** This is where the synthesis occurs.
     * It is called in a high priority background thread so do not do
     * anything crazy here like reading a file or doing network I/O.
     * Just do fast arithmetic.
     * <br>
     * The start and limit allow us to do either block or single sample processing.
     */
    public void generate(int start, int limit) {
        // Get signal arrays from ports.
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            double x = inputs[i];
            // Do the math.
            outputs[i] = x * x * x;
        }
    }
}
