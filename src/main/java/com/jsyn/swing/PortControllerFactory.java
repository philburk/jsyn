/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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
 * Factory class for making various controllers for JSyn ports.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PortControllerFactory {
    private static final int RESOLUTION = 100000;

    public static DoubleBoundedRangeSlider createPortSlider(final UnitInputPort port) {
        DoubleBoundedRangeModel rangeModel = new DoubleBoundedRangeModel(port.getName(),
                RESOLUTION, port.getMinimum(), port.getMaximum(), port.get());
        rangeModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                DoubleBoundedRangeModel model = (DoubleBoundedRangeModel) e.getSource();
                double value = model.getDoubleValue();
                port.set(value);
            }
        });
        return new DoubleBoundedRangeSlider(rangeModel, 4);
    }

    public static DoubleBoundedRangeSlider createExponentialPortSlider(final UnitInputPort port) {
        ExponentialRangeModel rangeModel = new ExponentialRangeModel(port.getName(), RESOLUTION,
                port.getMinimum(), port.getMaximum(), port.get());
        rangeModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ExponentialRangeModel model = (ExponentialRangeModel) e.getSource();
                double value = model.getDoubleValue();
                port.set(value);
            }
        });
        return new DoubleBoundedRangeSlider(rangeModel, 4);
    }

}
