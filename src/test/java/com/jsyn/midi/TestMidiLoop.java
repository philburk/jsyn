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

package com.jsyn.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import com.jsyn.devices.javasound.MidiDeviceTools;
import org.junit.jupiter.api.Test;

/**
 * Connect a USB MIDI Keyboard to the internal MIDI Synthesizer using JavaSound.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class TestMidiLoop {

    @Test
    private void midiLoop() {
        try {
            for (int result = 0, i = 0; i < 3 && result == 0; i++) {
                result = test();
            }
        } catch (MidiUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Write a Receiver to get the messages from a Transmitter.
    static class CustomReceiver implements Receiver {
        @Override
        public void close() {
            System.out.print("Receiver.close() was called.");
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            byte[] bytes = message.getMessage();
            System.out.println("Got " + bytes.length + " bytes.");
        }
    }

    public int test() throws MidiUnavailableException, InterruptedException {

        int result = -1;
        MidiDevice keyboard = MidiDeviceTools.findKeyboard();
        Receiver receiver = new CustomReceiver();
        // Just use default synthesizer.
        if (keyboard != null) {
            // If you forget to open them you will hear no sound.
            keyboard.open();
            // Put the receiver in the transmitter.
            // This gives fairly low latency playing.
            keyboard.getTransmitter().setReceiver(receiver);
            System.out.println("Play MIDI keyboard: " + keyboard.getDeviceInfo().getDescription());
            result = 0;
            Thread.sleep(4000);
            System.out.println("Close the keyboard. It may not work after this according to the docs!");
            keyboard.close();
        } else {
            System.out.println("Could not find a keyboard.");
        }
        return result;
    }


}
