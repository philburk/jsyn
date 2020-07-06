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

import com.jsyn.unitgen.UnitGenerator;
import com.softsynth.shared.time.ScheduledCommand;
import com.softsynth.shared.time.TimeStamp;

public class UnitGatePort extends UnitInputPort {
    private boolean autoDisableEnabled = false;
    private boolean triggered = false;
    private boolean off = true;
    private UnitGenerator gatedUnit;
    public static final double THRESHOLD = 0.01;

    public UnitGatePort(String name) {
        super(name);
    }

    public void on() {
        setOn(true);
    }

    public void off() {
        setOn(false);
    }

    public void off(TimeStamp timeStamp) {
        setOn(false, timeStamp);
    }

    public void on(TimeStamp timeStamp) {
        setOn(true, timeStamp);
    }

    private void setOn(final boolean on) {
        queueCommand(new ScheduledCommand() {
            @Override
            public void run() {
                setOnInternal(on);
            }
        });
    }

    private void setOn(final boolean on, TimeStamp timeStamp) {
        scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                setOnInternal(on);
            }
        });
    }

    private void setOnInternal(boolean on) {
        if (on) {
            triggerInternal();
        }
        setValueInternal(on ? 1.0 : 0.0);
    }

    private void triggerInternal() {
        getGatedUnit().setEnabled(true);
        triggered = true;
    }

    public void trigger() {
        queueCommand(new ScheduledCommand() {
            @Override
            public void run() {
                triggerInternal();
            }
        });
    }

    public void trigger(TimeStamp timeStamp) {
        scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                triggerInternal();
            }
        });
    }

    /**
     * This is called by UnitGenerators. It sets the off value that can be tested using isOff().
     *
     * @param i
     * @return true if triggered by a positive edge.
     */
    public boolean checkGate(int i) {
        double[] inputs = getValues();
        boolean result = triggered;
        triggered = false;
        if (off) {
            if (inputs[i] >= THRESHOLD) {
                result = true;
                off = false;
            }
        } else {
            if (inputs[i] < THRESHOLD) {
                off = true;
            }
        }
        return result;
    }

    public boolean isOff() {
        return off;
    }

    public boolean isAutoDisableEnabled() {
        return autoDisableEnabled;
    }

    /**
     * Request the containing UnitGenerator be disabled when checkAutoDisabled() is called. This can
     * be used to reduce CPU load.
     *
     * @param autoDisableEnabled
     */
    public void setAutoDisableEnabled(boolean autoDisableEnabled) {
        this.autoDisableEnabled = autoDisableEnabled;
    }

    /**
     * Called by UnitGenerator when an envelope reaches the end of its contour.
     */
    public void checkAutoDisable() {
        if (autoDisableEnabled) {
            getGatedUnit().setEnabled(false);
        }
    }

    private UnitGenerator getGatedUnit() {
        return (gatedUnit == null) ? getUnitGenerator() : gatedUnit;
    }

    public void setupAutoDisable(UnitGenerator unit) {
        gatedUnit = unit;
        setAutoDisableEnabled(true);
        // Start off disabled so we don't immediately swamp the CPU.
        gatedUnit.setEnabled(false);
    }
}
