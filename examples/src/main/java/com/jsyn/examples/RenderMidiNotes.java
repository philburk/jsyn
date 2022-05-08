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

/**
 * Render MIDI messages to PCM in non-real-time as an array.
 * Then write it to a WAV file.
 */

package com.jsyn.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.instruments.DualOscillatorSynthVoice;
import com.jsyn.midi.MidiConstants;
import com.jsyn.midi.MidiSynthesizer;
import com.jsyn.util.AudioStreamReader;
import com.jsyn.util.MultiChannelSynthesizer;
import com.jsyn.util.VoiceDescription;
import com.jsyn.util.WaveFileWriter;

public class RenderMidiNotes {

    private static final int NUM_MIDI_CHANNELS = 16;
    private static final int VOICES_PER_MIDI_CHANNEL = 6;

    private Synthesizer mSynth;
    private int mNumChannels = 2; // stereo

    private MidiSynthesizer mMidiSynthesizer;
    private MultiChannelSynthesizer mMultiSynth;

    private AudioStreamReader mReader;
    private double[] buffer = new double[256 * mNumChannels];
    private ArrayList<Double> mSampleArray = new ArrayList<Double>();

    private void test() throws IOException, InterruptedException {
        // Create a context for the JSyn synthesizer.
        mSynth = JSyn.createSynthesizer();
        mSynth.setRealTime(false);

        // Setup a MIDI synthesizer and a MIDI parser.
        VoiceDescription voiceDescription = DualOscillatorSynthVoice.getVoiceDescription();
        mMultiSynth = new MultiChannelSynthesizer();
        final int startChannel = 0;
        mMultiSynth.setup(mSynth, startChannel, NUM_MIDI_CHANNELS, VOICES_PER_MIDI_CHANNEL, voiceDescription);
        mMidiSynthesizer = new MidiSynthesizer(mMultiSynth);

        // The read will read data from the audio engine into a FIFO.
        mReader = new AudioStreamReader(mSynth, mNumChannels);

        // Connect the MIDI synthesizer to the left and right reader channels.
        mMultiSynth.getOutput().connect(0, mReader.getInput(), 0);
        mMultiSynth.getOutput().connect(0, mReader.getInput(), 1);

        // Start synthesizer using stereo output at 48000 Hz.
        mSynth.start(48000);

        // Play a sequence of notes using MIDI byte commands.
        double now = mSynth.getCurrentTime();
        for (int i = 0; i<8; i++) {
            // Note On
            byte[] bar = {(byte) MidiConstants.NOTE_ON,
                    (byte) (60 + (2*i)), // pitch
                    100}; // velocity
            sendMidiMessage(bar);
            now += 0.2;
            renderUntil(now);

            // Turn the Note Off by setting velocity to zero
            bar[2] = 0;
            sendMidiMessage(bar);
            now += 0.2;
            renderUntil(now);
        }

        // Render at the end of the song to let the reverb die down.
        now += 0.3;
        renderUntil(now);

        // Write the rendered PCM data to a WAV file for listening.
        double[] data = mSampleArray.stream().mapToDouble(d -> d).toArray();
        writeWavFile(data);

        // Stop everything.
        mSynth.stop();

        System.out.println("Test finished.");
    }


    private void renderUntil(double time) throws InterruptedException
    {
        while (mSynth.getCurrentTime() < time) {
            // Just sleep for a millisecond so we do not overflow the reader.
            mSynth.sleepFor(0.001);
            int available = mReader.available();
            // System.out.println("available = " + available);
            while (available > 0) {
                int numSamplesToRead = Math.min(buffer.length, available);
                int numRead = mReader.read(buffer, 0, numSamplesToRead);
                for (int i = 0; i < numRead; i++) {
                    mSampleArray.add(buffer[i]);
                }
                available -= numRead;
                // System.out.println("numRead = " + numRead);
            }
        }
    }

    private void sendMidiMessage(byte[] bytes) {
        mMidiSynthesizer.onReceive(bytes, 0, bytes.length);
    }


    private void writeWavFile(double[] data) throws FileNotFoundException, IOException
    {
        File outFile = new File("test.wav");
        WaveFileWriter writer = new WaveFileWriter(outFile);
        writer.setFrameRate(mSynth.getFrameRate());
        writer.setSamplesPerFrame(mNumChannels);
        writer.write(data);
        writer.close();
        System.out.println("wrote " + outFile.getAbsolutePath());
    }

    public static void main(String[] args) {
        try {
            new RenderMidiNotes().test();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
