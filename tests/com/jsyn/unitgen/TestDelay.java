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

import com.jsyn.util.AudioStreamReader;

public class TestDelay extends NonRealTimeTestCase {
    public void testFloor() {
        double x = -7.3;
        int n = (int) Math.floor(x);
        assertEquals("int", -8, n);
    }

    public void checkInterpolatingDelay(int maxFrames, double delayFrames)
            throws InterruptedException {
        synthesisEngine.start();

        System.out.printf("test delayFrames = %7.5f\n", delayFrames);
        InterpolatingDelay delay = new InterpolatingDelay();
        synthesisEngine.add(delay);
        delay.allocate(maxFrames);
        delay.delay.set(delayFrames / 44100.0);
        SawtoothOscillator osc = new SawtoothOscillator();
        synthesisEngine.add(osc);
        osc.frequency.set(synthesisEngine.getFrameRate() / 4.0);
        osc.amplitude.set(1.0);
        osc.output.connect(delay.input);

        int samplesPerFrame = 1;
        AudioStreamReader reader = new AudioStreamReader(synthesisEngine, samplesPerFrame);
        delay.output.connect(reader.getInput());

        delay.start();
        for (int i = 0; i < (3 * maxFrames); i++) {
            if (reader.available() == 0) {
                synthesisEngine.sleepFor(0.01);
            }
            double actual = reader.read();
            double expected = 1 + i - delayFrames;
            if (expected < 0.0) {
                expected = 0.0;
            }
            // System.out.printf( "[%d] expected = %7.3f, delayed = %7.3f\n", i, expected, actual );
            // assertEquals("delayed output", expected, actual, 0.00001);
        }
    }

    public void testSmall() throws InterruptedException {
        checkInterpolatingDelay(40, 7.0);
    }

    public void testEven() throws InterruptedException {
        checkInterpolatingDelay(44100, 13671.0);
    }

    public void testInterpolatingDelay() throws InterruptedException {
        checkInterpolatingDelay(44100, 13671.4);
    }
}
