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
/**
 * Aug 26, 2009
 * com.jsyn.engine.units.TunableFilter.java
 */

package com.jsyn.unitgen;

import com.jsyn.ports.UnitInputPort;

/**
 * A UnitFilter with a frequency port.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc Translated from 'C' to Java by Lisa
 *         Tolenti.
 */
public abstract class TunableFilter extends UnitFilter {

    static final double DEFAULT_FREQUENCY = 400;
    public UnitInputPort frequency;

    public TunableFilter() {
        addPort(frequency = new UnitInputPort("Frequency"));
        frequency.setup(40.0, DEFAULT_FREQUENCY, 6000.0);
    }

}
