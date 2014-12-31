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

public class TestEnable extends TestCase {
    SynthesisEngine synthesisEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        synthesisEngine.stop();
    }

    public void testEnablingGate() throws InterruptedException {
        LinearRamp ramp = new LinearRamp();
        synthesisEngine.add(ramp);
        EnablingGate enabler = new EnablingGate();
        synthesisEngine.add(enabler);
        Add adder = new Add();
        synthesisEngine.add(adder);

        ramp.output.connect(enabler.input);
        enabler.output.connect(adder.inputA);

        // set up so ramp should equal time
        ramp.current.set(0.0);
        ramp.input.set(1.0);
        ramp.time.set(1.0);
        enabler.gate.set(1.0);

        synthesisEngine.start();
        double startTime = synthesisEngine.getCurrentTime();
        // pull from final adder
        adder.start();
        synthesisEngine.sleepUntil(startTime + 0.1);
        double tolerance = 0.002;
        assertEquals("ramp going up", 0.1, ramp.output.getValue(), tolerance);
        assertEquals("enabler going up", 0.1, enabler.output.getValue(), tolerance);
        assertEquals("adder going up", 0.1, adder.output.getValue(), tolerance);
        synthesisEngine.sleepUntil(startTime + 0.2);
        assertEquals("start enabled", 0.2, adder.output.getValue(), tolerance);

        // disable everything upstream
        enabler.gate.set(0.0);

        synthesisEngine.sleepUntil(startTime + 0.3);
        assertEquals("should not be pulled", 0.2, ramp.output.getValue(), tolerance);
        assertEquals("should be disabled", false, enabler.isEnabled());
        assertEquals("should be zero", 0.0, enabler.output.getValue(), tolerance);
        assertEquals("zero", 0.0, adder.output.getValue(), tolerance);

    }
}
