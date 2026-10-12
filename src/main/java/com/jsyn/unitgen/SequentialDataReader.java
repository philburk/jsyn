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

import com.jsyn.ports.UnitDataQueuePort;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;

/**
 * Base class for reading a sample or envelope.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public abstract class SequentialDataReader extends UnitGenerator implements SupportsAutoDisable {
    public UnitDataQueuePort dataQueue;
    public UnitInputPort amplitude;
    public UnitOutputPort output;

    /* Define Unit Ports used by connect() and set(). */
    public SequentialDataReader() {
        addPort(dataQueue = new UnitDataQueuePort("Data"));
        addPort(amplitude = new UnitInputPort("Amplitude", UnitOscillator.DEFAULT_AMPLITUDE));
    }

    /**
     * Specify a unit to be disabled when the envelope or sample finishes.
     *
     * @param unit the UnitGenerator, such as a Circuit or voice, to disable
     */
    @Override
    public void setupAutoDisable(UnitGenerator unit) {
        dataQueue.setupAutoDisable(unit);
    }

    @Override
    public boolean isAutoDisableEnabled() {
        return dataQueue.isAutoDisableEnabled();
    }

    @Override
    public void setAutoDisableEnabled(boolean enabled) {
        dataQueue.setAutoDisableEnabled(enabled);
    }
}
