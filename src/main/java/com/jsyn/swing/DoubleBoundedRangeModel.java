/*
 * Copyright 2002 Phil Burk, Mobileer Inc
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

import javax.swing.DefaultBoundedRangeModel;

/**
 * Double precision data model for sliders and knobs. Maps integer range info to a double value.
 * 
 * @author Phil Burk, (C) 2002 SoftSynth.com, PROPRIETARY and CONFIDENTIAL
 */
public class DoubleBoundedRangeModel extends DefaultBoundedRangeModel {
    private static final long serialVersionUID = 284361767102120148L;
    protected String name;
    private double dmin;
    private double dmax;

    public DoubleBoundedRangeModel(String name, int resolution, double dmin, double dmax,
            double dval) {
        this.name = name;
        this.dmin = dmin;
        this.dmax = dmax;
        setMinimum(0);
        setMaximum(resolution);
        setDoubleValue(dval);
    }

    public boolean equivalentTo(Object other) {
        if (!(other instanceof DoubleBoundedRangeModel))
            return false;
        DoubleBoundedRangeModel otherModel = (DoubleBoundedRangeModel) other;
        return (getValue() == otherModel.getValue());
    }

    /** Set name of value. This may be used in labels or when saving the value. */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getDoubleMinimum() {
        return dmin;
    }

    public double getDoubleMaximum() {
        return dmax;
    }

    public double sliderToDouble(int sliderValue) {
        double doubleMin = getDoubleMinimum();
        return doubleMin + ((getDoubleMaximum() - doubleMin) * sliderValue / getMaximum());
    }

    public int doubleToSlider(double dval) {
        double doubleMin = getDoubleMinimum();
        // TODO consider using Math.floor() instead of (int) if not too slow.
        return (int) Math.round(getMaximum() * (dval - doubleMin)
                / (getDoubleMaximum() - doubleMin));
    }

    public double getDoubleValue() {
        return sliderToDouble(getValue());
    }

    public void setDoubleValue(double dval) {
        setValue(doubleToSlider(dval));
    }

}
