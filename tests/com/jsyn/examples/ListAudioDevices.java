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

public class ListAudioDevices {

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
            System.out.println("#" + i + " : " + deviceName);
            System.out.println("  max inputs : " + maxInputs
                    + (isDefaultInput ? "   (default)" : ""));
            System.out.println("  max outputs: " + maxOutputs
                    + (isDefaultOutput ? "   (default)" : ""));
        }

    }

}
