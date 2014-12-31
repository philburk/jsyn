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

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.data.Function;

/**
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TestFunction extends TestCase {
    Synthesizer synth;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        synth = JSyn.createSynthesizer();
        synth.setRealTime(false);
        synth.start();
    }

    @Override
    protected void tearDown() throws Exception {
        synth.stop();
    }

    public void testDoubleTable() {
        double[] data = {
                2.0, 0.0, 3.0
        };
        DoubleTable table = new DoubleTable(data);
        assertEquals("DoubleTable below", 2.0, table.evaluate(-1.4));
        assertEquals("DoubleTable edge", 2.0, table.evaluate(-1.0));
        assertEquals("DoubleTable mid", 1.0, table.evaluate(-0.5));
        assertEquals("DoubleTable zero", 0.0, table.evaluate(0.0));
        assertEquals("DoubleTable mid", 0.75, table.evaluate(0.25));
        assertEquals("DoubleTable above", 3.0, table.evaluate(1.3));

    }

    public void testFunctionEvaluator() throws InterruptedException {
        FunctionEvaluator shaper = new FunctionEvaluator();
        synth.add(shaper);
        shaper.start();

        Function cuber = new Function() {
            @Override
            public double evaluate(double x) {
                return x * x * x;
            }
        };
        shaper.function.set(cuber);

        shaper.input.set(0.5);
        synth.sleepFor(0.001);

        assertEquals("Cuber", (0.5 * 0.5 * 0.5), shaper.output.getValue());
    }

}
