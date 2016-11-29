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
 * Constants that define the MIDI standard.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class MidiConstants {

    public static final int MAX_CHANNELS = 16;
    // Basic commands.
    public static final int NOTE_OFF = 0x80;
    public static final int NOTE_ON = 0x90;
    public static final int POLYPHONIC_AFTERTOUCH = 0xA0;
    public static final int CONTROL_CHANGE = 0xB0;
    public static final int PROGRAM_CHANGE = 0xC0;
    public static final int CHANNEL_AFTERTOUCH = 0xD0;
    public static final int CHANNEL_PRESSURE = CHANNEL_AFTERTOUCH;
    public static final int PITCH_BEND = 0xE0;
    public static final int SYSTEM_COMMON = 0xF0;

    public static final int PITCH_BEND_CENTER = 0x2000;

    public static final int CONTROLLER_BANK_SELECT = 0;
    public static final int CONTROLLER_MOD_WHEEL = 1;
    public static final int CONTROLLER_BREATH = 2;
    public static final int CONTROLLER_DATA_ENTRY = 6;
    public static final int CONTROLLER_VOLUME = 7;
    public static final int CONTROLLER_PAN = 10;

    public static final int CONTROLLER_LSB_OFFSET = 32;
    public static final int CONTROLLER_DATA_ENTRY_LSB = CONTROLLER_DATA_ENTRY + CONTROLLER_LSB_OFFSET;

    public static final int CONTROLLER_TIMBRE = 74; // Often used by MPE for Y axis control.

    public static final int CONTROLLER_DATA_INCREMENT = 96;
    public static final int CONTROLLER_DATA_DECREMENT = 97;
    public static final int CONTROLLER_NRPN_LSB = 98;
    public static final int CONTROLLER_NRPN_MSB = 99;
    public static final int CONTROLLER_RPN_LSB = 100;
    public static final int CONTROLLER_RPN_MSB = 101;

    public static final int RPN_BEND_RANGE = 0;
    public static final int RPN_FINE_TUNING = 1;

    public static final String PITCH_NAMES[] = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    /**
     * Calculate frequency in Hertz based on MIDI pitch. Middle C is 60.0. You can use fractional
     * pitches so 60.5 would give you a pitch half way between C and C#.
     */
    static final double CONCERT_A_FREQUENCY = 440.0;
    static final double CONCERT_A_PITCH = 69.0;

    public static double convertPitchToFrequency(double pitch) {
        return CONCERT_A_FREQUENCY * Math.pow(2.0, ((pitch - CONCERT_A_PITCH) / 12.0));
    }

    /**
     * Calculate MIDI pitch based on frequency in Hertz. Middle C is 60.0.
     */
    public static double convertFrequencyToPitch(double frequency) {
        return CONCERT_A_PITCH + (12 * Math.log(frequency / CONCERT_A_FREQUENCY) / Math.log(2.0));
    }

}
