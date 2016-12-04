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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.UnitPort;

/**
 * Contains a list of units that are executed together.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class Circuit extends UnitGenerator {
    private ArrayList<UnitGenerator> units = new ArrayList<UnitGenerator>();

    private final LinkedHashMap<String, UnitPort> portAliases = new LinkedHashMap<String, UnitPort>();

    @Override
    public void generate(int start, int limit) {
        for (UnitGenerator unit : units) {
            unit.generate(start, limit);
        }
    }

    /**
     * Call flattenOutputs on subunits. Flatten output ports so we don't output a changing signal
     * when stopped.
     */
    @Override
    public void flattenOutputs() {
        for (UnitGenerator unit : units) {
            unit.flattenOutputs();
        }
    }

    /**
     * Call setEnabled on subunits.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (UnitGenerator unit : units) {
            unit.setEnabled(enabled);
        }
    }

    /**
     * @deprecated ignored, frameRate comes from the SynthesisEngine
     * @param frameRate
     */
    @Deprecated
    @Override
    public void setFrameRate(int frameRate) {
        super.setFrameRate(frameRate);
        for (UnitGenerator unit : units) {
            unit.setFrameRate(frameRate);
        }
    }

    @Override
    public void setSynthesisEngine(SynthesisEngine engine) {
        super.setSynthesisEngine(engine);
        for (UnitGenerator unit : units) {
            unit.setSynthesisEngine(engine);
        }
    }

    /** Add a unit to the circuit. */
    public void add(UnitGenerator unit) {
        units.add(unit);
        unit.setCircuit(this);
        // Propagate circuit properties down into subunits.
        unit.setEnabled(isEnabled());
    }

    public void usePreset(int presetIndex) {
    }


    /**
     * Add an alternate name for looking up a port.
     * @param port
     * @param alias
     */
    public void addPortAlias(UnitPort port, String alias) {
        // Store in a hash table by an alternate name.
        portAliases.put(alias.toLowerCase(), port);
    }


    /**
     * Case-insensitive search for a port by its name or alias.
     * @param portName
     * @return matching port or null
     */
    @Override
    public UnitPort getPortByName(String portName) {
        UnitPort port = super.getPortByName(portName);
        if (port == null) {
            port = portAliases.get(portName.toLowerCase());
        }
        return port;
    }

}
