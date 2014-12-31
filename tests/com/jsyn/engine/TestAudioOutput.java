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

import java.io.IOException;

import junit.framework.TestCase;

import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;
import com.jsyn.devices.javasound.JavaSoundAudioDevice;

/**
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TestAudioOutput extends TestCase {

    SynthesisEngine synthesisEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMonoSine() throws IOException {
        System.out.println("Test mono output.");
        final int FRAMES_PER_BUFFER = 128;
        final int SAMPLES_PER_FRAME = 1;
        double[] buffer = new double[FRAMES_PER_BUFFER * SAMPLES_PER_FRAME];
        AudioDeviceManager audioDevice = new JavaSoundAudioDevice();
        AudioDeviceOutputStream audioOutput = audioDevice.createOutputStream(
                audioDevice.getDefaultOutputDeviceID(), 44100, SAMPLES_PER_FRAME);
        for (int i = 0; i < FRAMES_PER_BUFFER; i++) {
            double angle = (i * Math.PI * 2.0) / FRAMES_PER_BUFFER;
            buffer[i] = Math.sin(angle);
        }
        audioOutput.start();
        for (int i = 0; i < 1000; i++) {
            audioOutput.write(buffer);
        }
        audioOutput.stop();

    }

    public void testStereoSine() throws IOException {
        System.out.println("Test stereo output.");
        final int FRAMES_PER_BUFFER = 128;
        final int SAMPLES_PER_FRAME = 2;
        double[] buffer = new double[FRAMES_PER_BUFFER * SAMPLES_PER_FRAME];
        AudioDeviceManager audioDevice = new JavaSoundAudioDevice();
        AudioDeviceOutputStream audioOutput = audioDevice.createOutputStream(
                audioDevice.getDefaultOutputDeviceID(), 44100, SAMPLES_PER_FRAME);
        int bi = 0;
        for (int i = 0; i < FRAMES_PER_BUFFER; i++) {
            double angle = (i * Math.PI * 2.0) / FRAMES_PER_BUFFER;
            buffer[bi++] = Math.sin(angle);
            buffer[bi++] = Math.sin(angle);
        }
        audioOutput.start();
        for (int i = 0; i < 1000; i++) {
            audioOutput.write(buffer);
        }
        audioOutput.stop();

    }

}
