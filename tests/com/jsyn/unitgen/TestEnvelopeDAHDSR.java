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

public class TestEnvelopeDAHDSR extends TestUnitGate {
    double delayTime;
    double attackTime;
    double holdTime;
    double decayTime;
    double sustainLevel;
    double releaseTime;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
        delayTime = 0.1;
        attackTime = 0.2;
        holdTime = 0.3;
        decayTime = 0.4;
        sustainLevel = 0.5;
        releaseTime = 0.6;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        synthesisEngine.stop();
    }

    public void testStages() throws InterruptedException {
        EnvelopeDAHDSR ramp = checkToSustain();

        // Change sustain level to simulate tremolo sustain.
        sustainLevel = 0.7;
        ramp.sustain.set(sustainLevel);
        time += 0.01;
        synthesisEngine.sleepUntil(time);
        assertEquals("sustain moving delaying", sustainLevel, ramp.output.getValue(), 0.01);

        // Gate off to let envelope release.
        ramp.input.set(0.0);
        synthesisEngine.sleepUntil(time + (releaseTime * 0.1));
        double releaseValue = ramp.output.getValue();
        assertEquals("partway down release", sustainLevel * 0.36, releaseValue, 0.01);
    }

    private EnvelopeDAHDSR checkToSustain() throws InterruptedException {
        EnvelopeDAHDSR ramp = new EnvelopeDAHDSR();
        synthesisEngine.add(ramp);

        ramp.delay.set(delayTime);
        ramp.attack.set(attackTime);
        ramp.hold.set(holdTime);
        ramp.decay.set(decayTime);
        ramp.sustain.set(sustainLevel);
        ramp.release.set(releaseTime);

        synthesisEngine.start();
        ramp.start();
        time = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(time + (2.0 * delayTime));
        assertEquals("still idling", 0.0, ramp.output.getValue());

        // Trigger the envelope.
        ramp.input.set(1.0);
        time = synthesisEngine.getCurrentTime();
        // Check end of delay cycle.
        synthesisEngine.sleepUntil(time + (delayTime * 0.9));
        assertEquals("still delaying", 0.0, ramp.output.getValue(), 0.01);
        // Half way up attack ramp.
        synthesisEngine.sleepUntil(time + delayTime + (attackTime * 0.5));
        assertEquals("half attack", 0.5, ramp.output.getValue(), 0.01);
        // Holding after attack.
        synthesisEngine.sleepUntil(time + delayTime + attackTime + (holdTime * 0.1));
        assertEquals("holding", 1.0, ramp.output.getValue(), 0.01);
        synthesisEngine.sleepUntil(time + delayTime + attackTime + (holdTime * 0.9));
        assertEquals("still holding", 1.0, ramp.output.getValue(), 0.01);
        synthesisEngine.sleepUntil(time + delayTime + attackTime + holdTime + decayTime);
        time = synthesisEngine.getCurrentTime();
        assertEquals("at sustain", sustainLevel, ramp.output.getValue(), 0.01);
        return ramp;
    }

    public void testRetrigger() throws InterruptedException {
        EnvelopeDAHDSR ramp = checkToSustain();

        // Gate off to let envelope release.
        ramp.input.set(0.0);
        synthesisEngine.sleepUntil(time + (releaseTime * 0.1));
        double releaseValue = ramp.output.getValue();
        assertEquals("partway down release", sustainLevel * 0.36, releaseValue, 0.01);

        // Retrigger during release phase.
        time = synthesisEngine.getCurrentTime();
        ramp.input.set(1.0);
        // Check end of delay cycle.
        synthesisEngine.sleepUntil(time + (delayTime * 0.9));
        assertEquals("still delaying", releaseValue, ramp.output.getValue(), 0.01);
        // Half way up attack ramp from where it started.
        synthesisEngine.sleepUntil(time + delayTime + (attackTime * 0.5));
        assertEquals("half attack", releaseValue + 0.5, ramp.output.getValue(), 0.01);

    }

    // I noticed a hang while playing with knobs.
    public void testHang() throws InterruptedException {

        delayTime = 0.0;
        attackTime = 0.0;
        holdTime = 0.0;
        decayTime = 0.0;
        sustainLevel = 0.3;
        releaseTime = 3.0;

        EnvelopeDAHDSR ramp = new EnvelopeDAHDSR();
        synthesisEngine.add(ramp);

        ramp.delay.set(delayTime);
        ramp.attack.set(attackTime);
        ramp.hold.set(holdTime);
        ramp.decay.set(decayTime);
        ramp.sustain.set(sustainLevel);
        ramp.release.set(releaseTime);

        synthesisEngine.start();
        ramp.start();
        // Trigger the envelope.
        ramp.input.set(1.0);
        time = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(time + 0.01);
        assertEquals("should jump to sustain level", sustainLevel, ramp.output.getValue());

        // Gate off to let envelope release.
        ramp.input.set(0.0);
        synthesisEngine.sleepUntil(time + 1.0);
        double releaseValue = ramp.output.getValue();
        assertTrue("partway down release", sustainLevel > releaseValue);

        holdTime = 0.5;
        ramp.hold.set(holdTime);
        decayTime = 0.5;
        ramp.decay.set(decayTime);

        // Retrigger during release phase and try to catch it at top of hold
        time = synthesisEngine.getCurrentTime();
        ramp.input.set(1.0);
        // Check end of delay cycle.
        synthesisEngine.sleepUntil(time + (holdTime * 0.1));
        assertEquals("should jump to hold", 1.0, ramp.output.getValue(), 0.01);
    }

    public void testNegative() throws InterruptedException {
        delayTime = -0.1;
        attackTime = -0.2;
        holdTime = -0.3;
        decayTime = -0.4;
        sustainLevel = 0.3;
        releaseTime = -0.5;

        EnvelopeDAHDSR ramp = new EnvelopeDAHDSR();
        synthesisEngine.add(ramp);

        ramp.delay.set(delayTime);
        ramp.attack.set(attackTime);
        ramp.hold.set(holdTime);
        ramp.decay.set(decayTime);
        ramp.sustain.set(sustainLevel);
        ramp.release.set(releaseTime);

        synthesisEngine.start();
        ramp.start();
        // Trigger the envelope.
        ramp.input.set(1.0);
        time = synthesisEngine.getCurrentTime();
        time += 0.1;
        synthesisEngine.sleepUntil(time + 0.01);
        assertEquals("should jump to sustain level", sustainLevel, ramp.output.getValue());

        ramp.sustain.set(sustainLevel = -0.4);
        time += 0.1;
        synthesisEngine.sleepUntil(time);
        assertEquals("sustain should clip at zero", sustainLevel, ramp.output.getValue());

        ramp.sustain.set(sustainLevel = 0.4);
        time += 0.1;
        synthesisEngine.sleepUntil(time);
        assertEquals("sustain should come back", sustainLevel, ramp.output.getValue());

        // Gate off to let envelope release.
        ramp.input.set(0.0);
        time += 0.1;
        synthesisEngine.sleepUntil(time);
        double releaseValue = ramp.output.getValue();
        assertEquals("release quickly", 0.0, releaseValue);
    }

    public void testOnOff() throws InterruptedException {
        EnvelopeDAHDSR ramp = new EnvelopeDAHDSR();
        synthesisEngine.add(ramp);

        ramp.delay.set(0.0);
        ramp.attack.set(0.1);
        ramp.hold.set(0.0);
        ramp.decay.set(0.0);
        ramp.sustain.set(0.9);
        ramp.release.set(0.1);

        synthesisEngine.start();
        ramp.start();
        time = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(time + 0.2);
        assertEquals("still idling", 0.0, ramp.output.getValue());

        // Trigger the envelope.
        ramp.input.on();
        time = synthesisEngine.getCurrentTime();
        // Check end of delay cycle.
        synthesisEngine.sleepUntil(time + 0.2);
        assertEquals("at sustain", 0.9, ramp.output.getValue(), 0.01);

        // Release the envelope.
        ramp.input.off();
        time = synthesisEngine.getCurrentTime();
        // Check end of delay cycle.
        synthesisEngine.sleepUntil(time + 0.2);
        assertEquals("after release", 0.0, ramp.output.getValue(), 0.01);
    }

    public void testAutoDisable() throws InterruptedException {

        LinearRamp ramp = new LinearRamp();
        synthesisEngine.add(ramp);
        EnvelopeDAHDSR envelope = new EnvelopeDAHDSR();
        synthesisEngine.add(envelope);
        envelope.attack.set(0.1);
        envelope.decay.set(0.1);
        envelope.release.set(0.1);
        envelope.sustain.set(0.1);
        ramp.output.connect(envelope.amplitude);

        checkAutoDisable(ramp, envelope);

    }

    class GatedRampCircuit extends Circuit {
        LinearRamp ramp;
        EnvelopeDAHDSR envelope;

        GatedRampCircuit() {
            add(ramp = new LinearRamp());
            add(envelope = new EnvelopeDAHDSR());
            envelope.attack.set(0.1);
            envelope.decay.set(0.1);
            envelope.release.set(0.1);
            envelope.sustain.set(0.1);

            envelope.setupAutoDisable(this);
            ramp.output.connect(envelope.amplitude);
        }
    }

    public void testAutoDisableCircuit() throws InterruptedException {
        GatedRampCircuit circuit = new GatedRampCircuit();
        synthesisEngine.add(circuit);
        checkAutoDisable(circuit.ramp, circuit.envelope);
    }

    public void checkReleaseTiming(double releaseTime, double tolerance)
            throws InterruptedException {
        delayTime = 0.0;
        attackTime = 0.2;
        holdTime = 0.0;
        decayTime = 10.0;
        sustainLevel = 1.0;

        EnvelopeDAHDSR ramp = new EnvelopeDAHDSR();
        synthesisEngine.add(ramp);

        ramp.delay.set(delayTime);
        ramp.attack.set(attackTime);
        ramp.hold.set(holdTime);
        ramp.decay.set(decayTime);
        ramp.sustain.set(sustainLevel);
        ramp.release.set(releaseTime);

        synthesisEngine.start();
        ramp.start();
        // Trigger the envelope.
        ramp.input.set(1.0);
        time = synthesisEngine.getCurrentTime();
        time += attackTime * 2;
        synthesisEngine.sleepUntil(time);
        assertEquals("should be at to sustain level", sustainLevel, ramp.output.getValue());

        // Start envelope release.
        ramp.input.set(0.0);
        final double db90 = 20.0 * Math.log(1.0 / 32768.0) / Math.log(10.0);
        System.out.println("JSyns DB90 is actually " + db90);
        int numSteps = 10;
        for (int i = 0; i < 10; i++) {
            time += releaseTime / numSteps;
            synthesisEngine.sleepUntil(time);
            double expectedDB = db90 * (i + 1) / numSteps;
            double expectedAmplitude = sustainLevel * Math.pow(10.0, expectedDB / 20.0);
            double releaseValue = ramp.output.getValue();
            assertEquals("release " + i + " at", expectedAmplitude, releaseValue, tolerance);
        }
        time += releaseTime / numSteps;
        synthesisEngine.sleepUntil(time);
        double releaseValue = ramp.output.getValue();
        assertEquals("env after release time should go to zero", 0.0, releaseValue, 0.0001);
    }

    public void testReleaseTiming() throws InterruptedException {
        checkReleaseTiming(0.1, 0.004);
        checkReleaseTiming(1.0, 0.002);
        checkReleaseTiming(2.5, 0.001);
        checkReleaseTiming(10.0, 0.001);
    }

}
