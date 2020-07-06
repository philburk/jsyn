/*
 * Copyright 2014 Phil Burk, Mobileer Inc
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

/**
 * Used inside UnitGenerators for fast smoothing of inputs.
 * 
 * @author Phil Burk (C) 2014 Mobileer Inc
 */
public class Unzipper {
    private double target;
    private double delta;
    private double current;
    private int counter;
    // About 30 msec. Power of 2 so divide should be faster.
    private static final int NUM_STEPS = 1024;

    public double smooth(double input) {
        if (input != target) {
            target = input;
            delta = (target - current) / NUM_STEPS;
            counter = NUM_STEPS;
        }
        if (counter > 0) {
            if (--counter == 0) {
                current = target;
            } else {
                current += delta;
            }
        }
        return current;
    }
}
