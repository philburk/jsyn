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

/**
 * String resonator tuned with 3rd order Lagrange interpolation.
 * <p>
 * The frequency is read on every frame and can change smoothly while the string rings. Use it for
 * pitch bends and vibrato, like on a guitar. The interpolation loses a little high frequency
 * energy, which varies slightly with the frequency. For fixed pitches
 * {@link FixedStringResonator} has a perfectly flat response.
 *
 * @author Phil Burk (C) 2026 Mobileer Inc
 */
public class VariableStringResonator extends StringResonator {

    public VariableStringResonator() {
        this(DEFAULT_MIN_FREQUENCY);
    }

    /**
     * @param minFrequency lowest frequency supported, determines the size of the delay line
     */
    public VariableStringResonator(double minFrequency) {
        super(minFrequency, Interpolation.LAGRANGE);
    }
}
