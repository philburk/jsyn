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

public class TestUnitGate extends TestCase {

    protected SynthesisEngine synthesisEngine;
    protected double time;

    public void checkAutoDisable(LinearRamp ramp, UnitGate envelope) throws InterruptedException {
        double tolerance = 0.01;
        Add adder = new Add();
        synthesisEngine.add(adder);

        envelope.output.connect(adder.inputA);
        if (ramp.getCircuit() != null) {
            ramp.output.connect(adder.inputB);
        }

        envelope.input.setAutoDisableEnabled(true);
        envelope.setEnabled(false);

        // set up so ramp value should equal time
        ramp.current.set(0.0);
        ramp.input.set(1.0);
        ramp.time.set(1.0);

        synthesisEngine.start();
        // pull from final adder
        adder.start();

        time = synthesisEngine.getCurrentTime();
        time += 0.1;
        synthesisEngine.sleepUntil(time);
        assertEquals("still idling", 0.0, envelope.output.getValue());
        assertEquals("ramp frozen at beginning", 0.0, ramp.output.getValue(), tolerance);

        // run multiple times to make sure we can retrigger the envelope.
        for (int i = 0; i < 3; i++) {
            double level = ramp.output.getValue();
            // Trigger the envelope using trigger()
            envelope.input.on();
            time += 0.1;
            level += 0.1;
            synthesisEngine.sleepUntil(time);
            assertEquals("ramp going up " + i, level, ramp.output.getValue(), tolerance);
            assertTrue("enabled at peak", envelope.isEnabled());

            envelope.input.off();
            time += 0.1;
            level += 0.1;
            synthesisEngine.sleepUntil(time);
            assertEquals("ramp going up more " + i, level, ramp.output.getValue(), tolerance);
            assertEquals("at bottom", 0.0, envelope.output.getValue(), 0.1);

            time += 0.2;
            synthesisEngine.sleepUntil(time);
            assertEquals("ramp frozen " + i, level, ramp.output.getValue(), tolerance);
        }
    }

}
