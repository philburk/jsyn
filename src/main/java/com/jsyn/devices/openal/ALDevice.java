/**
 * Copyright 2013-2023 Gothel Software e.K.
 * Copyright 2013-2023 JogAmp Community.
 * Copyright 2009 Phil Burk, Mobileer Inc
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * - AND -
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

package com.jsyn.devices.openal;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.common.av.AudioFormat;
import com.jogamp.common.nio.Buffers;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALCConstants;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALAudioSink;
import com.jsyn.devices.AudioDeviceInputStream;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;

/**
 * Use JOAL/OpenAL to access the audio hardware.
 */
public class ALDevice implements AudioDeviceManager {

    private static final boolean DEBUG = true;
    private static final boolean LITTLE_ENDIAN = true;
    private static final float DEFAULT_LATENCY = 1f/50f; // 50Hz -> 20ms

    @SuppressWarnings("unused")
    private static final AL al;
    private static final ALC alc;
    private static final boolean staticsInitialized;

    private static final List<DeviceInfo> deviceRecords = new ArrayList<DeviceInfo>();
    private static int defaultInputDeviceID = -1;
    private static int defaultOutputDeviceID = -1;
    private static int maxInputChannels = 0;
    private static int maxOutputChannels = 0;

    static {
        if( DEBUG ) {
            System.err.println("ALAudioSink/OpenAL initialization...");
        }
        if( ALAudioSink.isInitialized() ) {
            alc = ALFactory.getALC();
            al = ALFactory.getAL();

            sniffAvailableMixers();
            if( DEBUG ) {
                dumpAvailableMixers();
            }
            staticsInitialized = true;
        } else {
            alc = null;
            al = null;

            if( DEBUG ) {
                System.err.println("ALAudioSink/OpenAL couldn't be initialized");
            }
            staticsInitialized = false;
        }
    }

    private static class DeviceInfo {
        String name;
        boolean isInput;
        boolean isDefault;
        int defaultSampleRate; // Hz
        float latency; // s
        int outputSourceCount;

        @Override
        public String toString() {
            return String.format("AudioDevice: %s, %s, def %b, defSampleRate = %d Hz, latency = %.2f ms, outSourceCount = %d",
                    name, isInput ? "input" : "output", isDefault, defaultSampleRate, 1000f*latency, outputSourceCount);
        }
    }

    private static void dumpAvailableMixers() {
        for (int i=0; i < deviceRecords.size(); ++i) {
            final DeviceInfo deviceInfo = deviceRecords.get(i);
            System.out.printf("%2d: %s%n", i, deviceInfo);
        }
    }
    private static DeviceInfo getDeviceInfo(final int id, final boolean input, final boolean fallBackToDefault) {
        if( 0 <= id && id < deviceRecords.size() ) {
            final DeviceInfo di = deviceRecords.get(id);
            if( di.isInput != input ) {
                System.err.println("DeviceID "+id+" i/p type mismatch, req input "+input+", but is "+di);
                return null;
            }
            return di;
        } else if( fallBackToDefault ) {
            return deviceRecords.get(input ? defaultInputDeviceID : defaultOutputDeviceID);
        }
        return null;
    }
    private static DeviceInfo getDeviceInfo(final int id) {
        if( 0 <= id && id < deviceRecords.size() ) {
            return deviceRecords.get(id);
        }
        return null;
    }

    /**
     * Build device info and determine default devices.
     */
    private static void sniffAvailableMixers() {
        final String defOutDeviceName = alc.alcGetString(null, ALCConstants.ALC_DEFAULT_DEVICE_SPECIFIER);
        final String defInDeviceName = alc.alcGetString(null, ALCConstants.ALC_CAPTURE_DEFAULT_DEVICE_SPECIFIER);
        {
            final String[] outDevices = alc.alcGetDeviceSpecifiers();
            if( null != outDevices ) {
                for (int i=0; i < outDevices.length; ++i) {
                    final DeviceInfo di = new DeviceInfo();
                    di.name = outDevices[i];
                    di.isInput = false;
                    enumDevice(di, alc, defOutDeviceName, defInDeviceName);
                    deviceRecords.add(di);
                    if( di.isDefault ) {
                        defaultOutputDeviceID = i;
                    }
                }
            }
        }
        {
            final String[] inDevices = alc.alcGetCaptureDeviceSpecifiers();
            if( null != inDevices ) {
                for (int i=0; i < inDevices.length; ++i) {
                    final DeviceInfo di = new DeviceInfo();
                    di.name = inDevices[i];
                    di.isInput = true;
                    enumDevice(di, alc, defOutDeviceName, defInDeviceName);
                    deviceRecords.add(di);
                    if( di.isDefault ) {
                        defaultInputDeviceID = i;
                    }
                }
            }
        }
        maxInputChannels = 1; // FIXME
    }
    private static boolean enumDevice(final DeviceInfo di, final ALC alc, final String defOutDeviceName, final String defInDeviceName) {
        if( di.isInput ) {
            di.isDefault = di.name.equals(defInDeviceName);
            di.defaultSampleRate = 44100; // FIXME
            di.latency = DEFAULT_LATENCY; // FIXME
            di.outputSourceCount = 0;
            return true;
        } else {
            final ALAudioSink sink;
            try {
                sink = new ALAudioSink(di.name);
            } catch(final Throwable e) {
                e.printStackTrace();
                return false;
            }
            final AudioFormat nativeFormat = sink.getNativeFormat();
            di.isDefault = di.name.equals(defOutDeviceName);
            di.defaultSampleRate = nativeFormat.sampleRate;
            di.latency = sink.getDefaultLatency();
            di.outputSourceCount = sink.getSourceCount();

            if( di.isDefault ) {
                maxOutputChannels = nativeFormat.channelCount;
            }
            sink.destroy();
            return true;
        }
    }

