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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.javasound.MidiDeviceTools;
import com.jsyn.instruments.SubtractiveSynthVoice;
import com.jsyn.midi.MessageParser;
import com.jsyn.midi.MidiConstants;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PowerOfTwo;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.util.VoiceAllocator;
import com.softsynth.shared.time.TimeStamp;

/**
 * Connect a USB MIDI Keyboard to the internal MIDI Synthesizer using JavaSound.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class UseMidiKeyboard {
    private static final int MAX_VOICES = 8;
    private Synthesizer synth;
    private VoiceAllocator allocator;
    private LineOut lineOut;
    private double vibratoRate = 5.0;
    private double vibratoDepth = 0.0;

    private UnitOscillator lfo;
    private PowerOfTwo powerOfTwo;
    private MessageParser messageParser;
    private SubtractiveSynthVoice[] voices;

    public static void main(String[] args) {
        UseMidiKeyboard app = new UseMidiKeyboard();
        try {
            app.test();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Write a Receiver to get the messages from a Transmitter.
    class CustomReceiver implements Receiver {
        @Override
        public void close() {
            System.out.print("Closed.");
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            byte[] bytes = message.getMessage();
            messageParser.parse(bytes);
        }
    }

    public int test() throws MidiUnavailableException, IOException, InterruptedException {
        setupSynth();

        messageParser = new MyParser();

        int result = 2;
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
        } else {
            System.out.println("Could not find a keyboard.");
        }
        return result;
    }

    class MyParser extends MessageParser {
        @Override
        public void controlChange(int channel, int index, int value) {
            // Mod Wheel
            if (index == 1) {
                vibratoDepth = 0.1 * value / 128.0;
                // System.out.println( "vibratoDepth = " + vibratoDepth );
                lfo.amplitude.set(vibratoDepth);
            }
            // 102 is the index of the first knob on my Axiom 25
            else if (index == 102) {
                final double bump = 0.95;
                if (value < 64) {
                    vibratoRate *= bump;
                } else {
                    vibratoRate *= 1.0 / bump;
                }
                System.out.println("vibratoRate = " + vibratoRate);
                lfo.frequency.set(vibratoRate);
            }

        }

        @Override
        public void noteOff(int channel, int noteNumber, int velocity) {
            allocator.noteOff(noteNumber, synth.createTimeStamp());
        }

        @Override
        public void noteOn(int channel, int noteNumber, int velocity) {
            double frequency = convertPitchToFrequency(noteNumber);
            double amplitude = velocity / (4 * 128.0);
            TimeStamp timeStamp = synth.createTimeStamp();
            allocator.noteOn(noteNumber, frequency, amplitude, timeStamp);
        }

        @Override
        public void pitchBend(int channel, int bend) {
            double fraction = (bend - MidiConstants.PITCH_BEND_CENTER)
                    / ((double) MidiConstants.PITCH_BEND_CENTER);
            System.out.println("bend = " + bend + ", fraction = " + fraction);
        }
    }

    /**
     * Calculate frequency in Hertz based on MIDI pitch. Middle C is 60.0. You can use fractional
     * pitches so 60.5 would give you a pitch half way between C and C#.
     */
    double convertPitchToFrequency(double pitch) {
        final double concertA = 440.0;
        return concertA * Math.pow(2.0, ((pitch - 69) * (1.0 / 12.0)));
    }

    private void setupSynth() {
        synth = JSyn.createSynthesizer();

        // Add an output.
        synth.add(lineOut = new LineOut());

        synth.add(powerOfTwo = new PowerOfTwo());
        synth.add(lfo = new SineOscillator());
        // Sums pitch modulation.
        lfo.output.connect(powerOfTwo.input);
        lfo.amplitude.set(vibratoDepth);
        lfo.frequency.set(vibratoRate);

        voices = new SubtractiveSynthVoice[MAX_VOICES];
        for (int i = 0; i < MAX_VOICES; i++) {
            SubtractiveSynthVoice voice = new SubtractiveSynthVoice();
            synth.add(voice);
            powerOfTwo.output.connect(voice.pitchModulation);
            voice.getOutput().connect(0, lineOut.input, 0);
            voice.getOutput().connect(0, lineOut.input, 1);
            voices[i] = voice;
        }
        allocator = new VoiceAllocator(voices);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();

        // Get synthesizer time in seconds.
        double timeNow = synth.getCurrentTime();

        // Advance to a near future time so we have a clean start.
        double time = timeNow + 0.5;

    }

}
