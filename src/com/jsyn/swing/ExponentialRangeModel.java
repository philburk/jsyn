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

/**
 * Maps integer range info to a double value along an exponential scale.
 * 
 * <pre>
 * 
 *   x = ival / resolution
 *   f(x) = a*(root&circ;cx) + b
 *   f(0.0) = dmin
 *   f(1.0) = dmax
 *   b = dmin - a
 *   a = (dmax - dmin) / (root&circ;c - 1)
 *  
 *   Inverse function:
 *   x = log( (y-b)/a ) / log(root)
 * 
 * </pre>
 * 
 * @author Phil Burk, (C) 2011 Mobileer Inc
 */
public class ExponentialRangeModel extends DoubleBoundedRangeModel {
    private static final long serialVersionUID = -142785624892302160L;
    double a = 1.0;
    double b = -1.0;
    double span = 1.0;
    double root = 10.0;

    /** Use default root of 10.0 and span of 1.0. */
    public ExponentialRangeModel(String name, int resolution, double dmin, double dmax, double dval) {
        this(name, resolution, dmin, dmax, dval, 1.0);
    }

    /** Set span before setting double value so it is translated correctly. */
    ExponentialRangeModel(String name, int resolution, double dmin, double dmax, double dval,
            double span) {
        super(name, resolution, dmin, dmax, dval);
        setRoot(10.0);
        setSpan(span);
        /* Set again after coefficients setup. */
        setDoubleValue(dval);
    }

    private void updateCoefficients() {
        a = (getDoubleMaximum() - getDoubleMinimum()) / (Math.pow(root, span) - 1.0);
        b = getDoubleMinimum() - a;
    }

    private void setRoot(double w) {
        root = w;
        updateCoefficients();
    }

    public double getRoot() {
        return root;
    }

    public void setSpan(double c) {
        this.span = c;
        updateCoefficients();
    }

    public double getSpan() {
        return span;
    }

    @Override
    public double sliderToDouble(int sliderValue) {
        updateCoefficients(); // TODO optimize when we call this
        double x = (double) sliderValue / getMaximum();
        double y = (a * Math.pow(root, span * x)) + b;
        return y;
    }

    @Override
    public int doubleToSlider(double dval) {
        updateCoefficients(); // TODO optimize when we call this
        double z = (dval - b) / a;
        double x = Math.log(z) / (span * Math.log(root));
        return (int) Math.round(x * getMaximum());
    }

    public void test(int sliderValue) {
        double dval = sliderToDouble(sliderValue);
        int ival = doubleToSlider(dval);
        System.out.println(sliderValue + " => " + dval + " => " + ival);
    }

}
