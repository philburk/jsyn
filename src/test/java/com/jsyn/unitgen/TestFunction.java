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

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.data.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TestFunction {
    Synthesizer synth;

    @BeforeEach
    protected void beforeEach() {
        synth = JSyn.createSynthesizer();
        synth.setRealTime(false);
        synth.start();
    }

    @AfterEach
    protected void afterEach() {
        synth.stop();
    }

    @Test
    public void testDoubleTable() {
        double[] data = {
                2.0, 0.0, 3.0
        };
        DoubleTable table = new DoubleTable(data);
        assertEquals(2.0, table.evaluate(-1.4), "DoubleTable below");
        assertEquals(2.0, table.evaluate(-1.0), "DoubleTable edge");
        assertEquals(1.0, table.evaluate(-0.5), "DoubleTable mid");
        assertEquals(0.0, table.evaluate(0.0), "DoubleTable zero");
        assertEquals(0.75, table.evaluate(0.25), "DoubleTable mid");
        assertEquals(3.0, table.evaluate(1.3), "DoubleTable above");

    }

    @Test
    public void testFunctionEvaluator() throws InterruptedException {
        FunctionEvaluator shaper = new FunctionEvaluator();
        synth.add(shaper);
        shaper.start();

        Function cuber = x -> x * x * x;
        shaper.function.set(cuber);

        shaper.input.set(0.5);
        synth.sleepFor(0.001);

        assertEquals((0.5 * 0.5 * 0.5), shaper.output.getValue(), "Cuber");
    }

}
