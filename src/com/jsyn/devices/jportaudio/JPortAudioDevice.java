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

package com.jsyn.devices.jportaudio;

import com.jsyn.devices.AudioDeviceInputStream;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;
import com.portaudio.BlockingStream;
import com.portaudio.DeviceInfo;
import com.portaudio.HostApiInfo;
import com.portaudio.PortAudio;
import com.portaudio.StreamParameters;

public class JPortAudioDevice implements AudioDeviceManager {
    private double suggestedOutputLatency = 0.030;
    private double suggestedInputLatency = 0.050;
    private static final int FRAMES_PER_BUFFER = 128;

    // static Logger logger = Logger.getLogger( JPortAudioDevice.class.getName() );

    public JPortAudioDevice() {
        PortAudio.initialize();
    }

    @Override
    public int getDeviceCount() {
        return PortAudio.getDeviceCount();
    }

    @Override
    public String getDeviceName(int deviceID) {
        DeviceInfo deviceInfo = PortAudio.getDeviceInfo(deviceID);
        HostApiInfo hostInfo = PortAudio.getHostApiInfo(deviceInfo.hostApi);
        return deviceInfo.name + " - " + hostInfo.name;
    }

    @Override
    public int getDefaultInputDeviceID() {
        return PortAudio.getDefaultInputDevice();
    }

    @Override
    public int getDefaultOutputDeviceID() {
        return PortAudio.getDefaultOutputDevice();
    }

    @Override
    public int getMaxInputChannels(int deviceID) {
        if (deviceID < 0) {
            deviceID = PortAudio.getDefaultInputDevice();
        }
        return PortAudio.getDeviceInfo(deviceID).maxInputChannels;
    }

    @Override
    public int getMaxOutputChannels(int deviceID) {
        if (deviceID < 0) {
            deviceID = PortAudio.getDefaultOutputDevice();
        }
        return PortAudio.getDeviceInfo(deviceID).maxOutputChannels;
    }

    @Override
    public double getDefaultLowInputLatency(int deviceID) {
        if (deviceID < 0) {
            deviceID = PortAudio.getDefaultInputDevice();
        }
        return PortAudio.getDeviceInfo(deviceID).defaultLowInputLatency;
    }

    @Override
    public double getDefaultHighInputLatency(int deviceID) {
        if (deviceID < 0) {
            deviceID = PortAudio.getDefaultInputDevice();
        }
        return PortAudio.getDeviceInfo(deviceID).defaultHighInputLatency;
    }

    @Override
    public double getDefaultLowOutputLatency(int deviceID) {
        if (deviceID < 0) {
            deviceID = PortAudio.getDefaultOutputDevice();
        }
        return PortAudio.getDeviceInfo(deviceID).defaultLowOutputLatency;
    }

    @Override
    public double getDefaultHighOutputLatency(int deviceID) {
        if (deviceID < 0) {
            deviceID = PortAudio.getDefaultOutputDevice();
        }
        return PortAudio.getDeviceInfo(deviceID).defaultHighOutputLatency;
    }

    @Override
    public int setSuggestedOutputLatency(double latency) {
        suggestedOutputLatency = latency;
        return 0;
    }

    @Override
    public int setSuggestedInputLatency(double latency) {
        suggestedInputLatency = latency;
        return 0;
    }

    @Override
    public AudioDeviceOutputStream createOutputStream(int deviceID, int frameRate,
            int samplesPerFrame) {
        return new JPAOutputStream(deviceID, frameRate, samplesPerFrame);
    }

    @Override
    public AudioDeviceInputStream createInputStream(int deviceID, int frameRate, int samplesPerFrame) {
        return new JPAInputStream(deviceID, frameRate, samplesPerFrame);
    }

    private class JPAStream {
        BlockingStream blockingStream;
        float[] floatBuffer = null;
        int samplesPerFrame;

        public void close() {
            blockingStream.close();
        }

        public void start() {
            blockingStream.start();
        }

        public void stop() {
            blockingStream.stop();
        }

    }

    private class JPAOutputStream extends JPAStream implements AudioDeviceOutputStream {

        private JPAOutputStream(int deviceID, int frameRate, int samplesPerFrame) {
            this.samplesPerFrame = samplesPerFrame;
            StreamParameters streamParameters = new StreamParameters();
            streamParameters.channelCount = samplesPerFrame;
            if (deviceID < 0) {
                deviceID = PortAudio.getDefaultOutputDevice();
            }
            streamParameters.device = deviceID;
            streamParameters.suggestedLatency = suggestedOutputLatency;
            int flags = 0;
            System.out.println("Audio output on " + getDeviceName(deviceID));
            blockingStream = PortAudio.openStream(null, streamParameters, frameRate,
                    FRAMES_PER_BUFFER, flags);
        }

        /** Grossly inefficient. Call the array version instead. */
        @Override
        public void write(double value) {
            double[] buffer = new double[1];
            buffer[0] = value;
            write(buffer, 0, 1);
        }

        @Override
        public void write(double[] buffer) {
            write(buffer, 0, buffer.length);
        }

        @Override
        public void write(double[] buffer, int start, int count) {
            // Allocate float buffer if needed.
            if ((floatBuffer == null) || (floatBuffer.length < count)) {
                floatBuffer = new float[count];
            }
            for (int i = 0; i < count; i++) {

                floatBuffer[i] = (float) buffer[i + start];
            }
            blockingStream.write(floatBuffer, count / samplesPerFrame);
        }

        @Override
        public double getLatency() {
            return blockingStream.getInfo().outputLatency;
        }
    }

    private class JPAInputStream extends JPAStream implements AudioDeviceInputStream {
        private JPAInputStream(int deviceID, int frameRate, int samplesPerFrame) {
            this.samplesPerFrame = samplesPerFrame;
            StreamParameters streamParameters = new StreamParameters();
            streamParameters.channelCount = samplesPerFrame;
            if (deviceID < 0) {
                deviceID = PortAudio.getDefaultInputDevice();
            }
            streamParameters.device = deviceID;
            streamParameters.suggestedLatency = suggestedInputLatency;
            int flags = 0;
            System.out.println("Audio input from " + getDeviceName(deviceID));
            blockingStream = PortAudio.openStream(streamParameters, null, frameRate,
                    FRAMES_PER_BUFFER, flags);
        }

        @Override
        public double read() {
            double[] buffer = new double[1];
            read(buffer, 0, 1);
            return buffer[0];
        }

        @Override
        public int read(double[] buffer) {
            return read(buffer, 0, buffer.length);
        }

        @Override
        public int read(double[] buffer, int start, int count) {
            // Allocate float buffer if needed.
            if ((floatBuffer == null) || (floatBuffer.length < count)) {
                floatBuffer = new float[count];
            }
            blockingStream.read(floatBuffer, count / samplesPerFrame);

            for (int i = 0; i < count; i++) {

                buffer[i + start] = floatBuffer[i];
            }
            return count;
        }

        @Override
        public double getLatency() {
            return blockingStream.getInfo().inputLatency;
        }

        @Override
        public int available() {
            return blockingStream.getReadAvailable() * samplesPerFrame;
        }

    }

    @Override
    public String getName() {
        return "JPortAudio";
    }
}
