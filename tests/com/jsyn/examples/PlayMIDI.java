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

package com.jsyn.examples;

import java.io.IOException;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.javasound.MidiDeviceTools;
import com.jsyn.instruments.DualOscillatorSynthVoice;
import com.jsyn.instruments.SubtractiveSynthVoice;
import com.jsyn.midi.MessageParser;
import com.jsyn.midi.MidiConstants;
import com.jsyn.midi.MidiSynthesizer;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PowerOfTwo;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.MultiChannelSynthesizer;
import com.jsyn.util.VoiceAllocator;
import com.jsyn.util.VoiceDescription;
import com.softsynth.math.AudioMath;
import com.softsynth.shared.time.TimeStamp;

/**
 * Send MIDI messages to JSyn based MIDI synthesizer.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PlayMIDI {
    private static final int NUM_CHANNELS = 16;
    private static final int VOICES_PER_CHANNEL = 6;
    private Synthesizer synth;
    private MidiSynthesizer midiSynthesizer;
    private LineOut lineOut;

    private VoiceDescription voiceDescription;
    private MultiChannelSynthesizer multiSynth;

    public static void main(String[] args) {
        PlayMIDI app = new PlayMIDI();
        try {
            VoiceDescription description = DualOscillatorSynthVoice.getVoiceDescription();
            app.test(description);
            System.out.println("Test complete");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void sendMidiMessage(byte[] bytes) {
        midiSynthesizer.onReceive(bytes, 0, bytes.length);
    }

    public void sendNoteOff(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.NOTE_OFF + channel, pitch, velocity);
    }

    public void sendNoteOn(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.NOTE_ON + channel, pitch, velocity);
    }

    public void sendControlChange(int channel, int index, int value) {
        midiCommand(MidiConstants.CONTROL_CHANGE + channel, index, value);
    }

    /**
     * @param channel
     * @param program starts at zero
     */
    private void sendProgramChange(int channel, int program) {
        midiCommand(MidiConstants.PROGRAM_CHANGE + channel, program);

    }

    /**
     * Send either RPN or NRPN.
     */
    public void sendParameter(int channel, int index14, int value14, int controllerXPN) {
        int indexLsb = index14 & 0x07F;
        int indexMsb = (index14 >> 7) & 0x07F;
        int valueLsb = value14 & 0x07F;
        int valueMsb = (value14 >> 7) & 0x07F;
        sendControlChange(channel, controllerXPN + 1, indexMsb);
        sendControlChange(channel, controllerXPN, indexLsb);
        sendControlChange(channel, MidiConstants.CONTROLLER_DATA_ENTRY, valueMsb);
        sendControlChange(channel, MidiConstants.CONTROLLER_DATA_ENTRY_LSB, valueLsb);
        sendControlChange(channel, controllerXPN + 1, 0x7F); // NULL RPN index
        sendControlChange(channel, controllerXPN, 0x7F); // to deactivate RPN
    }

    public void sendRPN(int channel, int index14, int value14) {
        sendParameter(channel, index14, value14, MidiConstants.CONTROLLER_RPN_LSB);
    }

    public void sendNRPN(int channel, int index14, int value14) {
        sendParameter(channel, index14, value14, MidiConstants.CONTROLLER_NRPN_LSB);
    }

    private void midiCommand(int status, int data1, int data2) {
        byte[] buffer = new byte[3];
        buffer[0] = (byte) status;
        buffer[1] = (byte) data1;
        buffer[2] = (byte) data2;
        sendMidiMessage(buffer);
    }

    private void midiCommand(int status, int data1) {
        byte[] buffer = new byte[2];
        buffer[0] = (byte) status;
        buffer[1] = (byte) data1;
        sendMidiMessage(buffer);
    }

    public int test(VoiceDescription description) throws IOException, InterruptedException {
        setupSynth(description);

        //playOctaveUsingBend();
        playSameNotesBent();

        // Setup all the channels.
        int maxChannels = 8;
        for (int channel = 0; channel < maxChannels; channel++) {
            int program = channel;
            sendProgramChange(channel, program);
        }
        playNotePerChannel(maxChannels);

        return 0;
    }

    private void playOctaveUsingBend() throws InterruptedException {
        sendProgramChange(0, 0);
        float range0 = 12.0f;
        sendPitchBendRange(0, range0);
        for(int i = 0; i < 13; i++) {
            System.out.println("Bend to pitch " + i);
            sendPitchBend(0, i / range0);
            sendNoteOn(0, 60, 100);
            synth.sleepFor(0.5);
            sendNoteOff(0, 60, 100);
            synth.sleepFor(0.5);
        }
    }

    private void playSameNotesBent() throws InterruptedException {
        sendProgramChange(0, 0);
        sendProgramChange(1, 0);
        float range0 = 2.3f;
        float range1 = 6.8f;
        sendPitchBendRange(0, range0);
        sendPitchBendRange(1, range1);
        sendPitchBend(0, 0.0f / range0); // bend by 0 semitones
        sendPitchBend(1, 1.0f / range1); // bend by 1 semitones

        System.out.println("These two notes should play at the same pitch.");
        sendNoteOn(0, 61, 100);
        synth.sleepFor(0.5);
        sendNoteOff(0, 61, 100);

        sendNoteOn(1, 60, 100);
        synth.sleepFor(0.5);
        sendNoteOff(1, 60, 100);

        synth.sleepFor(2.0);
        System.out.println("------ done ---------------");
    }

    /**
     *
     * @param channel
     * @param normalizedBend between -1 and +1
     */
    private void sendPitchBend(int channel, float normalizedBend) {
            final int BEND_MIN = 0x0000;
            final int BEND_CENTER = 0x2000;
            final int BEND_MAX = 0x3FFF;
            int bend = BEND_CENTER + (int)(BEND_CENTER * normalizedBend);
            if (bend < BEND_MIN) bend = BEND_MIN;
            else if (bend > BEND_MAX) bend = BEND_MAX;
            int lsb = bend & 0x07F;
            int msb = (bend >> 7) & 0x07F;
            midiCommand(MidiConstants.PITCH_BEND + channel, lsb, msb);
    }

    private void sendPitchBendRange(int channel, float range0) {
        int semitones = (int)range0;
        int cents = (int) (100 * (range0 - semitones));
        int value = (semitones << 7) + cents;
        sendRPN(channel, MidiConstants.RPN_BEND_RANGE, value);
    }

    private void playNotePerChannel(int maxChannels) throws InterruptedException {
        // Play notes on those channels.
        for (int channel = 0; channel < maxChannels; channel++) {
            sendNoteOn(channel, 60 + channel, 100);
            synth.sleepFor(0.5);
            sendNoteOff(channel, 60 + channel, 100);
            synth.sleepFor(0.5);
        }
    }

    private void setupSynth(VoiceDescription description) {
        synth = JSyn.createSynthesizer();

        // Add an output.
        synth.add(lineOut = new LineOut());

        voiceDescription = description;
        multiSynth = new MultiChannelSynthesizer();
        final int startChannel = 0;
        multiSynth.setup(synth, startChannel, NUM_CHANNELS, VOICES_PER_CHANNEL, voiceDescription);
        midiSynthesizer = new MidiSynthesizer(multiSynth);

        multiSynth.getOutput().connect(0,lineOut.input, 0);
        multiSynth.getOutput().connect(1,lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        lineOut.start();
    }

}
