/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitInputPort;

/**
 * Connect a unit generator to the input. Then start() recording. The signal will be written to a
 * WAV format file that can be read by other programs.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class WaveRecorder {
    private AudioStreamReader reader;
    private WaveFileWriter writer;
    private StreamingThread thread;
    private Synthesizer synth;
    private TransportModel transportModel = new TransportModel();
    private double maxRecordingTime;

    /**
     * Create a stereo 16-bit recorder.
     * 
     * @param synth
     * @param outputFile
     * @throws FileNotFoundException
     */
    public WaveRecorder(Synthesizer synth, File outputFile) throws FileNotFoundException {
        this(synth, outputFile, 2, 16);
    }

    public WaveRecorder(Synthesizer synth, File outputFile, int samplesPerFrame)
            throws FileNotFoundException {
        this(synth, outputFile, samplesPerFrame, 16);
    }

    /**
     * @param synth
     * @param outputFile
     * @param samplesPerFrame 1 for mono, 2 for stereo
     * @param bitsPerSample 16 or 24
     * @throws FileNotFoundException
     */
    public WaveRecorder(Synthesizer synth, File outputFile, int samplesPerFrame, int bitsPerSample)
            throws FileNotFoundException {
        this.synth = synth;
        reader = new AudioStreamReader(synth, samplesPerFrame);

        writer = new WaveFileWriter(outputFile);
        writer.setFrameRate(synth.getFrameRate());
        writer.setSamplesPerFrame(samplesPerFrame);
        writer.setBitsPerSample(bitsPerSample);
    }

    public UnitInputPort getInput() {
        return reader.getInput();
    }

    public void start() {
        stop();
        thread = new StreamingThread(reader, writer);
        thread.setTransportModel(transportModel);
        thread.setSamplesPerFrame(writer.getSamplesPerFrame());
        updateMaxRecordingTime();
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.requestStop();
            try {
                thread.join(500);
            } catch (InterruptedException e) {
            }
            thread = null;
        }
    }

    /** Close and disconnect any connected inputs. */
    public void close() throws IOException {
        stop();
        if (writer != null) {
            writer.close();
            writer = null;
        }
        if (reader != null) {
            reader.close();
            for (int i = 0; i < reader.getInput().getNumParts(); i++) {
                reader.getInput().disconnectAll(i);
            }
            reader = null;
        }
    }

    public void addTransportListener(TransportListener listener) {
        transportModel.addTransportListener(listener);
    }

    public void removeTransportListener(TransportListener listener) {
        transportModel.removeTransportListener(listener);
    }

    public void setMaxRecordingTime(double maxRecordingTime) {
        this.maxRecordingTime = maxRecordingTime;
        updateMaxRecordingTime();
    }

    private void updateMaxRecordingTime() {
        StreamingThread streamingThread = thread;
        if (streamingThread != null) {
            long maxFrames = (long) (maxRecordingTime * synth.getFrameRate());
            streamingThread.setMaxFrames(maxFrames);
        }
    }
}
