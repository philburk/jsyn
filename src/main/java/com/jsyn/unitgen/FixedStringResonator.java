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
 * String resonator tuned with a first-order allpass filter.
 * <p>
 * The allpass has a flat gain so the decay and brightness are the same for every note. Use it for
 * strings whose pitch is fixed during a note, like a piano or harp. The frequency is read once per
 * block. Changing it while the string rings can cause a small transient. For pitch bends use
 * {@link VariableStringResonator}.
 *
 * @author Phil Burk (C) 2026 Mobileer Inc
 */
public class FixedStringResonator extends StringResonator {

    public FixedStringResonator() {
        this(DEFAULT_MIN_FREQUENCY);
    }

    /**
     * @param minFrequency lowest frequency supported, determines the size of the delay line
     */
    public FixedStringResonator(double minFrequency) {
        super(minFrequency, Interpolation.ALL_PASS);
    }
}
