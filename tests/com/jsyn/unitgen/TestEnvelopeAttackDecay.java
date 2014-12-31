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

import com.jsyn.engine.SynthesisEngine;

public class TestEnvelopeAttackDecay extends TestUnitGate {
    double attackTime;
    double decayTime;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
        attackTime = 0.2;
        decayTime = 0.4;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        synthesisEngine.stop();
    }

    public void testOnOff() throws InterruptedException {
        EnvelopeAttackDecay envelope = new EnvelopeAttackDecay();
        synthesisEngine.add(envelope);

        envelope.attack.set(0.1);
        envelope.decay.set(0.2);

        synthesisEngine.start();
        envelope.start();
        time = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(time + 0.1);
        assertEquals("still idling", 0.0, envelope.output.getValue());

        // Trigger the envelope using on/off
        envelope.input.on();
        time = synthesisEngine.getCurrentTime();
        // Check end of attack cycle.
        synthesisEngine.sleepUntil(time + 0.1);
        assertTrue("at peak", (envelope.output.getValue() > 0.8));
        envelope.input.off();
        // Check end of decay cycle.
        synthesisEngine.sleepUntil(time + 0.3);
        assertTrue("at peak", (envelope.output.getValue() < 0.1));

        synthesisEngine.sleepFor(0.1);

        // Trigger the envelope using trigger()
        envelope.input.trigger();
        time = synthesisEngine.getCurrentTime();
        // Check end of attack cycle.
        synthesisEngine.sleepUntil(time + 0.1);
        assertTrue("at peak", (envelope.output.getValue() > 0.8));
        // Check end of decay cycle.
        synthesisEngine.sleepUntil(time + 0.3);
        assertTrue("at peak", (envelope.output.getValue() < 0.1));

    }

    public void testRetrigger() throws InterruptedException {
        EnvelopeAttackDecay envelope = new EnvelopeAttackDecay();
        synthesisEngine.add(envelope);

        envelope.attack.set(0.1);
        envelope.decay.set(0.2);

        synthesisEngine.start();
        envelope.start();
        time = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(time + 0.1);
        assertEquals("still idling", 0.0, envelope.output.getValue());

        // Trigger the envelope using trigger()
        envelope.input.trigger();
        // Check end of attack cycle.
        synthesisEngine.sleepFor(0.1);
        assertEquals("at peak", 1.0, envelope.output.getValue(), 0.1);

        // Decay half way.
        synthesisEngine.sleepFor(0.1);
        assertTrue("at peak", (envelope.output.getValue() < 0.7));

        // Retrigger while decaying
        envelope.input.trigger();
        // Will get to top faster.
        synthesisEngine.sleepFor(0.1);
        assertEquals("at peak", 1.0, envelope.output.getValue(), 0.1);

        // Check end of decay cycle.
        synthesisEngine.sleepFor(0.2);
        assertTrue("at peak", (envelope.output.getValue() < 0.1));

    }

    public void testAutoDisable() throws InterruptedException {

        LinearRamp ramp = new LinearRamp();
        synthesisEngine.add(ramp);
        EnvelopeAttackDecay envelope = new EnvelopeAttackDecay();
        envelope.attack.set(0.1);
        envelope.decay.set(0.1);
        synthesisEngine.add(envelope);
        ramp.output.connect(envelope.amplitude);

        checkAutoDisable(ramp, envelope);

    }
}