    /** 30ms -> 23kB for 48000, stereo, double64 */
    private double suggestedOutputLatency = 0.030;
    /** 100ms -> ~30kB for 48000, mono, double64 */
    private double suggestedInputLatency = 0.100;

    public ALDevice() {
        if( !staticsInitialized ) {
            throw new RuntimeException("ALDevice failed to initialize JOAL/OpenAL subsystem.");
        }
    }

    private static class ALStream {
        DeviceInfo di;

        boolean useDouble64SampleType;
        int bytesPerSample;
        AudioFormat format;

        /**
         * @param deviceID device ID
         * @param sampleRate sampler per second, e.g. 44100
         * @param channelCount channel count
         */
        public ALStream(final int deviceID, final boolean input, final int sampleRate, final int channelCount) {
            di = getDeviceInfo(deviceID, input, true);
            if( null == di ) {
                throw new IllegalArgumentException("Can't load deviceID "+deviceID);
            }
        }

        int calculateBufferSize(final double suggestedOutputLatency) {
            final int numFrames = (int) (suggestedOutputLatency * format.sampleRate);
            return numFrames * format.channelCount * bytesPerSample;
        }

    }

    private static ByteBuffer allocate(final int size) {
        // return ByteBuffer.allocate(size);
        return Buffers.newDirectByteBuffer(size);
    }

    private class ALOutputStream extends ALStream implements AudioDeviceOutputStream {
        ALAudioSink sink;
        ByteBuffer sampleBuffer = null;

        /**
         * @param deviceID device ID
         * @param sampleRate sampler per second, e.g. 44100
         * @param channelCount channel count
         */
        public ALOutputStream(final int deviceID, final int sampleRate, final int channelCount) {
            super(deviceID, false, sampleRate, channelCount);
            if( di.isInput ) {
                throw new RuntimeException("deviceID "+deviceID+", is not an output device: "+di);
            }
            sink = new ALAudioSink(di.name);
            final AudioFormat d64 = new AudioFormat(sampleRate, 8<<3, channelCount, true /* signed */,
                                                    false /* fixed point */, false /* planar */, LITTLE_ENDIAN);
            if( sink.isSupported(d64) ) {
                useDouble64SampleType = true;
                bytesPerSample = 8;
                format = d64;
            } else {
                useDouble64SampleType = false;
                bytesPerSample = 2;
                format = new AudioFormat(sampleRate, bytesPerSample<<3, channelCount, true /* signed */,
                                         true /* fixed point */, false /* planar */, LITTLE_ENDIAN);
            }
            sampleBuffer = allocate(calculateBufferSize(suggestedOutputLatency));
            if( DEBUG ) {
                System.err.println("Created: "+sink);
            }
        }

        @Override
        public void start() {
            final int frameDuration = 10; // ms, observed 1.45 ms/frame
            if( !sink.init(format, frameDuration,
                           10 /* ms initial queue, 1 frame, let write() determine actual frameDuration (size in ms) */,
                           32 /* ms queue growth */,
                           32 /* ms queue limit */) )
            {
                // FIXME: jsyn API may allow to simply return false?
                throw new RuntimeException("Couldn't initialize ALAudioSink w/ format "+format+", frameDuration "+frameDuration+": "+sink);
            }
            sink.play();
            if( DEBUG ) {
                System.err.println("Start: "+sink);
            }
        }

        final double[] oneDouble = { 0 };

        @Override
        public void write(final double value) {
            oneDouble[0] = value;
            write(oneDouble, 0, 1);
        }

        @Override
        public void write(final double[] buffer) {
            write(buffer, 0, buffer.length);
        }

        @Override
        public void write(final double[] buffer, final int start, final int sample_count) {
            if( sampleBuffer.capacity() < bytesPerSample*sample_count ) {
                if( DEBUG ) {
                    System.err.printf("SampleBuffer grow: %d -> %d%n", sampleBuffer.capacity(), bytesPerSample*sample_count);
                }
                sampleBuffer = allocate(bytesPerSample*sample_count);
            }

            if( useDouble64SampleType ) {
                final DoubleBuffer d64sb = sampleBuffer.asDoubleBuffer();
                for(int i=0; i<sample_count; ++i) {
                    d64sb.put( buffer[i + start] );
                }
            } else {
                for(int i=0; i<sample_count; ++i) {
                    // Offset before casting so that we can avoid using floor().
                    // Also round by adding 0.5 so that very small signals go to zero.
                    final double temp = (32767.0 * buffer[i + start]) + 32768.5;
                    int s16 = ((int) temp) - 32768;
                    if (s16 > Short.MAX_VALUE) {
                        s16 = Short.MAX_VALUE;
                    } else if (s16 < Short.MIN_VALUE) {
                        s16 = Short.MIN_VALUE;
                    }
                    sampleBuffer.put( (byte) ( s16 & 0xff ) );
                    sampleBuffer.put( (byte) ( ( s16 >>> 8 ) & 0xff ) );
                }
            }
            sampleBuffer.rewind();
            sink.enqueueData(0 /* pts */, sampleBuffer, sample_count*bytesPerSample);
            sampleBuffer.clear();
        }

