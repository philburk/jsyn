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

package com.jsyn.swing;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jsyn.ports.UnitInputPort;

public class PortModelFactory {
    private static final int RESOLUTION = 1000000;

    public static DoubleBoundedRangeModel createLinearModel(final UnitInputPort pPort) {
        final DoubleBoundedRangeModel model = new DoubleBoundedRangeModel(pPort.getName(),
                RESOLUTION, pPort.getMinimum(), pPort.getMaximum(), pPort.get());
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                pPort.set(model.getDoubleValue());
            }
        });
        return model;
    }

    public static ExponentialRangeModel createExponentialModel(final UnitInputPort pPort) {
        final ExponentialRangeModel model = new ExponentialRangeModel(pPort.getName(), RESOLUTION,
                pPort.getMinimum(), pPort.getMaximum(), pPort.get());
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                pPort.set(model.getDoubleValue());
            }
        });
        return model;
    }

    public static ExponentialRangeModel createExponentialModel(final int partNum,
            final UnitInputPort pPort) {
        final ExponentialRangeModel model = new ExponentialRangeModel(pPort.getName(), RESOLUTION,
                pPort.getMinimum(), pPort.getMaximum(), pPort.get());
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                pPort.set(partNum, model.getDoubleValue());
            }
        });
        return model;
    }

}
