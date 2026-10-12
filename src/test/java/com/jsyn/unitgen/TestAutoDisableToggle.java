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

/**
 * After turning auto disable off and on again, the original target should be disabled when the
 * unit finishes, not the unit itself.
 */
public class TestAutoDisableToggle extends NonRealTimeTestCase {

    /**
     * Set up the target, toggle off and on, let the unit finish, then check which unit got
     * disabled.
     */
    protected void checkTargetRemembered(UnitGenerator unit, Runnable finish)
            throws InterruptedException {
        PassThrough target = new PassThrough();
        synthesisEngine.add(unit);
        synthesisEngine.add(target);
        String name = unit.getClass().getSimpleName();
        SupportsAutoDisable supporter = (SupportsAutoDisable) unit;

        supporter.setupAutoDisable(target);
        supporter.setAutoDisableEnabled(false);
        supporter.setAutoDisableEnabled(true);

        synthesisEngine.start();
        unit.start();
        finish.run();
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + 0.2);

        assertFalse(target.isEnabled(), name + " should disable the original target when it finishes");
        assertTrue(unit.isEnabled(), name + " should not disable itself");
    }

    @Test
    public void testEnvelope() throws InterruptedException {
        EnvelopeAttackDecay envelope = new EnvelopeAttackDecay();
        envelope.attack.set(0.01);
        envelope.decay.set(0.01);
        checkTargetRemembered(envelope, () -> envelope.input.trigger());
    }

    @Test
    public void testReader() throws InterruptedException {
        FixedRateMonoReader reader = new FixedRateMonoReader();
        float[] data = new float[100];
        Arrays.fill(data, 0.5f);
        checkTargetRemembered(reader, () -> reader.dataQueue.queue(new FloatSample(data)));
    }

    @Test
    public void testStringResonator() throws InterruptedException {
        // With no excitation the string is quiet, so it should disable the target soon.
        checkTargetRemembered(new FixedStringResonator(), () -> {
        });
    }
}
