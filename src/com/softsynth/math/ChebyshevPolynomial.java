/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

package com.softsynth.math;

/**
 * ChebyshevPolynomial<br>
 * Used to generate data for waveshaping table oscillators.
 * 
 * @author Nick Didkovsky (C) 1997 Phil Burk and Nick Didkovsky
 */

public class ChebyshevPolynomial {
    static final Polynomial twoX = new Polynomial(2, 0);
    static final Polynomial one = new Polynomial(1);
    static final Polynomial oneX = new Polynomial(1, 0);

    /**
     * Calculates Chebyshev polynomial of specified integer order. Recursively generated using
     * relation Tk+1(x) = 2xTk(x) - Tk-1(x)
     * 
     * @return Chebyshev polynomial of specified order
     */
    public static Polynomial T(int order) {
        if (order == 0)
            return one;
        else if (order == 1)
            return oneX;
        else
            return Polynomial.minus(Polynomial.mult(T(order - 1), (twoX)), T(order - 2));
    }
}
