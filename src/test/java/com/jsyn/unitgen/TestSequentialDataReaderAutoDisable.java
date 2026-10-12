/*
 * Copyright 2026 Phil Burk, Mobileer Inc
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.jsyn.data.FloatSample;
import com.jsyn.data.SegmentedEnvelope;

public class TestSequentialDataReaderAutoDisable extends NonRealTimeTestCase {

    private void advance(double seconds) throws InterruptedException {
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + seconds);
    }

    @Test
    public void testFixedRateMonoReaderAutoDisable() throws InterruptedException {
        LinearRamp ramp = new LinearRamp();
        FixedRateMonoReader reader = new FixedRateMonoReader();
        Add adder = new Add();

        synthesisEngine.add(ramp);
        synthesisEngine.add(reader);
        synthesisEngine.add(adder);

        reader.output.connect(adder.inputA);
        ramp.output.connect(adder.inputB);

        // 0.1 seconds of data.
        int numFrames = (int) (synthesisEngine.getFrameRate() * 0.1);
        float[] sampleData = new float[numFrames];
        Arrays.fill(sampleData, 0.5f);
        FloatSample sample = new FloatSample(sampleData, 1);

        reader.setupAutoDisable(ramp);
        assertFalse(ramp.isEnabled(), "Ramp should start disabled");

        synthesisEngine.start();
        adder.start();

        // Advance a bit while idle
        advance(0.05);
        assertFalse(ramp.isEnabled(), "Ramp should remain disabled while queue is idle");

        // Queue sample - advance time so scheduled command executes and playback begins
        reader.dataQueue.queue(sample);
        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should be enabled while sample is playing");

        // Advance halfway through sample
        advance(0.05);
        assertTrue(ramp.isEnabled(), "Ramp should remain enabled while sample is playing");

        // Advance past end of sample (0.1s total duration)
        advance(0.1);
        assertFalse(ramp.isEnabled(), "Ramp should be disabled after sample completes");
    }

    @Test
    public void testVariableRateMonoReaderAutoDisableWithEnvelope() throws InterruptedException {
        LinearRamp ramp = new LinearRamp();
        VariableRateMonoReader reader = new VariableRateMonoReader();
        Add adder = new Add();

        synthesisEngine.add(ramp);
        synthesisEngine.add(reader);
        synthesisEngine.add(adder);

        reader.output.connect(adder.inputA);
        ramp.output.connect(adder.inputB);

        // Envelope: attack (0.05s -> 1.0), decay (0.05s -> 0.5), release (0.05s -> 0.0)
        // sustain loop on frame 1 (decay end)
        SegmentedEnvelope envelope = new SegmentedEnvelope(new double[] {
                0.05, 1.0, // frame 0: attack
                0.05, 0.5, // frame 1: decay to sustain
                0.05, 0.0  // frame 2: release
        });
        envelope.setSustainBegin(1);
        envelope.setSustainEnd(2);

        reader.rate.set(1.0);
        reader.setupAutoDisable(ramp);
        assertFalse(ramp.isEnabled(), "Ramp should start disabled");

        synthesisEngine.start();
        adder.start();

        // Advance a bit while idle
        advance(0.05);
        assertFalse(ramp.isEnabled(), "Ramp should remain disabled while queue is idle");

        // Note On: queueOn attack + sustain loop
        reader.dataQueue.queueOn(envelope);
        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should be enabled on queueOn");

        // Advance through attack into sustain loop
        advance(0.15);
        assertTrue(ramp.isEnabled(), "Ramp should remain enabled while sustaining");

        // Note Off: queueOff release
        reader.dataQueue.queueOff(envelope);
        // Advance slightly - release is still playing
        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should remain enabled during release phase");

        // Advance past release completion (0.05s decay remaining + 0.05s release)
        advance(0.15);
        assertFalse(ramp.isEnabled(), "Ramp should be disabled after envelope release completes");

        // Retrigger: Note On again
        reader.dataQueue.queueOn(envelope);
        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should wake up on subsequent queueOn");

        // Note Off and let finish
        reader.dataQueue.queueOff(envelope);
        advance(0.15);
        assertFalse(ramp.isEnabled(), "Ramp should be disabled again after second note finishes");
    }

    @Test
    public void testSelfAutoDisableDefault() throws InterruptedException {
        VariableRateMonoReader reader = new VariableRateMonoReader();
        synthesisEngine.add(reader);

        SegmentedEnvelope envelope = new SegmentedEnvelope(new double[] {
                0.05, 1.0,
                0.05, 0.0
        });

        reader.rate.set(1.0);
        reader.setupAutoDisable(); // defaults to this
        assertFalse(reader.isEnabled(), "Reader should start disabled");

        synthesisEngine.start();
        reader.start();

        reader.dataQueue.queue(envelope);
        advance(0.02);
        assertTrue(reader.isEnabled(), "Reader should become enabled when queued");

        advance(0.15);
        assertFalse(reader.isEnabled(), "Reader should become disabled when envelope completes");
    }

    @Test
    public void testVariableRateMonoReaderAutoDisableWithSustainPoint() throws InterruptedException {
        LinearRamp ramp = new LinearRamp();
        VariableRateMonoReader reader = new VariableRateMonoReader();
        Add adder = new Add();

        synthesisEngine.add(ramp);
        synthesisEngine.add(reader);
        synthesisEngine.add(adder);

        reader.output.connect(adder.inputA);
        ramp.output.connect(adder.inputB);

        // Envelope: attack (0.05s -> 1.0), decay to sustain point (0.05s -> 0.5), release (0.05s -> 0.0)
        // sustain point on frame 1: sustainBegin == sustainEnd
        SegmentedEnvelope envelope = new SegmentedEnvelope(new double[] {
                0.05, 1.0, // frame 0: attack
                0.05, 0.5, // frame 1: decay to sustain point
                0.05, 0.0  // frame 2: release
        });
        envelope.setSustainBegin(1);
        envelope.setSustainEnd(1);

        reader.rate.set(1.0);
        reader.setupAutoDisable(ramp);
        assertFalse(ramp.isEnabled(), "Ramp should start disabled");

        synthesisEngine.start();
        adder.start();

        // Advance a bit while idle
        advance(0.05);
        assertFalse(ramp.isEnabled(), "Ramp should remain disabled while queue is idle");

        // Note On: queueOn attack portion up to sustain point
        reader.dataQueue.queueOn(envelope);
        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should be enabled on queueOn");

        // Advance through attack and decay into the sustain point.
        // The queue runs dry (starves) here, but because it is a sustain point and not an end block,
        // it must remain enabled!
        advance(0.15);
        assertTrue(ramp.isEnabled(), "Ramp should remain enabled while holding at sustain point");

        // Note Off: queueOff release portion
        reader.dataQueue.queueOff(envelope);
        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should remain enabled during release phase");

        // Advance past release completion (0.05s release)
        advance(0.15);
        assertFalse(ramp.isEnabled(), "Ramp should be disabled after envelope release completes");

        // Retrigger: Note On again
        reader.dataQueue.queueOn(envelope);
        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should wake up on subsequent queueOn");

        // Note Off and let finish
        reader.dataQueue.queueOff(envelope);
        advance(0.15);
        assertFalse(ramp.isEnabled(), "Ramp should be disabled again after second note finishes");
    }

    @Test
    public void testAutoDisableWithMultipleQueuedEnvelopes() throws InterruptedException {
        LinearRamp ramp = new LinearRamp();
        VariableRateMonoReader reader = new VariableRateMonoReader();
        Add adder = new Add();

        synthesisEngine.add(ramp);
        synthesisEngine.add(reader);
        synthesisEngine.add(adder);

        reader.output.connect(adder.inputA);
        ramp.output.connect(adder.inputB);

        SegmentedEnvelope env1 = new SegmentedEnvelope(new double[] {
                0.05, 1.0,
                0.05, 0.0
        });
        SegmentedEnvelope env2 = new SegmentedEnvelope(new double[] {
                0.05, 1.0,
                0.05, 0.0
        });

        reader.rate.set(1.0);
        reader.setupAutoDisable(ramp);
        assertFalse(ramp.isEnabled(), "Ramp should start disabled");

        synthesisEngine.start();
        adder.start();

        // Queue both envelopes together
        reader.dataQueue.queue(env1);
        reader.dataQueue.queue(env2);

        advance(0.02);
        assertTrue(ramp.isEnabled(), "Ramp should be enabled while first envelope is playing");

        // Advance to when first envelope finishes and second envelope is playing (0.1s in)
        advance(0.10);
        assertTrue(ramp.isEnabled(), "Ramp should remain enabled while second envelope is playing");

        // Advance past end of second envelope (0.2s total)
        advance(0.15);
        assertFalse(ramp.isEnabled(), "Ramp should be disabled only after all queued envelopes complete");
    }
}
