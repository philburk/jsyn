/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

package com.jsyn.swing;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jsyn.ports.UnitInputPort;

/**
 * A bounded range model that drives a UnitInputPort. The range of the model is set based on the min
 * and max of the port.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class PortBoundedRangeModel extends DoubleBoundedRangeModel {
    private static final long serialVersionUID = -8011867146560305808L;
    private UnitInputPort port;

    public PortBoundedRangeModel(UnitInputPort pPort) {
        super(pPort.getName(), 10000, pPort.getMinimum(), pPort.getMaximum(), pPort.getValue());
        this.port = pPort;
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                port.set(getDoubleValue());
            }
        });
    }

}
