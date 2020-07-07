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

package com.jsyn.engine;

import java.io.File;
import java.io.IOException;

import com.jsyn.data.FloatSample;
import com.jsyn.util.SampleLoader;
import com.jsyn.util.WaveFileWriter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestWaveFileReadWrite {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestWaveFileReadWrite.class);

    public void checkWriteReadWave(int numChannels, float[] data) throws IOException {
        File temp = File.createTempFile("test_wave", ".wav");
        temp.deleteOnExit();
        LOGGER.debug("Creating wave file " + temp);

        WaveFileWriter writer = new WaveFileWriter(temp);
        writer.setFrameRate(44100);
        writer.setSamplesPerFrame(numChannels);
        writer.setBitsPerSample(16);

        for (float datum : data) {
            writer.write(datum);
        }
        writer.close();

        // TODO Make sure blow up if writing after close.
        // writer.write( 0.7 );

        FloatSample sample = SampleLoader.loadFloatSample(temp);
        assertEquals(numChannels, sample.getChannelsPerFrame(), "stereo");
        assertEquals(44100.0, sample.getFrameRate(), "frame rate");

        for (int i = 0; i < data.length; i++) {
            float v = data[i];
            if (v > 1.0)
                v = 1.0f;
            else if (v < -1.0)
                v = -1.0f;
            assertEquals(v, sample.readDouble(i), 0.0001, "sample data");
        }

    }

    @Test
    public void testRamp() throws IOException {
        float[] data = new float[200];
        for (int i = 0; i < data.length; i++) {
            data[i] = i / 1000.0f;
        }

        checkWriteReadWave(2, data);
    }

    @Test
    public void testClippedSine() throws IOException {
        float[] data = new float[200];
        for (int i = 0; i < data.length; i++) {
            double phase = i * Math.PI * 2.0 / 100;
            data[i] = (float) (1.3 * Math.sin(phase));
        }

        checkWriteReadWave(2, data);
    }

    @Test
    public void testArguments() throws IOException {
        File temp = File.createTempFile("test_wave", ".wav");
        temp.deleteOnExit();
        LOGGER.debug("Creating wave file " + temp);

        WaveFileWriter writer = new WaveFileWriter(temp);
        writer.setBitsPerSample(16);
        assertEquals(16, writer.getBitsPerSample(), "bitsPerSample");
        writer.setBitsPerSample(24);
        assertEquals(24, writer.getBitsPerSample(), "bitsPerSample");
        try {
            writer.setBitsPerSample(17);
            fail("tried setting illegal value");
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
            return;
        } finally {
            writer.close();
        }

        fail("17 generated exception");
    }

}
