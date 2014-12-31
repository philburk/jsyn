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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

public class MidiDeviceTools {
    /** Print the available MIDI Devices. */
    public static void listDevices() {
        // Ask the MidiSystem what is available.
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        // Print info about each device.
        for (MidiDevice.Info info : infos) {
            System.out.println("MIDI Info: " + info.getDescription() + ", " + info.getName() + ", "
                    + info.getVendor() + ", " + info.getVersion());
            // Get the device for more information.
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                System.out.println("   Device: " + ", #recv = " + device.getMaxReceivers()
                        + ", #xmit = " + device.getMaxTransmitters() + ", open = "
                        + device.isOpen() + ", " + device);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    /** Find a MIDI transmitter that contains text in the name. */
    public static MidiDevice findKeyboard(String text) {
        MidiDevice keyboard = null;
        // Ask the MidiSystem what is available.
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        // Print info about each device.
        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                // Hardware devices are not Synthesizers or Sequencers.
                if (!(device instanceof Synthesizer) && !(device instanceof Sequencer)) {
                    // Is this a transmitter?
                    // Might be -1 if unlimited.
                    if (device.getMaxTransmitters() != 0) {
                        if ((text == null)
                                || (info.getDescription().toLowerCase()
                                        .contains(text.toLowerCase()))) {
                            keyboard = device;
                            System.out.println("Chose: " + info.getDescription());
                            break;
                        }
                    }
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        return keyboard;
    }

    public static MidiDevice findKeyboard() {
        return findKeyboard(null);
    }

}
