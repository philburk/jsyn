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
 * This unit performs a signed addition on its two inputs. <br>
 * 
 * <pre>
 * output = inputA + inputB
 * </pre>
 * 
 * <br>
 * Note that signals connected to an InputPort are automatically added together so you may not need
 * this unit.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @version 016
 * @see MultiplyAdd
 * @see Subtract
 */
public class Add extends UnitBinaryOperator {

    @Override
    public void generate(int start, int limit) {
        double[] aValues = inputA.getValues();
        double[] bValues = inputB.getValues();
        double[] outputs = output.getValues();

        // System.out.println("adder = " + this);
        // System.out.println("A = " + aValues[0]);
        for (int i = start; i < limit; i++) {
            outputs[i] = aValues[i] + bValues[i];
        }
        // System.out.println("add out = " + outputs[0]);
    }
}
