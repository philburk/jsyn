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

package com.jsyn.devices.javasound;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.jsyn.devices.AudioDeviceInputStream;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;

/**
 * Use JavaSound to access the audio hardware.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class JavaSoundAudioDevice implements AudioDeviceManager {
    private static final int BYTES_PER_SAMPLE = 2;
    private static final boolean USE_BIG_ENDIAN = false;

    ArrayList<DeviceInfo> deviceRecords;
    private double suggestedOutputLatency = 0.040;
    private double suggestedInputLatency = 0.100;
    private int defaultInputDeviceID = -1;
    private int defaultOutputDeviceID = -1;

    static Logger logger = Logger.getLogger(JavaSoundAudioDevice.class.getName());

    public JavaSoundAudioDevice() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            suggestedOutputLatency = 0.08;
            logger.info("JSyn: default output latency set to "
                    + ((int) (suggestedOutputLatency * 1000)) + " msec for " + osName);
        }
        deviceRecords = new ArrayList<DeviceInfo>();
        sniffAvailableMixers();
        dumpAvailableMixers();
    }

    private void dumpAvailableMixers() {
        for (DeviceInfo deviceInfo : deviceRecords) {
            logger.fine("" + deviceInfo);
        }
    }

    /**
     * Build device info and determine default devices.
     */
    private void sniffAvailableMixers() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixers.length; i++) {
            DeviceInfo deviceInfo = new DeviceInfo();

            deviceInfo.name = mixers[i].getName();
            Mixer mixer = AudioSystem.getMixer(mixers[i]);

            Line.Info[] lines = mixer.getTargetLineInfo();
            deviceInfo.maxInputs = scanMaxChannels(lines);
            // Remember first device that supports input.
            if ((defaultInputDeviceID < 0) && (deviceInfo.maxInputs > 0)) {
                defaultInputDeviceID = i;
            }

            lines = mixer.getSourceLineInfo();
            deviceInfo.maxOutputs = scanMaxChannels(lines);
            // Remember first device that supports output.
            if ((defaultOutputDeviceID < 0) && (deviceInfo.maxOutputs > 0)) {
                defaultOutputDeviceID = i;
            }

            deviceRecords.add(deviceInfo);
        }
    }

    private int scanMaxChannels(Line.Info[] lines) {
        int maxChannels = 0;
        for (Line.Info line : lines) {
            if (line instanceof DataLine.Info) {
                int numChannels = scanMaxChannels(((DataLine.Info) line));
                if (numChannels > maxChannels) {
                    maxChannels = numChannels;
                }
            }
        }
        return maxChannels;
    }

    private int scanMaxChannels(DataLine.Info info) {
        int maxChannels = 0;
        for (AudioFormat format : info.getFormats()) {
            int numChannels = format.getChannels();
            if (numChannels > maxChannels) {
                maxChannels = numChannels;
            }
        }
        return maxChannels;
    }

    class DeviceInfo {
        String name;
        int maxInputs;
        int maxOutputs;

        @Override
        public String toString() {
            return "AudioDevice: " + name + ", max in = " + maxInputs + ", max out = " + maxOutputs;
        }
    }

    private class JavaSoundStream {
        AudioFormat format;
        byte[] bytes;
        int frameRate;
        int deviceID;
        int samplesPerFrame;

        public JavaSoundStream(int deviceID, int frameRate, int samplesPerFrame) {
            this.deviceID = deviceID;
            this.frameRate = frameRate;
            this.samplesPerFrame = samplesPerFrame;
            format = new AudioFormat(frameRate, 16, samplesPerFrame, true, USE_BIG_ENDIAN);
        }

        Line getDataLine(DataLine.Info info) throws LineUnavailableException {
            Line dataLine;
            if (deviceID >= 0) {
                Mixer.Info[] mixers = AudioSystem.getMixerInfo();
                Mixer mixer = AudioSystem.getMixer(mixers[deviceID]);
                dataLine = mixer.getLine(info);
            } else {
                dataLine = AudioSystem.getLine(info);
            }
            return dataLine;
        }

        int calculateBufferSize(double suggestedOutputLatency) {
            int numFrames = (int) (suggestedOutputLatency * frameRate);
            int numBytes = numFrames * samplesPerFrame * BYTES_PER_SAMPLE;
            return numBytes;
        }

    }

    private class JavaSoundOutputStream extends JavaSoundStream implements AudioDeviceOutputStream {
        SourceDataLine line;

        public JavaSoundOutputStream(int deviceID, int frameRate, int samplesPerFrame) {
            super(deviceID, frameRate, samplesPerFrame);
        }

        @Override
        public void start() {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                // Handle the error.
                logger.severe("JavaSoundOutputStream - not supported." + format);
            } else {
                try {
                    line = (SourceDataLine) getDataLine(info);
                    int bufferSize = calculateBufferSize(suggestedOutputLatency);
                    line.open(format, bufferSize);
                    logger.fine("Output buffer size = " + bufferSize + " bytes.");
                    line.start();

                } catch (Exception e) {
                    e.printStackTrace();
                    line = null;
                }
            }
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
            // Allocate byte buffer if needed.
            if ((bytes == null) || ((bytes.length * 2) < count)) {
                bytes = new byte[count * 2];
            }

            // Convert float samples to LittleEndian bytes.
            int byteIndex = 0;
            for (int i = 0; i < count; i++) {
                // Offset before casting so that we can avoid using floor().
                // Also round by adding 0.5 so that very small signals go to zero.
                double temp = (32767.0 * buffer[i + start]) + 32768.5;
                int sample = ((int) temp) - 32768;
                if (sample > Short.MAX_VALUE) {
                    sample = Short.MAX_VALUE;
                } else if (sample < Short.MIN_VALUE) {
                    sample = Short.MIN_VALUE;
                }
                bytes[byteIndex++] = (byte) sample; // little end
                bytes[byteIndex++] = (byte) (sample >> 8); // big end
            }

            line.write(bytes, 0, byteIndex);
        }

        @Override
        public void stop() {
            if (line != null) {
                line.stop();
                line.flush();
                line.close();
                line = null;
            } else {
                new RuntimeException("AudioOutput stop attempted when no line created.")
                        .printStackTrace();
            }
        }

        @Override
        public double getLatency() {
            if (line == null) {
                return 0.0;
            }
            int numBytes = line.getBufferSize();
            int numFrames = numBytes / (BYTES_PER_SAMPLE * samplesPerFrame);
            return ((double) numFrames) / frameRate;
        }

        @Override
        public void close() {
        }

    }

    private class JavaSoundInputStream extends JavaSoundStream implements AudioDeviceInputStream {
        TargetDataLine line;

        public JavaSoundInputStream(int deviceID, int frameRate, int samplesPerFrame) {
            super(deviceID, frameRate, samplesPerFrame);
        }

        @Override
        public void start() {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                // Handle the error.
                logger.severe("JavaSoundInputStream - not supported." + format);
            } else {
                try {
                    line = (TargetDataLine) getDataLine(info);
                    int bufferSize = calculateBufferSize(suggestedInputLatency);
                    line.open(format, bufferSize);
                    logger.fine("Input buffer size = " + bufferSize + " bytes.");
                    line.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    line = null;
                }
            }
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
            // Allocate byte buffer if needed.
            if ((bytes == null) || ((bytes.length * 2) < count)) {
                bytes = new byte[count * 2];
            }
            int bytesRead = line.read(bytes, 0, bytes.length);

            // Convert BigEndian bytes to float samples
            int bi = 0;
            for (int i = 0; i < count; i++) {
                int sample = bytes[bi++] & 0x00FF; // little end
                sample = sample + (bytes[bi++] << 8); // big end
                buffer[i + start] = sample * (1.0 / 32767.0);
            }
            return bytesRead / 4;
        }

        @Override
        public void stop() {
            if (line != null) {
                line.drain();
                line.close();
            } else {
                new RuntimeException("AudioInput stop attempted when no line created.")
                        .printStackTrace();
            }
        }

        @Override
        public double getLatency() {
            if (line == null) {
                return 0.0;
            }
            int numBytes = line.getBufferSize();
            int numFrames = numBytes / (BYTES_PER_SAMPLE * samplesPerFrame);
            return ((double) numFrames) / frameRate;
        }

        @Override
        public int available() {
            return line.available() / BYTES_PER_SAMPLE;
        }

        @Override
        public void close() {
        }

    }

    @Override
    public AudioDeviceOutputStream createOutputStream(int deviceID, int frameRate,
            int samplesPerFrame) {
        return new JavaSoundOutputStream(deviceID, frameRate, samplesPerFrame);
    }

    @Override
    public AudioDeviceInputStream createInputStream(int deviceID, int frameRate, int samplesPerFrame) {
        return new JavaSoundInputStream(deviceID, frameRate, samplesPerFrame);
    }

    @Override
    public double getDefaultHighInputLatency(int deviceID) {
        return 0.300;
    }

    @Override
    public double getDefaultHighOutputLatency(int deviceID) {
        return 0.300;
    }

    @Override
    public int getDefaultInputDeviceID() {
        return defaultInputDeviceID;
    }

    @Override
    public int getDefaultOutputDeviceID() {
        return defaultOutputDeviceID;
    }

    @Override
    public double getDefaultLowInputLatency(int deviceID) {
        return 0.100;
    }

    @Override
    public double getDefaultLowOutputLatency(int deviceID) {
        return 0.100;
    }

    @Override
    public int getDeviceCount() {
        return deviceRecords.size();
    }

    @Override
    public String getDeviceName(int deviceID) {
        return deviceRecords.get(deviceID).name;
    }

    @Override
    public int getMaxInputChannels(int deviceID) {
        return deviceRecords.get(deviceID).maxInputs;
    }

    @Override
    public int getMaxOutputChannels(int deviceID) {
        return deviceRecords.get(deviceID).maxOutputs;
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
    public String getName() {
        return "JavaSound";
    }

}
