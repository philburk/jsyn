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

package com.jsyn.ports;

import junit.framework.TestCase;

import com.jsyn.data.FloatSample;

public class TestSequentialData extends TestCase {

    float[] data1 = {
            0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f
    };
    FloatSample sample1;
    float[] data2 = {
            20.0f, 19.0f, 18.0f, 17.0f, 16.0f, 15.0f, 14.0f, 13.0f, 12.0f, 11.0f
    };
    FloatSample sample2;

    public void testCrossfade() {
        sample1 = new FloatSample(data1);
        sample2 = new FloatSample(data2);
        SequentialDataCrossfade xfade = new SequentialDataCrossfade();
        xfade.setup(sample1, 4, 3, sample2, 1, 6);

        for (int i = 0; i < 3; i++) {
            double factor = i / 3.0;
            double value = ((1.0 - factor) * data1[i + 4]) + (factor * data2[i + 1]);
            System.out.println("i = " + i + ", factor = " + factor + ", value = " + value);
            assertEquals("crossfade " + i, value, xfade.readDouble(i), 0.00001);
        }
        for (int i = 3; i < 6; i++) {
            assertEquals("crossfade " + i, sample2.readDouble(i + 1), xfade.readDouble(i), 0.00001);
        }
    }
}
