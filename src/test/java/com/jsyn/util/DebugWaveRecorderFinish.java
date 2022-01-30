/*
 * Copyright 2022 Phil Burk, Mobileer Inc
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

package com.jsyn.util;

import com.jsyn.util.WaveRecorder;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.instruments.DualOscillatorSynthVoice;
import com.softsynth.shared.time.TimeStamp;

/**
 * Debug the thread not finishing.
 * Contributed by Zoran Stojanović on GitHub.
 */
public class DebugWaveRecorderFinish {
    public static void main(String[] args) throws Exception {
        System.out.println("Started!");
        Synthesizer synth = JSyn.createSynthesizer();
        synth.setRealTime(false);
        DualOscillatorSynthVoice voice = new DualOscillatorSynthVoice();
        synth.add(voice);
        WaveRecorder recorder = new WaveRecorder(synth, new java.io.File("test.wav"), 1);
        voice.getOutput().connect(0, recorder.getInput(), 0);
        synth.start();
        recorder.start();

        voice.noteOn(440, 1, new TimeStamp(0));
        voice.noteOff(new TimeStamp(3));
        synth.sleepUntil(4.0);

        recorder.stop();
        recorder.close();
        synth.stop();
        System.out.println("Finished!");
    }
}
