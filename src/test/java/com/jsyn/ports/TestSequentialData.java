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

import com.jsyn.data.FloatSample;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSequentialData {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSequentialData.class);

    private final static float[] data1 = {
            0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f
    };

    private final static float[] data2 = {
            20.0f, 19.0f, 18.0f, 17.0f, 16.0f, 15.0f, 14.0f, 13.0f, 12.0f, 11.0f
    };

    @Test
    public void testCrossfade() {
        var sample1 = new FloatSample(data1);
        var sample2 = new FloatSample(data2);
        SequentialDataCrossfade xfade = new SequentialDataCrossfade();
        xfade.setup(sample1, 4, 3, sample2, 1, 6);

        for (int i = 0; i < 3; i++) {
            double factor = i / 3.0;
            double value = ((1.0 - factor) * data1[i + 4]) + (factor * data2[i + 1]);
            LOGGER.debug("i = " + i + ", factor = " + factor + ", value = " + value);
            assertEquals(value, xfade.readDouble(i), 0.00001, "crossfade " + i);
        }
        for (int i = 3; i < 6; i++) {
            assertEquals(sample2.readDouble(i + 1), xfade.readDouble(i), 0.00001, "crossfade " + i);
        }
    }
}
