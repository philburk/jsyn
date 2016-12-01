/*
 * Copyright 2016 Phil Burk, Mobileer Inc
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

import com.jsyn.instruments.DualOscillatorSynthVoice;
import com.jsyn.util.MultiChannelSynthesizer;

/**
 * Map MIDI messages into calls to a MultiChannelSynthesizer.
 * Handles CONTROLLER_MOD_WHEEL, TIMBRE, VOLUME and PAN.
 * Handles Bend Range RPN.
 *
 * <pre><code>
    voiceDescription = DualOscillatorSynthVoice.getVoiceDescription();
    multiSynth = new MultiChannelSynthesizer();
    final int startChannel = 0;
    multiSynth.setup(synth, startChannel, NUM_CHANNELS, VOICES_PER_CHANNEL, voiceDescription);
    midiSynthesizer = new MidiSynthesizer(multiSynth);
    // pass MIDI bytes
    midiSynthesizer.onReceive(bytes, 0, bytes.length);
    </code></pre>
 *
 * See the example UseMidiKeyboard.java
 *
 * @author Phil Burk (C) 2016 Mobileer Inc
 */
public class MidiSynthesizer extends MessageParser {

    private MultiChannelSynthesizer multiSynth;

    public MidiSynthesizer(MultiChannelSynthesizer multiSynth) {
        this.multiSynth = multiSynth;
    }

    @Override
    public void controlChange(int channel, int index, int value) {
        //System.out.println("controlChange(" + channel + ", " + index + ", " + value + ")");
        double normalized = value * (1.0 / 127.0);
        switch (index) {
            case MidiConstants.CONTROLLER_MOD_WHEEL:
                double vibratoDepth = 0.1 * normalized;
                System.out.println( "vibratoDepth = " + vibratoDepth );
                multiSynth.setVibratoDepth(channel, vibratoDepth);
                break;
            case MidiConstants.CONTROLLER_TIMBRE:
                multiSynth.setTimbre(channel, normalized);
                break;
            case MidiConstants.CONTROLLER_VOLUME:
                multiSynth.setVolume(channel, normalized);
                break;
            case MidiConstants.CONTROLLER_PAN:
                // convert to -1 to +1 range
                multiSynth.setPan(channel, (normalized * 2.0) - 1.0);
                break;
        }
    }

    @Override
    public void registeredParameter(int channel, int index14, int value14) {
        switch(index14) {
            case MidiConstants.RPN_BEND_RANGE:
                int semitones = value14 >> 7;
                int cents = value14 & 0x7F;
                double bendRange = semitones + (cents * 0.01);
                multiSynth.setBendRange(channel, bendRange);
                break;
            default:
                break;
        }
    }

    @Override
    public void programChange(int channel, int program) {
        multiSynth.programChange(channel, program);
    }

    @Override
    public void channelPressure(int channel, int value) {
        double normalized = value * (1.0 / 127.0);
        multiSynth.setPressure(channel, normalized);
    }

    @Override
    public void noteOff(int channel, int noteNumber, int velocity) {
        multiSynth.noteOff(channel, noteNumber, velocity);
    }

    @Override
    public void noteOn(int channel, int noteNumber, int velocity) {
        multiSynth.noteOn(channel, noteNumber, velocity);
    }

    @Override
    public void pitchBend(int channel, int bend) {
        double offset = (bend - MidiConstants.PITCH_BEND_CENTER)
                * (1.0 / (MidiConstants.PITCH_BEND_CENTER));
        multiSynth.setPitchBend(channel, offset);
    }

    public void onReceive(byte[] bytes, int i, int length) {
        parse(bytes); // TODO
    }

}
