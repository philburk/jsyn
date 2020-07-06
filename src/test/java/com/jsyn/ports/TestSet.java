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

package com.jsyn.ports;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.Minimum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestSet {

    /** Internal value setting. */
    @Test
    public void testSetValue() {
        int numParts = 4;
        UnitInputPort port = new UnitInputPort(numParts, "Tester");
        port.setValueInternal(0, 100.0);
        port.setValueInternal(2, 120.0);
        port.setValueInternal(1, 110.0);
        port.setValueInternal(3, 130.0);
        assertEquals(100.0, port.getValue(0), "check port value");
        assertEquals(120.0, port.getValue(2), "check port value");
        assertEquals(110.0, port.getValue(1), "check port value");
        assertEquals(130.0, port.getValue(3), "check port value");
    }

    @Test
    public void testSet() throws InterruptedException {
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
        synthesisEngine.start();
        synthesisEngine.sleepUntil(0.01);
        Minimum min;
        synthesisEngine.add(min = new Minimum());

        double x = 33.99;
        double y = 8.31;
        min.inputA.set(x);
        min.inputB.set(y);
        synthesisEngine.sleepFor(0.01);
        assertEquals(x, min.inputA.getValue(), "min set A");
        assertEquals(y, min.inputB.getValue(), "min set B");
        min.start();
        synthesisEngine.sleepFor(0.01);

        assertEquals(y, min.output.getValue(), "min output");
        synthesisEngine.stop();
    }

    /** if we use a port index out of range we want to know now and not blow up the engine. */
    @Test
    public void testSetBadPort() throws InterruptedException {
        SynthesisEngine synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
        synthesisEngine.start();
        Minimum min;
        synthesisEngine.add(min = new Minimum());

        min.start();
        try {
            min.inputA.set(1, 23.45);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        } catch (Exception e) {
            fail("Catch port out of range, caught " + e);
        }

        // Don't blow up here.
        synthesisEngine.sleepUntil(0.01);

        synthesisEngine.stop();
    }

}
