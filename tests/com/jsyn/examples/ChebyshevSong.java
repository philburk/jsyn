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

import java.awt.BorderLayout;

import javax.swing.JApplet;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.instruments.WaveShapingVoice;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.LineOut;
import com.jsyn.util.PseudoRandom;
import com.jsyn.util.VoiceAllocator;
import com.softsynth.math.AudioMath;
import com.softsynth.shared.time.TimeStamp;

/***************************************************************
 * Play notes using a WaveShapingVoice. Allocate the notes using a VoiceAllocator.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class ChebyshevSong extends JApplet implements Runnable {
    private static final long serialVersionUID = -7459137388629333223L;
    private Synthesizer synth;
    private Add mixer;
    private LineOut lineOut;
    private AudioScope scope;
    private volatile boolean go = false;
    private PseudoRandom pseudo = new PseudoRandom();
    private final static int MAX_VOICES = 8;
    private final static int MAX_NOTES = 5;
    private VoiceAllocator allocator;
    private final static int scale[] = {
            0, 2, 4, 7, 9
    }; // pentatonic scale

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        ChebyshevSong applet = new ChebyshevSong();
        JAppletFrame frame = new JAppletFrame("ChebyshevSong", applet);
        frame.setSize(640, 300);
        frame.setVisible(true);
        frame.test();
    }

    /*
     * Setup synthesis.
     */
    @Override
    public void start() {
        setLayout(new BorderLayout());

        synth = JSyn.createSynthesizer();

        // Use a submix so we can show it on the scope.
        synth.add(mixer = new Add());
        synth.add(lineOut = new LineOut());

        mixer.output.connect(0, lineOut.input, 0);
        mixer.output.connect(0, lineOut.input, 1);

        WaveShapingVoice[] voices = new WaveShapingVoice[MAX_VOICES];
        for (int i = 0; i < MAX_VOICES; i++) {
            WaveShapingVoice voice = new WaveShapingVoice();
            synth.add(voice);
            voice.usePreset(0);
            voice.getOutput().connect(mixer.inputA);
            voices[i] = voice;
        }
        allocator = new VoiceAllocator(voices);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        lineOut.start();

        // Use a scope to show the mixed output.
        scope = new AudioScope(synth);
        scope.addProbe(mixer.output);
        scope.setTriggerMode(AudioScope.TriggerMode.NORMAL);
        scope.getView().setControlsVisible(false);
        add(BorderLayout.CENTER, scope.getView());
        scope.start();

        /* Synchronize Java display. */
        getParent().validate();
        getToolkit().sync();

        // start thread that plays notes
        Thread thread = new Thread(this);
        go = true;
        thread.start();

    }

    @Override
    public void stop() {
        // tell song thread to finish
        go = false;
        removeAll();
        synth.stop();
    }

    double indexToFrequency(int index) {
        int octave = index / scale.length;
        int temp = index % scale.length;
        int pitch = scale[temp] + (12 * octave);
        return AudioMath.pitchToFrequency(pitch + 16);
    }

    private void noteOff(double time, int noteNumber) {
        allocator.noteOff(noteNumber, new TimeStamp(time));
    }

    private void noteOn(double time, int noteNumber) {
        double frequency = indexToFrequency(noteNumber);
        double amplitude = 0.1;
        TimeStamp timeStamp = new TimeStamp(time);
        allocator.noteOn(noteNumber, frequency, amplitude, timeStamp);
        allocator.setPort(noteNumber, "Range", 0.7, synth.createTimeStamp());
    }

    @Override
    public void run() {
        // always choose a new song based on time&date
        int savedSeed = (int) System.currentTimeMillis();
        // calculate tempo
        double duration = 0.2;
        // set time ahead of any system latency
        double advanceTime = 0.5;
        // time for next note to start
        double nextTime = synth.getCurrentTime() + advanceTime;
        // note is ON for half the duration
        double onTime = duration / 2;
        int beatIndex = 0;
        try {
            do {
                // on every measure, maybe repeat previous pattern
                if ((beatIndex & 7) == 0) {
                    if ((Math.random() < (1.0 / 2.0)))
                        pseudo.setSeed(savedSeed);
                    else if ((Math.random() < (1.0 / 2.0)))
                        savedSeed = pseudo.getSeed();
                }

                // Play a bunch of random notes in the scale.
                int numNotes = pseudo.choose(MAX_NOTES);
                for (int i = 0; i < numNotes; i++) {
                    int noteNumber = pseudo.choose(30);
                    noteOn(nextTime, noteNumber);
                    noteOff(nextTime + onTime, noteNumber);
                }

                nextTime += duration;
                beatIndex += 1;

                // wake up before we need to play note to cover system latency
                synth.sleepUntil(nextTime - advanceTime);
            } while (go);
        } catch (InterruptedException e) {
            System.err.println("Song exiting. " + e);
        }
    }
}
