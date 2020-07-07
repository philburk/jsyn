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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEnvelopeAttackDecay extends TestUnitGate {
    double attackTime;
    double decayTime;

    @BeforeEach
    protected void beforeEach() {
        synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
        attackTime = 0.2;
        decayTime = 0.4;
    }

    @AfterEach
    protected void afterEach() {
        synthesisEngine.stop();
    }

    @Test
    public void testOnOff() throws InterruptedException {
        EnvelopeAttackDecay envelope = new EnvelopeAttackDecay();
        synthesisEngine.add(envelope);

        envelope.attack.set(0.1);
        envelope.decay.set(0.2);

        synthesisEngine.start();
        envelope.start();
        time = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(time + 0.1);
        assertEquals(0.0, envelope.output.getValue(), "still idling");

        // Trigger the envelope using on/off
        envelope.input.on();
        time = synthesisEngine.getCurrentTime();
        // Check end of attack cycle.
        synthesisEngine.sleepUntil(time + 0.1);
        assertTrue(envelope.output.getValue() > 0.8, "at peak");
        envelope.input.off();
        // Check end of decay cycle.
        synthesisEngine.sleepUntil(time + 0.3);
        assertTrue(envelope.output.getValue() < 0.1, "at peak");

        synthesisEngine.sleepFor(0.1);

        // Trigger the envelope using trigger()
        envelope.input.trigger();
        time = synthesisEngine.getCurrentTime();
        // Check end of attack cycle.
        synthesisEngine.sleepUntil(time + 0.1);
        assertTrue(envelope.output.getValue() > 0.8, "at peak");
        // Check end of decay cycle.
        synthesisEngine.sleepUntil(time + 0.3);
        assertTrue(envelope.output.getValue() < 0.1, "at peak");
    }

    @Test
    public void testRetrigger() throws InterruptedException {
        EnvelopeAttackDecay envelope = new EnvelopeAttackDecay();
        synthesisEngine.add(envelope);

        envelope.attack.set(0.1);
        envelope.decay.set(0.2);

        synthesisEngine.start();
        envelope.start();
        time = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(time + 0.1);
        assertEquals(0.0, envelope.output.getValue(), "still idling");

        // Trigger the envelope using trigger()
        envelope.input.trigger();
        // Check end of attack cycle.
        synthesisEngine.sleepFor(0.1);
        assertEquals(1.0, envelope.output.getValue(), 0.1, "at peak");

        // Decay half way.
        synthesisEngine.sleepFor(0.1);
        assertTrue(envelope.output.getValue() < 0.7, "at peak");

        // Retrigger while decaying
        envelope.input.trigger();
        // Will get to top faster.
        synthesisEngine.sleepFor(0.1);
        assertEquals(1.0, envelope.output.getValue(), 0.1, "at peak");

        // Check end of decay cycle.
        synthesisEngine.sleepFor(0.2);
        assertTrue(envelope.output.getValue() < 0.1, "at peak");

    }

    @Test
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