        @Override
        public void stop() {
            if( null != sink ) {
                if( DEBUG ) {
                    System.err.println("Stop: "+sink);
                }
                sink.destroy();
                sink = null;
            } else {
                new RuntimeException("AudioOutput stop attempted w/o sink")
                        .printStackTrace();
            }
        }

        @Override
        public double getLatency() {
            if (null == sink) {
                return 0.0;
            }
            final int numBytes = sink.getQueuedByteCount();
            final int numSamples = numBytes / (bytesPerSample * format.channelCount);
            return ((double) numSamples) / format.sampleRate;
        }

        @Override
        public void close() {
            if( null != sink ) {
                sink.destroy();
            }
        }
    }

    private class ALInputStream extends ALStream implements AudioDeviceInputStream {
        public ALInputStream(final int deviceID, final int sampleRate, final int channelCount) {
            super(deviceID, true, sampleRate, channelCount);
            if( !di.isInput ) {
                throw new RuntimeException("deviceID "+deviceID+", is not an input device: "+di);
            }
            throw new RuntimeException("Input not yet supported; Requested "+di);
        }

        @Override
        public void start() {
            throw new RuntimeException("Input not yet supported; Requested "+di);
        }

        final double[] oneDouble = { 0 };

        @Override
        public double read() {
            read(oneDouble, 0, 1);
            return oneDouble[0];
        }

        @Override
        public int read(final double[] buffer) {
            return read(buffer, 0, buffer.length);
        }

        @Override
        public int read(final double[] buffer, final int start, final int count) {
            throw new RuntimeException("Input not yet supported; Requested "+di);
        }

        @Override
        public void stop() {
        }

        @Override
        public double getLatency() {
            return suggestedInputLatency;
        }

        @Override
        public int available() {
            return 0;
        }

        @Override
        public void close() {
        }

    }

    /**
     * {@inheritDoc}
     * <p>
     * Original API doc parameter names were misleading (wrong), i.e. 'frameRate' was used for actual
     * 'sampleRate', e.g. 44100 not 'frames/second'.
     * </p>
     * @param deviceID device ID
     * @param sampleRate sampler per second, e.g. 44100
     * @param channelCount channel count
     */
    @Override
    public AudioDeviceOutputStream createOutputStream(final int deviceID, final int sampleRate, final int channelCount) {
        return new ALOutputStream(deviceID, sampleRate, channelCount);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Original API doc parameter names were misleading (wrong), i.e. 'frameRate' was used for actual
     * 'sampleRate', e.g. 44100 not 'frames/second'.
     * </p>
     * @param deviceID device ID
     * @param sampleRate sampler per second, e.g. 44100
     * @param channelCount channel count
     */
    @Override
    public AudioDeviceInputStream createInputStream(final int deviceID, final int sampleRate, final int channelCount) {
        return new ALInputStream(deviceID, sampleRate, channelCount);
    }

    @Override
    public double getDefaultHighInputLatency(final int deviceID) {
        return 3 * getDefaultLowInputLatency(deviceID);
    }

    @Override
    public double getDefaultHighOutputLatency(final int deviceID) {
        return 3 * getDefaultLowOutputLatency(deviceID);
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
    public double getDefaultLowInputLatency(final int deviceID) {
        final DeviceInfo di = getDeviceInfo(deviceID);
        if( null != di ) {
            return di.latency;
        } else {
            return DEFAULT_LATENCY;
        }
    }

    @Override
    public double getDefaultLowOutputLatency(final int deviceID) {
        final DeviceInfo di = getDeviceInfo(deviceID);
        if( null != di ) {
            return di.latency;
        } else {
            return DEFAULT_LATENCY;
        }
    }

    @Override
    public int getDeviceCount() {
        return deviceRecords.size();
    }

    @Override
    public String getDeviceName(final int deviceID) {
        return deviceRecords.get(deviceID).name;
    }

    @Override
    public int getMaxInputChannels(final int deviceID) {
        return maxInputChannels;
    }

    @Override
    public int getMaxOutputChannels(final int deviceID) {
        return maxOutputChannels;
    }

    @Override
    public int setSuggestedOutputLatency(final double latency) {
        suggestedOutputLatency = latency;
        return 0;
    }

    @Override
    public int setSuggestedInputLatency(final double latency) {
        suggestedInputLatency = latency;
        return 0;
    }

    @Override
    public String getName() {
        return "JOAL/OpenAL";
    }

}
