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

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.UnitGenerator;
import com.softsynth.shared.time.ScheduledCommand;
import com.softsynth.shared.time.TimeStamp;

/**
 * Basic audio port for JSyn unit generators.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class UnitPort {
    private String name;
    private UnitGenerator unit;

    public UnitPort(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUnitGenerator(UnitGenerator unit) {
        // If a port is in a circuit then we want to just use the lower level
        // unit that instantiated the circuit.
        if (this.unit == null) {
            this.unit = unit;
        }
    }

    public UnitGenerator getUnitGenerator() {
        return unit;
    }

    SynthesisEngine getSynthesisEngine() {
        if (unit == null) {
            return null;
        }
        return unit.getSynthesisEngine();
    }

    public int getNumParts() {
        return 1;
    }

    public void scheduleCommand(TimeStamp timeStamp, ScheduledCommand scheduledCommand) {
        if (getSynthesisEngine() == null) {
            scheduledCommand.run();
        } else {
            getSynthesisEngine().scheduleCommand(timeStamp, scheduledCommand);
        }
    }

    public void queueCommand(ScheduledCommand scheduledCommand) {
        if (getSynthesisEngine() == null) {
            scheduledCommand.run();
        } else {
            getSynthesisEngine().scheduleCommand(getSynthesisEngine().createTimeStamp(),
                    scheduledCommand);
        }
    }

}
