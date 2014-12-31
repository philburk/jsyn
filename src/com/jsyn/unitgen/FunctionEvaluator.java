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
/**
 * Aug 26, 2009
 * com.jsyn.engine.units.TunableFilter.java 
 */

package com.jsyn.unitgen;

import com.jsyn.data.Function;
import com.jsyn.ports.UnitFunctionPort;
import com.jsyn.ports.UnitInputPort;

/**
 * Convert an input value to an output value. The Function is typically implemented by looking up a
 * value in a DoubleTable. But other implementations of Function can be used. Input typically ranges
 * from -1.0 to +1.0.
 * 
 * <pre>
 * <code>
 *     // A unit that will lookup the function.
 * 	FunctionEvaluator shaper = new FunctionEvaluator();
 * 	synth.add( shaper );
 * 	shaper.start();
 * 	// Define a custom function.
 * 	Function cuber = new Function()
 * 	{
 * 		public double evaluate( double x )
 * 		{
 * 			return x * x * x;
 * 		}
 * 	};
 * 	shaper.function.set(cuber);
 * 
 * 	shaper.input.set( 0.5 );
 *  </code>
 * </pre>
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @see Function
 */
public class FunctionEvaluator extends UnitFilter {
    public UnitInputPort amplitude;
    public UnitFunctionPort function;

    public FunctionEvaluator() {
        addPort(amplitude = new UnitInputPort("Amplitude", 1.0));
        addPort(function = new UnitFunctionPort("Function"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();
        Function functionObject = function.get();

        for (int i = start; i < limit; i++) {
            outputs[i] = functionObject.evaluate(inputs[i]) * amplitudes[i];
        }

    }
}
