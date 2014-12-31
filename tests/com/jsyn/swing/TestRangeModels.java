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

import junit.framework.TestCase;

public class TestRangeModels extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void checkDoubleRange(double dmin, double dmax, double dval) {
        int resolution = 1000;
        DoubleBoundedRangeModel model = new DoubleBoundedRangeModel("test", resolution, dmin, dmax,
                dval);
        assertEquals("setup min", dmin, model.getDoubleMinimum(), 0.0001);
        assertEquals("setup max", dmax, model.getDoubleMaximum(), 0.0001);
        assertEquals("setup value", dval, model.getDoubleValue(), 0.0001);

        model.setDoubleValue(dmin);
        assertEquals("min double value", dmin, model.getDoubleValue(), 0.0001);
        assertEquals("min value", 0, model.getValue());

        double dmid = (dmax + dmin) / 2.0;
        model.setDoubleValue(dmid);
        assertEquals("middle double value", dmid, model.getDoubleValue(), 0.0001);
        assertEquals("middle value", resolution / 2, model.getValue());

        model.setDoubleValue(dmax);
        assertEquals("max double value", dmax, model.getDoubleValue(), 0.0001);
        assertEquals("max value", resolution, model.getValue());

    }

    public void testDoubleRange() {
        checkDoubleRange(10.0, 20.0, 12.0);
        checkDoubleRange(-1.0, 1.0, 0.5);
    }
}
