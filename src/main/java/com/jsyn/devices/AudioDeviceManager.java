/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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

package com.jsyn.devices;

/**
 * Interface for an audio system. This may be implemented using JavaSound, or a native device
 * wrapper.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public interface AudioDeviceManager {
    /**
     * Pass this value to the start method to request the default device ID.
     */
    public final static int USE_DEFAULT_DEVICE = -1;

    /**
     * @return The number of devices available.
     */
    public int getDeviceCount();

    /**
     * Get the name of an audio device.
     * 
     * @param deviceID An index between 0 to deviceCount-1.
     * @return A name that can be shown to the user.
     */
    public String getDeviceName(int deviceID);

    /**
     * @return A name of the device manager that can be shown to the user.
     */
    public String getName();

    /**
     * The user can generally select a default device using a control panel that is part of the
     * operating system.
     * 
     * @return The ID for the input device that the user has selected as the default.
     */
    public int getDefaultInputDeviceID();

    /**
     * The user can generally select a default device using a control panel that is part of the
     * operating system.
     * 
     * @return The ID for the output device that the user has selected as the default.
     */
    public int getDefaultOutputDeviceID();

    /**
     * @param deviceID
     * @return The maximum number of channels that the device will support.
     */
    public int getMaxInputChannels(int deviceID);

    /**
     * @param deviceID An index between 0 to numDevices-1.
     * @return The maximum number of channels that the device will support.
     */
    public int getMaxOutputChannels(int deviceID);

    /**
     * This the lowest latency that the device can support reliably. It should be used for
     * applications that require low latency such as live processing of guitar signals.
     * 
     * @param deviceID An index between 0 to numDevices-1.
     * @return Latency in seconds.
     */
    public double getDefaultLowInputLatency(int deviceID);

    /**
     * This the highest latency that the device can support. High latency is recommended for
     * applications that are not time critical, such as recording.
     * 
     * @param deviceID An index between 0 to numDevices-1.
     * @return Latency in seconds.
     */
    public double getDefaultHighInputLatency(int deviceID);

    public double getDefaultLowOutputLatency(int deviceID);

    public double getDefaultHighOutputLatency(int deviceID);

    /**
     * Set latency in seconds for the audio device. If set to zero then the DefaultLowLatency value
     * for the device will be used. This is just a suggestion that will be used when the
     * AudioDeviceInputStream is started.
     **/
    public int setSuggestedInputLatency(double latency);

    public int setSuggestedOutputLatency(double latency);

    /**
     * Create a stream that can be used internally by JSyn for outputting audio data. Applications
     * should not call this directly.
     */
    AudioDeviceOutputStream createOutputStream(int deviceID, int frameRate, int numOutputChannels);

    /**
     * Create a stream that can be used internally by JSyn for acquiring audio input data.
     * Applications should not call this directly.
     */
    AudioDeviceInputStream createInputStream(int deviceID, int frameRate, int numInputChannels);

}
