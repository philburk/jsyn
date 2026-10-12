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

/**
 * Implemented by units that can disable another unit when they finish, for example when an
 * envelope or sample ends or a string becomes quiet. This can be used to reduce CPU load.
 * <p>
 * Test for this interface instead of checking for specific classes:
 *
 * <pre>
 * <code>
 * if (unit instanceof SupportsAutoDisable) {
 *     SupportsAutoDisable supporter = (SupportsAutoDisable) unit;
 *     supporter.setupAutoDisable(voice);
 *     ...
 *     supporter.setAutoDisableEnabled(false); // run continuously
 *     supporter.setAutoDisableEnabled(true);  // disable the voice again when finished
 * }
 * </code>
 * </pre>
 *
 * @author Phil Burk (C) 2026 Mobileer Inc
 */
public interface SupportsAutoDisable {
    /** @return the unit that implements this interface */
    UnitGenerator getUnitGenerator();

    /** @return true if the target unit will be disabled when this unit finishes */
    boolean isAutoDisableEnabled();

    /**
     * Specify a unit to be disabled when this unit finishes, and turn auto disable on. The target
     * unit starts off disabled until this unit is started, for example by a note on.
     *
     * @param unit the UnitGenerator, such as a Circuit or voice, to disable
     */
    void setupAutoDisable(UnitGenerator unit);

    /**
     * Disable this unit when it finishes.
     *
     * @see #setupAutoDisable(UnitGenerator)
     */
    default void setupAutoDisable() {
        setupAutoDisable(getUnitGenerator());
    }

    /**
     * Turn auto disable on or off. The target unit from {@link #setupAutoDisable(UnitGenerator)}
     * is remembered. If it was never set then the target is this unit.
     * <p>
     * Turning it on does not disable the target right away, so a sound in progress is not cut
     * off. The target is disabled the next time this unit finishes.
     * <p>
     * Turning it off enables the target, so that it runs continuously.
     *
     * @param enabled
     */
    void setAutoDisableEnabled(boolean enabled);
}
