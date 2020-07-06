/*
 * Copyright 2012 Phil Burk, Mobileer Inc
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

import com.jsyn.ports.UnitGatePort;
import com.jsyn.ports.UnitOutputPort;

/**
 * Base class for other envelopes.
 * 
 * @author Phil Burk (C) 2012 Mobileer Inc
 */
public abstract class UnitGate extends UnitGenerator implements UnitSource {
    /**
     * Input that triggers the envelope. Use amplitude port if you want to connect a signal to be
     * modulated by the envelope.
     */
    public UnitGatePort input;
    public UnitOutputPort output;

    public UnitGate() {
        addPort(input = new UnitGatePort("Input"));
        addPort(output = new UnitOutputPort("Output"));
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

    /**
     * Specify a unit to be disabled when the envelope finishes.
     * 
     * @param unit
     */
    public void setupAutoDisable(UnitGenerator unit) {
        input.setupAutoDisable(unit);
    }

}
