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

package com.jsyn.unitgen;

import junit.framework.TestCase;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;

public class TestConnections extends TestCase {
    Add add1;
    Add add2;
    Add add3;

    Synthesizer synth;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        synth = JSyn.createSynthesizer();

        synth.add(add1 = new Add());
        synth.add(add2 = new Add());
        synth.add(add3 = new Add());

        add1.start();
        add2.start();
        add3.start();

        add1.inputA.set(0.1);
        add1.inputB.set(0.2);

        add2.inputA.set(0.4);
        add2.inputB.set(0.8);

        add3.inputA.set(1.6);
        add3.inputB.set(3.2);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSet() throws InterruptedException {
        synth.sleepFor(0.01);
        assertEquals("set inputs of adder", 0.3, add1.output.getValue(), 0.0001);
    }

    public void testConnect() throws InterruptedException {
        synth.sleepFor(0.01);
        assertEquals("set inputs of adder", 0.3, add1.output.getValue(), 0.0001);
        assertEquals("set inputs of adder", 1.2, add2.output.getValue(), 0.0001);

        // Test different ways of connecting.
        add1.output.connect(add2.inputB);
        checkConnection();

        add1.output.connect(0, add2.inputB, 0);
        checkConnection();

        add1.output.connect(add2.inputB.getConnectablePart(0));
        checkConnection();

        add1.output.getConnectablePart(0).connect(add2.inputB);
        checkConnection();

        add1.output.getConnectablePart(0).connect(add2.inputB.getConnectablePart(0));
        checkConnection();

        add2.inputB.connect(add1.output);
        checkConnection();

        add2.inputB.connect(0, add1.output, 0);
        checkConnection();

        add2.inputB.connect(add1.output.getConnectablePart(0));
        checkConnection();

        add2.inputB.getConnectablePart(0).connect(add1.output);
        checkConnection();

        add2.inputB.getConnectablePart(0).connect(add1.output.getConnectablePart(0));
        checkConnection();
    }

    private void checkConnection() throws InterruptedException {
        synth.sleepFor(0.01);
        assertEquals("connection should not change output", 0.3, add1.output.getValue(), 0.0001);
        assertEquals("replace set value with output", 0.7, add2.output.getValue(), 0.0001);

        // Revert to set value after disconnection.
        add1.output.disconnectAll();
        synth.sleepFor(0.01);
        assertEquals("still the same", 0.3, add1.output.getValue(), 0.0001);
        assertEquals("should revert to original set() value", 1.2, add2.output.getValue(), 0.0001);
    }

}
