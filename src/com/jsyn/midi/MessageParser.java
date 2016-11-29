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
    private int[] parameterIndices = new int[MidiConstants.MAX_CHANNELS];
    private int[] parameterValues = new int[MidiConstants.MAX_CHANNELS];
    private int BIT_NON_RPM = 1 << 14;
    private int MASK_14BIT = (1 << 14) - 1;

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

            case MidiConstants.POLYPHONIC_AFTERTOUCH:
                polyphonicAftertouch(channel, message[1], message[2]);
                break;

            case MidiConstants.CHANNEL_PRESSURE:
                channelPressure(channel, message[1]);
                break;

            case MidiConstants.CONTROL_CHANGE:
                rawControlChange(channel, message[1], message[2]);
                break;

            case MidiConstants.PROGRAM_CHANGE:
                programChange(channel, message[1]);
                break;

            case MidiConstants.PITCH_BEND:
                int bend = (message[2] << 7) + message[1];
                pitchBend(channel, bend);
                break;
        }

    }

    public void rawControlChange(int channel, int index, int value) {
        int paramIndex;
        int paramValue;
        switch(index) {
            case MidiConstants.CONTROLLER_DATA_ENTRY:
                parameterValues[channel] = value << 7;
                fireParameterChange(channel);
                break;
            case MidiConstants.CONTROLLER_DATA_ENTRY_LSB:
                paramValue = parameterValues[channel] & ~0x7F;
                paramValue |= value;
                parameterValues[channel] = paramValue;
                fireParameterChange(channel);
                break;
            case MidiConstants.CONTROLLER_NRPN_LSB:
                paramIndex = parameterIndices[channel] & ~0x7F;
                paramIndex |= value | BIT_NON_RPM;
                parameterIndices[channel] = paramIndex;
                break;
            case MidiConstants.CONTROLLER_NRPN_MSB:
                parameterIndices[channel] = (value << 7) | BIT_NON_RPM;;
                break;
            case MidiConstants.CONTROLLER_RPN_LSB:
                paramIndex = parameterIndices[channel] & ~0x7F;
                paramIndex |= value;
                parameterIndices[channel] = paramIndex;
                break;
            case MidiConstants.CONTROLLER_RPN_MSB:
                parameterIndices[channel] = value << 7;
                break;
            default:
                controlChange(channel, index, value);
                break;

        }
    }

    private void fireParameterChange(int channel) {
        int paramIndex;
        paramIndex = parameterIndices[channel];
        if ((paramIndex & BIT_NON_RPM) == 0) {
            registeredParameter(channel, paramIndex, parameterValues[channel]);
        } else {
            nonRegisteredParameter(channel, paramIndex & MASK_14BIT, parameterValues[channel]);
        }
    }

    public void nonRegisteredParameter(int channel, int index14, int value14) {
    }

    public void registeredParameter(int channel, int index14, int value14) {
    }

    public void pitchBend(int channel, int bend) {
    }

    public void programChange(int channel, int program) {
    }

    public void polyphonicAftertouch(int channel, int pitch, int pressure) {
    }

    public void channelPressure(int channel, int pressure) {
    }

    public void controlChange(int channel, int index, int value) {
    }

    public void noteOn(int channel, int pitch, int velocity) {
    }

    // If a NOTE_ON with zero velocity is received then noteOff will be called.
    public void noteOff(int channel, int pitch, int velocity) {
    }
}
