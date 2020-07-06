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
/**
 * Sep 9, 2009
 * com.jsyn.engine.units.SynthRandom.java 
 */

package com.jsyn.util;

import java.util.Random;

/**
 * Pseudo-random numbers using predictable and fast linear-congruential method.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc Translated from 'C' to Java by Lisa
 *         Tolenti.
 */
public class PseudoRandom {
    // We must shift 1L or else we get a negative number!
    private static final double INT_TO_DOUBLE = (1.0 / (1L << 31));
    private long seed = 99887766;

    /**
     * Create an instance of SynthRandom.
     */
    public PseudoRandom() {
        this(new Random().nextInt());
    }

    /**
     * Create an instance of PseudoRandom.
     */
    public PseudoRandom(int seed) {
        setSeed(seed);
    }

    public void setSeed(int seed) {
        this.seed = (long) seed;
    }

    public int getSeed() {
        return (int) seed;
    }

    /**
     * Returns the next random double from 0.0 to 1.0
     * 
     * @return value from 0.0 to 1.0
     */
    public double random() {
        int positiveInt = nextRandomInteger() & 0x7FFFFFFF;
        return positiveInt * INT_TO_DOUBLE;
    }

    /**
     * Returns the next random double from -1.0 to 1.0
     * 
     * @return value from -1.0 to 1.0
     */
    public double nextRandomDouble() {
        return nextRandomInteger() * INT_TO_DOUBLE;
    }

    /** Calculate random 32 bit number using linear-congruential method. */
    public int nextRandomInteger() {
        // Use values for 64-bit sequence from MMIX by Donald Knuth.
        seed = (seed * 6364136223846793005L) + 1442695040888963407L;
        return (int) (seed >> 32); // The higher bits have a longer sequence.
    }

    public int choose(int range) {
        long positiveInt = nextRandomInteger() & 0x7FFFFFFF;
        long temp = positiveInt * range;
        return (int) (temp >> 31);
    }
}
