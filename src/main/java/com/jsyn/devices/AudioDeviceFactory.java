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

import com.jsyn.util.JavaTools;

/**
 * Create a device appropriate for the platform.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class AudioDeviceFactory {
    private static AudioDeviceManager instance;

    /**
     * Use a custom device interface. Overrides the selection of a default device manager.
     * 
     * @param instance
     */
    public static void setInstance(AudioDeviceManager instance) {
        AudioDeviceFactory.instance = instance;
    }

    /**
     * Try to load JPortAudio or JavaSound devices.
     * 
     * @return A device supported on this platform.
     */
    public static AudioDeviceManager createAudioDeviceManager() {
        return createAudioDeviceManager(false);
    }

    /**
     * Try to load JPortAudio or JavaSound devices.
     * 
     * @param preferJavaSound if true then try to create a JavaSound manager before other types.
     * @return A device supported on this platform.
     */
    public static AudioDeviceManager createAudioDeviceManager(boolean preferJavaSound) {
        if (preferJavaSound) {
            tryJavaSound();
            tryJPortAudio();
        } else {
            tryJPortAudio();
            tryJavaSound();
        }
        return instance;
    }

    private static void tryJavaSound() {
        if (instance == null) {
            try {
                @SuppressWarnings("unchecked")
                Class<AudioDeviceManager> clazz = JavaTools.loadClass(
                        "com.jsyn.devices.javasound.JavaSoundAudioDevice", false);
                if (clazz != null) {
                    instance = clazz.newInstance();
                }
            } catch (Throwable e) {
                System.err.println("Could not load JavaSound device. " + e);
            }
        }
    }

    private static void tryJPortAudio() {
        if (instance == null) {
            try {
                if (JavaTools.loadClass("com.portaudio.PortAudio", false) != null) {
                    instance = (AudioDeviceManager) JavaTools.loadClass(
                            "com.jsyn.devices.jportaudio.JPortAudioDevice").newInstance();
                }

            } catch (Throwable e) {
                System.err.println("Could not load JPortAudio device. " + e);
            }
        }
    }

}
