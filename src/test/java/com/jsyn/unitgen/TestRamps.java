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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRamps extends NonRealTimeTestCase {

    public void viewContinuousRamp(double duration, double startValue, double targetValue)
            throws InterruptedException {
        ContinuousRamp ramp = new ContinuousRamp();
        synthesisEngine.add(ramp);

        ramp.current.set(startValue);
        ramp.input.set(startValue);
        ramp.time.set(duration);

        synthesisEngine.setRealTime(false);
        synthesisEngine.start();
        ramp.start();
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + 0.01);
        ramp.input.set(targetValue);

        double time = synthesisEngine.getCurrentTime();
        int numLoops = 20;
        double increment = duration / numLoops;
        for (int i = 0; i < (numLoops + 1); i++) {
            double value = ramp.output.getValue();
            System.out.printf("i = %d, t = %9.5f,  value = %8.4f\n", i, time, value);
            time += increment;
            synthesisEngine.sleepUntil(time);
        }

        synthesisEngine.stop();
    }

    public void checkContinuousRamp(double duration, double startValue, double targetValue)
            throws InterruptedException {
        ContinuousRamp ramp = new ContinuousRamp();
        synthesisEngine.add(ramp);

        ramp.current.set(startValue);
        ramp.input.set(startValue);
        ramp.time.set(duration);

        synthesisEngine.setRealTime(false);
        synthesisEngine.start();
        ramp.start();
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + 0.01);
        assertEquals(ramp.input.getValue(), ramp.output.getValue(), "start flat");

        ramp.input.set(targetValue);
        double startTime = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(startTime + (duration / 2));
        assertEquals((targetValue + startValue) / 2.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + duration);
        assertEquals(targetValue, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + duration + 0.1);
        assertEquals(targetValue, ramp.output.getValue(), "flat again");

        synthesisEngine.stop();
    }

    @Test
    public void testContinuousRamp() throws InterruptedException {
        viewContinuousRamp(4.0, 0.0, 1.0);
    }

    @Test
    public void testExponentialRamp() throws InterruptedException {
        ExponentialRamp ramp = new ExponentialRamp();
        synthesisEngine.add(ramp);

        double duration = 0.3;
        ramp.current.set(1.0);
        ramp.input.set(1.0);
        ramp.time.set(duration);

        synthesisEngine.start();
        ramp.start();
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + 0.01);
        assertEquals(ramp.input.getValue(), ramp.output.getValue(), "start flat");

        ramp.input.set(8.0);
        double startTime = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(startTime + 0.1);
        assertEquals(2.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.2);
        assertEquals(4.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.3);
        assertEquals(8.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.4);
        assertEquals(8.0, ramp.output.getValue(), "flat again");
    }

    @Test
    public void testLinearRamp() throws InterruptedException {
        LinearRamp ramp = new LinearRamp();
        synthesisEngine.add(ramp);

        double duration = 0.4;
        ramp.current.set(0.0);
        ramp.input.set(0.0);
        ramp.time.set(duration);

        synthesisEngine.start();
        ramp.start();
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + 0.01);
        assertEquals(ramp.input.getValue(), ramp.output.getValue(), "start flat");

        ramp.input.set(8.0);
        double startTime = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(startTime + 0.1);
        assertEquals(2.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.2);
        assertEquals(4.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.3);
        assertEquals(6.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.4);
        assertEquals(8.0, ramp.output.getValue(), "flat again");
    }

    @Test
    public void testExponentialRampConnected() throws InterruptedException {
        ExponentialRamp ramp = new ExponentialRamp();
        PassThrough pass = new PassThrough();
        synthesisEngine.add(ramp);
        synthesisEngine.add(pass);

        double duration = 0.3;
        ramp.current.set(1.0);
        pass.input.set(1.0);
        ramp.time.set(duration);

        // Send value through a connected unit.
        pass.input.set(1.0);
        pass.output.connect(ramp.input);

        synthesisEngine.start();
        ramp.start();
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + 0.01);
        assertEquals(ramp.input.getValue(), ramp.output.getValue(), "start flat");

        pass.input.set(8.0);
        double startTime = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(startTime + 0.1);
        assertEquals(2.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.2);
        assertEquals(4.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.3);
        assertEquals(8.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.4);
        assertEquals(8.0, ramp.output.getValue(), "flat again");
    }

    @Test
    public void testLinearRampConnected() throws InterruptedException {
        LinearRamp ramp = new LinearRamp();
        PassThrough pass = new PassThrough();
        synthesisEngine.add(ramp);
        synthesisEngine.add(pass);

        double duration = 0.4;
        ramp.current.set(0.0);
        pass.input.set(0.0);
        ramp.time.set(duration);

        // Send value through a connected unit.
        pass.input.set(0.0);
        pass.output.connect(ramp.input);

        synthesisEngine.start();
        ramp.start();
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + 0.01);
        assertEquals(ramp.input.getValue(), ramp.output.getValue(), "start flat");

        pass.input.set(8.0);
        double startTime = synthesisEngine.getCurrentTime();
        synthesisEngine.sleepUntil(startTime + 0.1);
        assertEquals(2.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.2);
        assertEquals(4.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.3);
        assertEquals(6.0, ramp.output.getValue(), 0.01, "ramping up");
        synthesisEngine.sleepUntil(startTime + 0.4);
        assertEquals(8.0, ramp.output.getValue(), "flat again");
    }

}
