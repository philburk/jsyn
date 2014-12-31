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

import com.jsyn.engine.MultiTable;

/**
 * Impulse oscillator created by differentiating a sawtoothBL. A band limited impulse is very narrow
 * but is slightly wider than one sample.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class ImpulseOscillatorBL extends SawtoothOscillatorBL {
    private double previous = 0.0;

    @Override
    protected double generateBL(MultiTable multiTable, double currentPhase,
            double positivePhaseIncrement, double flevel, int i) {
        double saw = multiTable.calculateSawtooth(currentPhase, positivePhaseIncrement, flevel);
        double result = previous - saw;
        previous = saw;
        return result;
    }

}
