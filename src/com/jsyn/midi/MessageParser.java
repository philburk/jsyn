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

/**
 * Parse the message and call the appropriate method to handle it.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class MessageParser {
    public void parse(byte[] message) {
        int status = message[0];
        int command = status & 0xF0;
        int channel = status & 0x0F;

        switch (command) {
            case MidiConstants.NOTE_ON:
                int velocity = message[2];
                if (velocity == 0) {
                    noteOff(channel, message[1], velocity);
                } else {
                    noteOn(channel, message[1], velocity);
                }
                break;

            case MidiConstants.NOTE_OFF:
                noteOff(channel, message[1], message[2]);
                break;

            case MidiConstants.CONTROL_CHANGE:
                controlChange(channel, message[1], message[2]);
                break;

            case MidiConstants.PITCH_BEND:
                int bend = (((message[2]) & 0x007F) << 7) + ((message[1]) & 0x007F);
                pitchBend(channel, bend);
                break;
        }

    }

    public void pitchBend(int channel, int bend) {
    }

    public void controlChange(int channel, int index, int value) {
    }

    public void noteOn(int channel, int pitch, int velocity) {
    }

    public void noteOff(int channel, int pitch, int velocity) {
    }
}
