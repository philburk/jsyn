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
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestSupportsAutoDisable {

    private List<UnitGenerator> createUnits() {
        return Arrays.asList(
                new EnvelopeAttackDecay(),
                new EnvelopeDAHDSR(),
                new FixedRateMonoReader(),
                new FixedRateStereoReader(),
                new VariableRateMonoReader(),
                new VariableRateStereoReader());
    }

    @Test
    public void testDefaultTargetIsSelf() {
        for (UnitGenerator unit : createUnits()) {
            String name = unit.getClass().getSimpleName();
            assertTrue(unit instanceof SupportsAutoDisable, name + " should support auto disable");
            assertTrue(unit.isEnabled(), name + " should start enabled");
            ((SupportsAutoDisable) unit).setupAutoDisable();
            assertFalse(unit.isEnabled(), name + " should disable itself until it is started");
        }
    }

    @Test
    public void testOtherTarget() {
        for (UnitGenerator unit : createUnits()) {
            String name = unit.getClass().getSimpleName();
            PassThrough target = new PassThrough();
            ((SupportsAutoDisable) unit).setupAutoDisable(target);
            assertFalse(target.isEnabled(), name + " should disable the target until it is started");
            assertTrue(unit.isEnabled(), name + " should not disable itself when given a target");
        }
    }

    @Test
    public void testToggle() {
        for (UnitGenerator unit : createUnits()) {
            String name = unit.getClass().getSimpleName();
            SupportsAutoDisable supporter = (SupportsAutoDisable) unit;
            PassThrough target = new PassThrough();
            assertFalse(supporter.isAutoDisableEnabled(), name + " should start with auto disable off");

            supporter.setupAutoDisable(target);
            assertTrue(supporter.isAutoDisableEnabled(), name + " setup should turn auto disable on");
            assertFalse(target.isEnabled(), name + " setup should disable the target");

            supporter.setAutoDisableEnabled(false);
            assertFalse(supporter.isAutoDisableEnabled(), name + " should turn auto disable off");
            assertTrue(target.isEnabled(), name + " should enable the target when turned off");

            supporter.setAutoDisableEnabled(true);
            assertTrue(supporter.isAutoDisableEnabled(), name + " should turn auto disable on again");
            assertTrue(target.isEnabled(), name + " should not cut off the target when turned on");
        }
    }

    @Test
    public void testTurningOffWhenNeverEnabledLeavesUnitAlone() {
        for (UnitGenerator unit : createUnits()) {
            String name = unit.getClass().getSimpleName();
            unit.setEnabled(false);
            ((SupportsAutoDisable) unit).setAutoDisableEnabled(false);
            assertFalse(unit.isEnabled(),
                    name + " should not be enabled by turning off auto disable that was never on");
        }
    }

    @Test
    public void testUnsupportedUnit() {
        UnitGenerator unit = new SineOscillator();
        assertFalse(unit instanceof SupportsAutoDisable, "SineOscillator does not support auto disable");
    }
}
