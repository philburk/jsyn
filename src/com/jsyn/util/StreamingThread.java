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

import java.io.IOException;

import com.jsyn.io.AudioInputStream;
import com.jsyn.io.AudioOutputStream;

/**
 * Read from an AudioInputStream and write to an AudioOutputStream as a background thread.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class StreamingThread extends Thread {
    private AudioInputStream inputStream;
    private AudioOutputStream outputStream;
    private int framesPerBuffer = 1024;
    private volatile boolean go = true;
    private TransportModel transportModel;
    private long framePosition;
    private long maxFrames;
    private int samplesPerFrame = 1;

    public StreamingThread(AudioInputStream inputStream, AudioOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        double[] buffer = new double[framesPerBuffer * samplesPerFrame];
        try {
            transportModel.firePositionChanged(framePosition);
            transportModel.fireStateChanged(TransportModel.STATE_RUNNING);
            int framesToRead = geteFramesToRead(buffer);
            while (go && (framesToRead > 0)) {
                int samplesToRead = framesToRead * samplesPerFrame;
                while (samplesToRead > 0) {
                    int samplesRead = inputStream.read(buffer, 0, samplesToRead);
                    outputStream.write(buffer, 0, samplesRead);
                    samplesToRead -= samplesRead;
                }
                framePosition += framesToRead;
                transportModel.firePositionChanged(framePosition);
                framesToRead = geteFramesToRead(buffer);
            }
            transportModel.fireStateChanged(TransportModel.STATE_STOPPED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int geteFramesToRead(double[] buffer) {
        if (maxFrames > 0) {
            long numToRead = maxFrames - framePosition;
            if (numToRead < 0) {
                return 0;
            } else if (numToRead > framesPerBuffer) {
                numToRead = framesPerBuffer;
            }
            return (int) numToRead;
        } else {
            return framesPerBuffer;
        }
    }

    public int getFramesPerBuffer() {
        return framesPerBuffer;
    }

    /**
     * Only call this before the thread has started.
     * 
     * @param framesPerBuffer
     */
    public void setFramesPerBuffer(int framesPerBuffer) {
        this.framesPerBuffer = framesPerBuffer;
    }

    public void requestStop() {
        go = false;
    }

    public TransportModel getTransportModel() {
        return transportModel;
    }

    public void setTransportModel(TransportModel transportModel) {
        this.transportModel = transportModel;
    }

    /**
     * @param maxFrames
     */
    public void setMaxFrames(long maxFrames) {
        this.maxFrames = maxFrames;
    }

    public int getSamplesPerFrame() {
        return samplesPerFrame;
    }

    public void setSamplesPerFrame(int samplesPerFrame) {
        this.samplesPerFrame = samplesPerFrame;
    }
}
