/*
 * Copyright 2012 Phil Burk, Mobileer Inc
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

package com.jsyn.apps;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.swing.JApplet;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.javasound.MidiDeviceTools;
import com.jsyn.instruments.JSynInstrumentLibrary;
import com.jsyn.midi.MessageParser;
import com.jsyn.swing.InstrumentBrowser;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.swing.PresetSelectionListener;
import com.jsyn.swing.SoundTweaker;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.UnitSource;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.PolyphonicInstrument;
import com.jsyn.util.VoiceDescription;
import com.softsynth.math.AudioMath;
import com.softsynth.shared.time.TimeStamp;

/**
 * Let the user select an instrument using the InstrumentBrowser and play
 * them using the ASCII keyboard or with MIDI.
 * Sound parameters can be tweaked using faders.
 *
 * @author Phil Burk (C) 2012 Mobileer Inc
 */
public class InstrumentTester extends JApplet {
    private static final long serialVersionUID = -2704222221111608377L;
    private Synthesizer synth;
    private LineOut lineOut;
    private SoundTweaker tweaker;
    protected PolyphonicInstrument instrument;
    private MyParser messageParser;

    class MyParser extends MessageParser {

        @Override
        public void controlChange(int channel, int index, int value) {
        }

        @Override
        public void noteOff(int channel, int noteNumber, int velocity) {
            instrument.noteOff(noteNumber, synth.createTimeStamp());
        }

        @Override
        public void noteOn(int channel, int noteNumber, int velocity) {
            double frequency = AudioMath.pitchToFrequency(noteNumber);
            double amplitude = velocity / (4 * 128.0);
            TimeStamp timeStamp = synth.createTimeStamp();
            instrument.noteOn(noteNumber, frequency, amplitude, timeStamp);
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

    public int setupMidiKeyboard() throws MidiUnavailableException, IOException, InterruptedException {
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

    @Override
    public void init() {
        setLayout(new BorderLayout());

        synth = JSyn.createSynthesizer();
        synth.add(lineOut = new LineOut());

        InstrumentBrowser browser = new InstrumentBrowser(new JSynInstrumentLibrary());
        browser.addPresetSelectionListener(new PresetSelectionListener() {

            @Override
            public void presetSelected(VoiceDescription voiceDescription, int presetIndex) {
                UnitVoice[] voices = new UnitVoice[8];
                for (int i = 0; i < voices.length; i++) {
                    voices[i] = voiceDescription.createUnitVoice();
                }
                instrument = new PolyphonicInstrument(voices);
                synth.add(instrument);
                instrument.usePreset(presetIndex, synth.createTimeStamp());
                String title = voiceDescription.getVoiceClassName() + ": "
                        + voiceDescription.getPresetNames()[presetIndex];
                useSource(instrument, title);
            }
        });
        add(browser, BorderLayout.NORTH);

        try {
            setupMidiKeyboard();
        } catch (MidiUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        validate();
    }

    private void useSource(UnitSource voice, String title) {

        lineOut.input.disconnectAll(0);
        lineOut.input.disconnectAll(1);

        // Connect the source to both left and right output.
        voice.getOutput().connect(0, lineOut.input, 0);
        voice.getOutput().connect(0, lineOut.input, 1);

        if (tweaker != null) {
            remove(tweaker);
        }
        try {
            if (synth.isRunning()) {
                synth.sleepFor(0.1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tweaker = new SoundTweaker(synth, title, voice);
        add(tweaker, BorderLayout.CENTER);
        validate();
    }

    @Override
    public void start() {
        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();
    }

    @Override
    public void stop() {
        synth.stop();
    }

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        InstrumentTester applet = new InstrumentTester();
        JAppletFrame frame = new JAppletFrame("InstrumentTester", applet);
        frame.setSize(600, 800);
        frame.setVisible(true);
        frame.test();
    }

}
