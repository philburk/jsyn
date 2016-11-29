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

import junit.framework.TestCase;

import com.jsyn.engine.SynthesisEngine;
import com.softsynth.math.AudioMath;

/**
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TestMath extends TestCase {
    SynthesisEngine synthesisEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        synthesisEngine = new SynthesisEngine();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAdd() {
        Add add = new Add();
        add.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        add.inputA.setValueInternal(x);
        add.inputB.setValueInternal(y);

        add.generate();

        assertEquals("Add", x + y, add.output.getValue(), 0.001);
    }

    public void testPartialAdd() {
        Add add = new Add();
        add.setSynthesisEngine(synthesisEngine);

        double x = 2.5;
        double y = 9.7;
        add.inputA.setValueInternal(x);
        add.inputB.setValueInternal(y);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        add.generate(2, 5);

        assertEquals("Add partial", 0.0, add.output.getValues()[0], 0.001);
        assertEquals("Add partial", 0.0, add.output.getValues()[1], 0.001);
        assertEquals("Add partial", x + y, add.output.getValues()[2], 0.001);
        assertEquals("Add partial", x + y, add.output.getValues()[3], 0.001);
        assertEquals("Add partial", x + y, add.output.getValues()[4], 0.001);
        assertEquals("Add partial", 0.0, add.output.getValues()[5], 0.001);
        assertEquals("Add partial", 0.0, add.output.getValues()[6], 0.001);
        assertEquals("Add partial", 0.0, add.output.getValues()[7], 0.001);

    }

    /**
     * Unit test for Subtract.java - added by Lisa Tolentino 06/17/2009
     */
    public void testSubtract() {
        Subtract sub = new Subtract();
        sub.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        sub.inputA.setValueInternal(x);
        sub.inputB.setValueInternal(y);

        sub.generate();

        assertEquals("Subtract", x - y, sub.output.getValue(), 0.001);
    }

    public void testPartialSubtract() {
        Subtract sub = new Subtract();
        sub.setSynthesisEngine(synthesisEngine);

        double x = 2.5;
        double y = 9.7;
        sub.inputA.setValueInternal(x);
        sub.inputB.setValueInternal(y);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        sub.generate(2, 5);

        assertEquals("Subtract partial", 0.0, sub.output.getValues()[0], 0.001);
        assertEquals("Subtract partial", 0.0, sub.output.getValues()[1], 0.001);
        assertEquals("Subtract partial", x - y, sub.output.getValues()[2], 0.001);
        assertEquals("Subtract partial", x - y, sub.output.getValues()[3], 0.001);
        assertEquals("Subtract partial", x - y, sub.output.getValues()[4], 0.001);
        assertEquals("Subtract partial", 0.0, sub.output.getValues()[5], 0.001);
        assertEquals("Subtract partial", 0.0, sub.output.getValues()[6], 0.001);
        assertEquals("Subtract partial", 0.0, sub.output.getValues()[7], 0.001);

    }

    /**
     * Unit test for Multiply.java - added by Lisa Tolentino 06/19/2009
     */
    public void testMultiply() {
        Multiply mult = new Multiply();
        mult.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        mult.inputA.setValueInternal(x);
        mult.inputB.setValueInternal(y);

        mult.generate();

        assertEquals("Multiply", x * y, mult.output.getValue(), 0.001);
    }

    public void testPartialMultiply() {
        Multiply mult = new Multiply();
        mult.setSynthesisEngine(synthesisEngine);

        double x = 2.5;
        double y = 9.7;
        mult.inputA.setValueInternal(x);
        mult.inputB.setValueInternal(y);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        mult.generate(2, 5);

        assertEquals("Multiply partial", 0.0, mult.output.getValues()[0], 0.001);
        assertEquals("Multiply partial", 0.0, mult.output.getValues()[1], 0.001);
        assertEquals("Multiply partial", x * y, mult.output.getValues()[2], 0.001);
        assertEquals("Multiply partial", x * y, mult.output.getValues()[3], 0.001);
        assertEquals("Multiply partial", x * y, mult.output.getValues()[4], 0.001);
        assertEquals("Multiply partial", 0.0, mult.output.getValues()[5], 0.001);
        assertEquals("Multiply partial", 0.0, mult.output.getValues()[6], 0.001);
        assertEquals("Multiply partial", 0.0, mult.output.getValues()[7], 0.001);

    }

    /**
     * Unit test for Divide.java - added by Lisa Tolentino 06/19/2009
     */
    public void testDivide() {
        Divide divide = new Divide();
        divide.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        divide.inputA.setValueInternal(x);
        divide.inputB.setValueInternal(y);

        divide.generate();

        assertEquals("Divide", x / y, divide.output.getValue(), 0.001);
    }

    public void testPartialDivide() {
        Divide divide = new Divide();
        divide.setSynthesisEngine(synthesisEngine);

        double x = 2.5;
        double y = 9.7;
        divide.inputA.setValueInternal(x);
        divide.inputB.setValueInternal(y);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        divide.generate(2, 5);

        assertEquals("Divide partial", 0.0, divide.output.getValues()[0], 0.001);
        assertEquals("Divide partial", 0.0, divide.output.getValues()[1], 0.001);
        assertEquals("Divide partial", x / y, divide.output.getValues()[2], 0.001);
        assertEquals("Divide partial", x / y, divide.output.getValues()[3], 0.001);
        assertEquals("Divide partial", x / y, divide.output.getValues()[4], 0.001);
        assertEquals("Divide partial", 0.0, divide.output.getValues()[5], 0.001);
        assertEquals("Divide partial", 0.0, divide.output.getValues()[6], 0.001);
        assertEquals("Divide partial", 0.0, divide.output.getValues()[7], 0.001);

    }

    /**
     * Unit test for MultiplyAdd.java - added by Lisa Tolentino 06/19/2009
     */
    public void testMultiplyAdd() {
        MultiplyAdd multAdd = new MultiplyAdd();
        multAdd.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        double z = 2.28;
        multAdd.inputA.setValueInternal(x);
        multAdd.inputB.setValueInternal(y);
        multAdd.inputC.setValueInternal(z);

        multAdd.generate();

        assertEquals("MultiplyAdd", (x * y) + z, multAdd.output.getValue(), 0.001);
    }

    public void testPartialMultiplyAdd() {
        MultiplyAdd multAdd = new MultiplyAdd();
        multAdd.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        double z = 2.28;
        multAdd.inputA.setValueInternal(x);
        multAdd.inputB.setValueInternal(y);
        multAdd.inputC.setValueInternal(z);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        multAdd.generate(2, 5);

        assertEquals("MultiplyAdd partial", 0.0, multAdd.output.getValues()[0], 0.001);
        assertEquals("MultiplyAdd partial", 0.0, multAdd.output.getValues()[1], 0.001);
        assertEquals("MultiplyAdd partial", (x * y) + z, multAdd.output.getValues()[2], 0.001);
        assertEquals("MultiplyAdd partial", (x * y) + z, multAdd.output.getValues()[3], 0.001);
        assertEquals("MultiplyAdd partial", (x * y) + z, multAdd.output.getValues()[4], 0.001);
        assertEquals("MultiplyAdd partial", 0.0, multAdd.output.getValues()[5], 0.001);
        assertEquals("MultiplyAdd partial", 0.0, multAdd.output.getValues()[6], 0.001);
        assertEquals("MultiplyAdd partial", 0.0, multAdd.output.getValues()[7], 0.001);

    }

    /**
     * Unit test for Compare.java - added by Lisa Tolentino 06/19/2009
     */
    public void testCompare() {
        UnitBinaryOperator compare = new Compare();
        compare.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        compare.inputA.setValueInternal(x);
        compare.inputB.setValueInternal(y);

        compare.generate();

        assertEquals("Compare", (x > y ? 1 : 0), compare.output.getValue(), 0.001);
    }

    public void testPartialCompare() {
        UnitBinaryOperator compare = new Compare();
        compare.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        compare.inputA.setValueInternal(x);
        compare.inputB.setValueInternal(y);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        compare.generate(2, 5);

        assertEquals("Compare partial", 0.0, compare.output.getValues()[0], 0.001);
        assertEquals("Compare partial", 0.0, compare.output.getValues()[1], 0.001);
        assertEquals("Compare partial", (x > y ? 1 : 0), compare.output.getValues()[2], 0.001);
        assertEquals("Compare partial", (x > y ? 1 : 0), compare.output.getValues()[3], 0.001);
        assertEquals("Compare partial", (x > y ? 1 : 0), compare.output.getValues()[4], 0.001);
        assertEquals("Compare partial", 0.0, compare.output.getValues()[5], 0.001);
        assertEquals("Compare partial", 0.0, compare.output.getValues()[6], 0.001);
        assertEquals("Compare partial", 0.0, compare.output.getValues()[7], 0.001);

    }

    /**
     * Unit test for Maximum.java - added by Lisa Tolentino 06/20/2009
     */
    public void testMaximum() {
        Maximum max = new Maximum();
        max.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        max.inputA.setValueInternal(x);
        max.inputB.setValueInternal(y);

        max.generate();

        assertEquals("Maximum", (x > y ? x : y), max.output.getValue(), 0.001);
    }

    public void testPartialMaximum() {
        Maximum max = new Maximum();
        max.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        max.inputA.setValueInternal(x);
        max.inputB.setValueInternal(y);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        max.generate(2, 5);

        assertEquals("Maximum partial", 0.0, max.output.getValues()[0], 0.001);
        assertEquals("Maximum partial", 0.0, max.output.getValues()[1], 0.001);
        assertEquals("Maximum partial", (x > y ? x : y), max.output.getValues()[2], 0.001);
        assertEquals("Maximum partial", (x > y ? x : y), max.output.getValues()[3], 0.001);
        assertEquals("Maximum partial", (x > y ? x : y), max.output.getValues()[4], 0.001);
        assertEquals("Maximum partial", 0.0, max.output.getValues()[5], 0.001);
        assertEquals("Maximum partial", 0.0, max.output.getValues()[6], 0.001);
        assertEquals("Maximum partial", 0.0, max.output.getValues()[7], 0.001);

    }

    /**
     * Unit test for Minimum.java - added by Lisa Tolentino 06/20/2009
     */
    public void testMinimum() {
        Minimum min = new Minimum();
        min.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        min.inputA.setValueInternal(x);
        min.inputB.setValueInternal(y);

        min.generate();

        assertEquals("Minimum", (x < y ? x : y), min.output.getValue(), 0.001);
    }

    public void testPartialMinimum() {
        Minimum min = new Minimum();
        min.setSynthesisEngine(synthesisEngine);

        double x = 33.99;
        double y = 8.31;
        min.inputA.setValueInternal(x);
        min.inputB.setValueInternal(y);

        // Only generate a few values in the middle.
        // This is to test low latency feedback loops.
        // Only generate values for 2,3,4
        min.generate(2, 5);

        assertEquals("Maximum partial", 0.0, min.output.getValues()[0], 0.001);
        assertEquals("Maximum partial", 0.0, min.output.getValues()[1], 0.001);
        assertEquals("Maximum partial", (x < y ? x : y), min.output.getValues()[2], 0.001);
        assertEquals("Maximum partial", (x < y ? x : y), min.output.getValues()[3], 0.001);
        assertEquals("Maximum partial", (x < y ? x : y), min.output.getValues()[4], 0.001);
        assertEquals("Maximum partial", 0.0, min.output.getValues()[5], 0.001);
        assertEquals("Maximum partial", 0.0, min.output.getValues()[6], 0.001);
        assertEquals("Maximum partial", 0.0, min.output.getValues()[7], 0.001);

    }

    public void testPowerOfTwo() {
        PowerOfTwo powerOfTwo = new PowerOfTwo();
        powerOfTwo.setSynthesisEngine(synthesisEngine);
        final double smallValue = -1.5308084989341915E-17;
        double values[] = {
                0.0, 1.3, 4.5, -0.5, -1.0, -2.8, smallValue, -smallValue, 1.0 - smallValue,
                1.0 + smallValue
        };
        for (double in : values) {
            powerOfTwo.input.setValueInternal(in);
            powerOfTwo.generate();
            assertEquals("PowerOfTwo", Math.pow(2.0, in), powerOfTwo.output.getValue(), 0.001);
        }
    }
    public void testPitchToFrequency() {
        PitchToFrequency ugen = new PitchToFrequency();
        ugen.setSynthesisEngine(synthesisEngine);
        final double smallValue = -1.5308084989341915E-17;
        double values[] = {
                49.0, 49.5, 50.0 + smallValue,
                60.0 -smallValue,
                79.2, 12.9, 118.973
        };
        // Sanity check AudioMath
        assertEquals("PitchToFrequency", 440.0,  AudioMath.pitchToFrequency(69), 0.001);
        assertEquals("PitchToFrequency", 660.0,  AudioMath.pitchToFrequency(69+7.02), 0.1);

        for (double pitch : values) {
            ugen.input.setValueInternal(pitch);
            ugen.generate();
            assertEquals("PitchToFrequency", AudioMath.pitchToFrequency(pitch),
                    ugen.output.getValue(), 0.001);
        }
    }

}
