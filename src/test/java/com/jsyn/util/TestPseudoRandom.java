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

package com.jsyn.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPseudoRandom {
    PseudoRandom pseudoRandom = new PseudoRandom(12345);
    private int[] bins;
    private final static int BIN_SHIFTER = 8;
    private final static int BIN_COUNT = 1 << BIN_SHIFTER;
    private final static int BIN_MASK = BIN_COUNT - 1;

    @Test
    public void testMath() {
        long seed = 3964771111L;
        int positiveInt = (int) (seed & 0x7FFFFFFF);
        assertTrue((positiveInt >= 0), "masked random positive, " + positiveInt);
        double rand = positiveInt * (1.0 / (1L << 31));
        assertTrue((rand >= 0.0), "not too low, " + rand);
        assertTrue((rand < 1.0), "not too high, " + rand);
    }

    @Test
    public void testIntegerDistribution() {
        int scaler = 100;
        bins = new int[BIN_COUNT];
        for (int i = 0; i < (bins.length * scaler); i++) {
            int rand = pseudoRandom.nextRandomInteger();
            int positiveInt = rand & 0x7FFFFFFF;
            assertTrue((positiveInt >= 0), "masked random " + positiveInt);
            int index = (rand >> (32 - BIN_SHIFTER)) & BIN_MASK;
            bins[index] += 1;
        }
        checkDistribution(scaler);
    }

    @Test
    public void test01Distribution() {
        int scaler = 100;
        bins = new int[BIN_COUNT];
        for (int i = 0; i < (bins.length * scaler); i++) {
            double rand = pseudoRandom.random();
            assertTrue((rand >= 0.0), "not too low, #" + i + " = " + rand);
            assertTrue((rand < 1.0), "not too high, #" + i + " = " + rand);
            int index = (int) (rand * BIN_COUNT);
            bins[index] += 1;
        }
        checkDistribution(scaler);
    }

    private void checkDistribution(int scaler) {
        // Generate running average that should stay near scaler.
    	// TODO This could randomly fail.
        double average = scaler;
        double coefficient = 0.9;
        for (int i = 0; i < (bins.length); i++) {
            average = (average * coefficient) + (bins[i] * (1.0 - coefficient));
            assertEquals(scaler, average, 0.2 * scaler, "average at " + i);
        }
    }
}
