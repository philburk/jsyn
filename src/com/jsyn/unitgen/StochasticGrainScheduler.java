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

package com.jsyn.unitgen;

import com.jsyn.util.PseudoRandom;

/**
 * Use a random function to schedule grains.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class StochasticGrainScheduler implements GrainScheduler {
    PseudoRandom pseudoRandom = new PseudoRandom();

    @Override
    public double nextDuration(double duration) {
        return duration;
    }

    @Override
    public double nextGap(double duration, double density) {
        if (density < 0.00000001) {
            density = 0.00000001;
        }
        double gapRange = duration * (1.0 - density) / density;
        return pseudoRandom.random() * gapRange;
    }

}
