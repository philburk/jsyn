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
 * This unit divides its two inputs. <br>
 * 
 * <pre>
 * output = inputA / inputB
 * </pre>
 * 
 * <br>
 * Note that this unit is protected from dividing by zero. But you can still get some very big
 * outputs.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @version 016
 * @see Multiply
 * @see Subtract
 */
public class Divide extends UnitBinaryOperator {

    @Override
    public void generate(int start, int limit) {
        double[] aValues = inputA.getValues();
        double[] bValues = inputB.getValues();
        double[] outputs = output.getValues();
        for (int i = start; i < limit; i++) {
            /* Prevent divide by zero crash. */
            double b = bValues[i];
            if (b == 0.0) {
                b = 0.0000001;
            }

            outputs[i] = aValues[i] / b;
        }
    }

}
