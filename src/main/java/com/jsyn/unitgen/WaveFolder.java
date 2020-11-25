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

import com.jsyn.ports.UnitInputPort;

/**
 * Wave Folder
 *
 * Fold the input waveform by passing it through a sine function
 * and output the results in the range of -1.0 to 1.0.
 *
 * This works best if the amplitude of the input waveform is close to 1.0.
 *
 * @author Phil Burk (C) 2020 Mobileer Inc
 */
public class WaveFolder extends UnitFilter
{
	/**
	 * The depth of the wave-folding effect.
	 * At zero there should be no audible effect.
	 */
    public UnitInputPort amount;
    private static final double MAX_SCALE = 8.0;

    /* Define Unit Ports used by connect() and set(). */
    public WaveFolder() {
        addPort(amount = new UnitInputPort("Amount"));
        amount.setup(0.0, 0.0, MAX_SCALE);
    }

	@Override
	public void generate( int start, int limit )
	{
        double[] inputs = input.getValues();
        double[] scales = amount.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
        	double inputValue = inputs[i];
        	double scaleValue = scales[i];

        	// Prevent blowup near zero.
        	scaleValue = Math.max(scaleValue, 0.00001);
        	double phase = inputValue * scaleValue;

        	double folded;
//        	if (true) {
	        	// Clip to -1/+1 range for fastSin even for extreme ranges.
	    		phase = (phase + 256.0 + 1.0) * 0.5;
	    		phase -= Math.floor(phase);
	    		phase = (phase * 2.0) - 1.0;

	        	// Fold using a sine function that ranges -1 to +1.
	        	folded = SineOscillator.fastSin(phase);
//        	} else {
//        		folded = Math.sin(phase * Math.PI); // slow
//        	}

        	// Try to maintain constant amplitude at small scale value.
        	// Based on sin(x) ~= x for low values of x.
        	if (scaleValue < 1.0) {
	        	folded /= scaleValue * (((1 - Math.PI) * scaleValue) + Math.PI);
        	}
            outputs[i] = folded;
        }
	}

}
