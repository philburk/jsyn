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

package com.jsyn.dsp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.Compare;
import com.jsyn.unitgen.Divide;
import com.jsyn.unitgen.Maximum;
import com.jsyn.unitgen.Minimum;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.MultiplyAdd;
import com.jsyn.unitgen.PitchToFrequency;
import com.jsyn.unitgen.PowerOfTwo;
import com.jsyn.unitgen.Subtract;
import com.jsyn.unitgen.UnitBinaryOperator;
import com.softsynth.math.AudioMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TestSimpleDelay {

    @Test
    public void testProcess() {
        SimpleDelay delay = new SimpleDelay(3);
        assertEquals(0.0f, delay.process(0.0f), 0.00001, "Start with zero");
        assertEquals(0.0f, delay.process(0.7f), 0.00001, "Add an impulse");
        assertEquals(0.0f, delay.process(0.0f), 0.00001, "Waiting 0");
        assertEquals(0.0f, delay.process(0.0f), 0.00001, "Waiting 1");
        assertEquals(0.7f, delay.process(0.0f), 0.00001, "Got it.");
    }

    @Test
    public void testAddRead() {
        SimpleDelay delay = new SimpleDelay(3);
        assertEquals(0.0f, delay.read(0), 0.00001, "read[0]");
        assertEquals(0.0f, delay.read(1), 0.00001, "read[1]");
        assertEquals(0.0f, delay.read(2), 0.00001, "read[2]");
        delay.write(1.23f);
        assertEquals(1.23f, delay.read(0), 0.00001, "w0 read[0]");
        assertEquals(0.0f, delay.read(1), 0.00001, "w0 read[1]");
        assertEquals(0.0f, delay.read(2), 0.00001, "w0 read[2]");
        delay.advance();
        delay.write(0.0f);
        assertEquals(0.0f, delay.read(0), 0.00001, "w1 read[0]");
        assertEquals(1.23f, delay.read(1), 0.00001, "w1 read[1]");
        assertEquals(0.0f, delay.read(2), 0.00001, "w1 read[2]");
        delay.advance();
        delay.write(0.0f);
        assertEquals(0.0f, delay.read(0), 0.00001, "w1 read[0]");
        assertEquals(0.0f, delay.read(1), 0.00001, "w1 read[1]");
        assertEquals(1.23f, delay.read(2), 0.00001, "w1 read[2]");
    }
}
