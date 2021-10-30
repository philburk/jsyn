/*
 * Copyright 2021 Phil Burk, Mobileer Inc
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Circuit class
 */
public class TestCircuit {

    @Test
    public void testAddGetUnits() {
        Circuit circuit = new Circuit();
        SineOscillator sine = new SineOscillator();
        circuit.add(sine);
        UnitGenerator units[] = circuit.getUnits();
        assertTrue(circuit != null, "null units array");
        assertEquals(sine, units[0], "sine");

        SawtoothOscillator saw = new SawtoothOscillator();
        circuit.add(saw);
        units = circuit.getUnits();
        assertTrue(circuit != null, "null units array");
        assertEquals(sine, units[0], "sine");
        assertEquals(saw, units[1], "saw");
    }
}
