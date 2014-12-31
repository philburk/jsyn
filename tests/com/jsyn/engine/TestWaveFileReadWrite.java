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

import javax.sound.sampled.UnsupportedAudioFileException;

import junit.framework.TestCase;

import com.jsyn.data.FloatSample;
import com.jsyn.util.SampleLoader;
import com.jsyn.util.WaveFileWriter;

public class TestWaveFileReadWrite extends TestCase {

    public void checkWriteReadWave(int numChannels, float[] data) throws IOException,
            UnsupportedAudioFileException {
        File temp = File.createTempFile("test_wave", ".wav");
        temp.deleteOnExit();
        System.out.println("Creating wave file " + temp);

        WaveFileWriter writer = new WaveFileWriter(temp);
        writer.setFrameRate(44100);
        writer.setSamplesPerFrame(numChannels);
        writer.setBitsPerSample(16);

        for (int i = 0; i < data.length; i++) {
            writer.write(data[i]);
        }
        writer.close();

        // TODO Make sure blow up if writing after close.
        // writer.write( 0.7 );

        FloatSample sample = SampleLoader.loadFloatSample(temp);
        assertEquals("stereo", numChannels, sample.getChannelsPerFrame());
        assertEquals("frame rate", 44100.0, sample.getFrameRate());

        for (int i = 0; i < data.length; i++) {
            float v = data[i];
            if (v > 1.0)
                v = 1.0f;
            else if (v < -1.0)
                v = -1.0f;
            assertEquals("sample data", v, sample.readDouble(i), 0.0001);
        }

    }

    public void testRamp() throws IOException, UnsupportedAudioFileException {
        float[] data = new float[200];
        for (int i = 0; i < data.length; i++) {
            data[i] = i / 1000.0f;
        }

        checkWriteReadWave(2, data);
    }

    public void testClippedSine() throws IOException, UnsupportedAudioFileException {
        float[] data = new float[200];
        for (int i = 0; i < data.length; i++) {
            double phase = i * Math.PI * 2.0 / 100;
            data[i] = (float) (1.3 * Math.sin(phase));
        }

        checkWriteReadWave(2, data);
    }

    public void testArguments() throws IOException {
        File temp = File.createTempFile("test_wave", ".wav");
        temp.deleteOnExit();
        System.out.println("Creating wave file " + temp);

        WaveFileWriter writer = new WaveFileWriter(temp);
        writer.setBitsPerSample(16);
        assertEquals("bitsPerSample", 16, writer.getBitsPerSample());
        writer.setBitsPerSample(24);
        assertEquals("bitsPerSample", 24, writer.getBitsPerSample());
        boolean caughtIt = false;
        try {
            writer.setBitsPerSample(17);
            assertTrue("tried setting illegal value", false);
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
            caughtIt = true;
        }
        assertTrue("17 generated exception", caughtIt);
        writer.close();
    }

}
