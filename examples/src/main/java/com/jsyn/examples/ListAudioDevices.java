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

package com.jsyn.examples;

import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListAudioDevices {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListAudioDevices.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        AudioDeviceManager audioManager = AudioDeviceFactory.createAudioDeviceManager();

        int numDevices = audioManager.getDeviceCount();
        for (int i = 0; i < numDevices; i++) {
            String deviceName = audioManager.getDeviceName(i);
            int maxInputs = audioManager.getMaxInputChannels(i);
            int maxOutputs = audioManager.getMaxInputChannels(i);
            boolean isDefaultInput = (i == audioManager.getDefaultInputDeviceID());
            boolean isDefaultOutput = (i == audioManager.getDefaultOutputDeviceID());
            LOGGER.debug("#" + i + " : " + deviceName);
            LOGGER.debug("  max inputs : " + maxInputs
                    + (isDefaultInput ? "   (default)" : ""));
            LOGGER.debug("  max outputs: " + maxOutputs
                    + (isDefaultOutput ? "   (default)" : ""));
        }

    }

}
