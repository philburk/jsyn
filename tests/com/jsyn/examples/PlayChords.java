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

package com.jsyn.examples;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.instruments.SubtractiveSynthVoice;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.VoiceAllocator;
import com.softsynth.math.AudioMath;
import com.softsynth.shared.time.TimeStamp;

/**
 * Play chords and melody using the VoiceAllocator.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class PlayChords {
    private static final int MAX_VOICES = 8;
    private Synthesizer synth;
    private VoiceAllocator allocator;
    private LineOut lineOut;
    /** Number of seconds to generate music in advance of presentation-time. */
    private double advance = 0.2;
    private double secondsPerBeat = 0.6;
    // on time over note duration
    private double dutyCycle = 0.8;
    private double measure = secondsPerBeat * 4.0;
    private UnitVoice[] voices;

    private void test() {
        synth = JSyn.createSynthesizer();

        // Add an output.
        synth.add(lineOut = new LineOut());

        voices = new UnitVoice[MAX_VOICES];
        for (int i = 0; i < MAX_VOICES; i++) {
            SubtractiveSynthVoice voice = new SubtractiveSynthVoice();
            synth.add(voice);
            voice.getOutput().connect(0, lineOut.input, 0);
            voice.getOutput().connect(0, lineOut.input, 1);
            voices[i] = voice;
        }
        allocator = new VoiceAllocator(voices);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the
        // voices.
        lineOut.start();

        // Get synthesizer time in seconds.
        double timeNow = synth.getCurrentTime();

        // Advance to a near future time so we have a clean start.
        double time = timeNow + 1.0;

        try {
            int tonic = 60 - 12;
            for (int i = 0; i < 4; i++) {
                playMajorMeasure1(time, tonic);
                time += measure;
                catchUp(time);
                playMajorMeasure1(time, tonic + 4);
                time += measure;
                catchUp(time);
                playMajorMeasure1(time, tonic + 7);
                time += measure;
                catchUp(time);
                playMinorMeasure1(time, tonic + 2); // minor chord
                time += measure;
                catchUp(time);
            }
            time += secondsPerBeat;
            catchUp(time);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop everything.
        synth.stop();
    }

    private void playMinorMeasure1(double time, int base) throws InterruptedException {
        int p1 = base;
        int p2 = base + 3;
        int p3 = base + 7;
        playChord1(time, p1, p2, p3);
        playNoodle1(time, p1 + 24, p2 + 24, p3 + 24);
    }

    private void playMajorMeasure1(double time, int base) throws InterruptedException {
        int p1 = base;
        int p2 = base + 4;
        int p3 = base + 7;
        playChord1(time, p1, p2, p3);
        playNoodle1(time, p1 + 24, p2 + 24, p3 + 24);
    }

    private void playNoodle1(double time, int p1, int p2, int p3) {
        double secondsPerNote = secondsPerBeat * 0.5;
        for (int i = 0; i < 8; i++) {
            int p = pickFromThree(p1, p2, p3);
            noteOn(time, p);
            noteOff(time + dutyCycle * secondsPerNote, p);
            time += secondsPerNote;
        }
    }

    private int pickFromThree(int p1, int p2, int p3) {
        int r = (int) (Math.random() * 3.0);
        if (r < 1)
            return p1;
        else if (r < 2)
            return p2;
        else
            return p3;
    }

    private void playChord1(double time, int p1, int p2, int p3) throws InterruptedException {
        double dur = dutyCycle * secondsPerBeat;
        playTriad(time, dur, p1, p2, p3);
        time += secondsPerBeat;
        playTriad(time, dur, p1, p2, p3);
        time += secondsPerBeat;
        playTriad(time, dur * 0.25, p1, p2, p3);
        time += secondsPerBeat * 0.25;
        playTriad(time, dur * 0.25, p1, p2, p3);
        time += secondsPerBeat * 0.75;
        playTriad(time, dur, p1, p2, p3);
        time += secondsPerBeat;
    }

    private void playTriad(double time, double dur, int p1, int p2, int p3)
            throws InterruptedException {
        noteOn(time, p1);
        noteOn(time, p2);
        noteOn(time, p3);
        double offTime = time + dur;
        noteOff(offTime, p1);
        noteOff(offTime, p2);
        noteOff(offTime, p3);
    }

    private void catchUp(double time) throws InterruptedException {
        synth.sleepUntil(time - advance);
    }

    private void noteOff(double time, int noteNumber) {
        allocator.noteOff(noteNumber, new TimeStamp(time));
    }

    private void noteOn(double time, int noteNumber) {
        double frequency = AudioMath.pitchToFrequency(noteNumber);
        double amplitude = 0.2;
        TimeStamp timeStamp = new TimeStamp(time);
        allocator.noteOn(noteNumber, frequency, amplitude, timeStamp);
    }

    public static void main(String[] args) {
        new PlayChords().test();
    }
}
