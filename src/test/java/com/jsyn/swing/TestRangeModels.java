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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRangeModels {

    public void checkDoubleRange(double dmin, double dmax, double dval) {
        int resolution = 1000;
        DoubleBoundedRangeModel model = new DoubleBoundedRangeModel("test", resolution, dmin, dmax,
                dval);
        assertEquals(dmin, model.getDoubleMinimum(), 0.0001, "setup min");
        assertEquals(dmax, model.getDoubleMaximum(), 0.0001, "setup max");
        assertEquals(dval, model.getDoubleValue(), 0.0001, "setup value");

        model.setDoubleValue(dmin);
        assertEquals(dmin, model.getDoubleValue(), 0.0001, "min double value");
        assertEquals(0, model.getValue(), "min value");

        double dmid = (dmax + dmin) / 2.0;
        model.setDoubleValue(dmid);
        assertEquals(dmid, model.getDoubleValue(), 0.0001, "middle double value");
        assertEquals(resolution / 2, model.getValue(), "middle value");

        model.setDoubleValue(dmax);
        assertEquals(dmax, model.getDoubleValue(), 0.0001, "max double value");
        assertEquals(resolution, model.getValue(), "max value");

    }

    @Test
    public void testDoubleRange() {
        checkDoubleRange(10.0, 20.0, 12.0);
        checkDoubleRange(-1.0, 1.0, 0.5);
    }
}
