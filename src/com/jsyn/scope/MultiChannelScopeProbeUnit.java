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

package com.jsyn.scope;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.scope.AudioScope.TriggerMode;
import com.jsyn.unitgen.UnitGenerator;
import com.softsynth.shared.time.ScheduledCommand;

/**
 * Multi-channel scope probe with an independent trigger input.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class MultiChannelScopeProbeUnit extends UnitGenerator {
    // Signal that is captured.
    public UnitInputPort input;
    // Signal that triggers the probe.
    public UnitInputPort trigger;

    // I am using ints instead of an enum for performance reasons.
    private static final int STATE_IDLE = 0;
    private static final int STATE_ARMED = 1;
    private static final int STATE_READY = 2;
    private static final int STATE_TRIGGERED = 3;
    private int state = STATE_IDLE;

    private int numChannels;
    private double[][] inputValues;
    private static final int FRAMES_PER_BUFFER = 4096; // must be power of two
    private static final int FRAMES_PER_BUFFER_MASK = FRAMES_PER_BUFFER - 1;
    private Runnable callback;

    private TriggerModel triggerModel;
    private int autoCountdown;
    private int countdown;
    private int postTriggerSize = 512;
    SignalBuffer captureBuffer;
    SignalBuffer displayBuffer;

    // Use double buffers. One for capture, one for display.
    class SignalBuffer {
        float[][] buffers;
        private int writeCursor;
        private int triggerIndex;
        private int framesCaptured;

        SignalBuffer(int numChannels) {
            buffers = new float[numChannels][];
            for (int j = 0; j < numChannels; j++) {
                buffers[j] = new float[FRAMES_PER_BUFFER];
            }
        }

        void reset() {
            writeCursor = 0;
            triggerIndex = 0;
            framesCaptured = 0;
        }

        public void saveChannelValue(int j, float value) {
            buffers[j][writeCursor] = value;
        }

        public void markTrigger() {
            triggerIndex = writeCursor;
        }

        public void bumpCursor() {
            writeCursor = (writeCursor + 1) & FRAMES_PER_BUFFER_MASK;
            if (writeCursor >= FRAMES_PER_BUFFER) {
                writeCursor = 0;
            }
            if (framesCaptured < FRAMES_PER_BUFFER) {
                framesCaptured += 1;
            }
        }

        private int convertInternalToExternalIndex(int internalIndex) {
            if (framesCaptured < FRAMES_PER_BUFFER) {
                return internalIndex;
            } else {
                return (internalIndex - writeCursor) & (FRAMES_PER_BUFFER_MASK);
            }
        }

        private int convertExternalToInternalIndex(int externalIndex) {
            if (framesCaptured < FRAMES_PER_BUFFER) {
                return externalIndex;
            } else {
                return (externalIndex + writeCursor) & (FRAMES_PER_BUFFER_MASK);
            }
        }

        public int getTriggerIndex() {
            return convertInternalToExternalIndex(triggerIndex);
        }

        public int getFramesCaptured() {
            return framesCaptured;
        }

        public float getSample(int bufferIndex, int sampleIndex) {
            int index = convertExternalToInternalIndex(sampleIndex);
            return buffers[bufferIndex][index];
        }
    }

    public MultiChannelScopeProbeUnit(int numChannels, TriggerModel triggerModel) {
        this.numChannels = numChannels;
        captureBuffer = new SignalBuffer(numChannels);
        displayBuffer = new SignalBuffer(numChannels);
        this.triggerModel = triggerModel;
        addPort(trigger = new UnitInputPort(numChannels, "Trigger"));
        addPort(input = new UnitInputPort(numChannels, "Input"));
        inputValues = new double[numChannels][];
    }

    private synchronized void switchBuffers() {
        SignalBuffer temp = captureBuffer;
        captureBuffer = displayBuffer;
        displayBuffer = temp;
    }

    private void internalArm(Runnable callback) {
        this.callback = callback;
        state = STATE_ARMED;
        captureBuffer.reset();
    }

    class ScheduledArm implements ScheduledCommand {
        private Runnable callback;

        ScheduledArm(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            internalArm(this.callback);
        }
    }

    /** Arm the probe at a future time. */
    public void arm(double time, Runnable callback) {
        ScheduledArm command = new ScheduledArm(callback);
        getSynthesisEngine().scheduleCommand(time, command);
    }

    @Override
    public void generate(int start, int limit) {
        if (state != STATE_IDLE) {
            TriggerMode triggerMode = triggerModel.getMode();
            double triggerLevel = triggerModel.getTriggerLevel();
            double[] triggerValues = trigger.getValues();

            for (int j = 0; j < numChannels; j++) {
                inputValues[j] = input.getValues(j);
            }

            for (int i = start; i < limit; i++) {
                // Capture one sample from each channel.
                for (int j = 0; j < numChannels; j++) {
                    captureBuffer.saveChannelValue(j, (float) inputValues[j][i]);
                }
                captureBuffer.bumpCursor();

                switch (state) {
                    case STATE_ARMED:
                        if (triggerValues[i] <= triggerLevel) {
                            state = STATE_READY;
                            autoCountdown = 44100;
                        }
                        break;

                    case STATE_READY: {
                        boolean triggered = false;
                        if (triggerValues[i] > triggerLevel) {
                            triggered = true;
                        } else if (triggerMode.equals(TriggerMode.AUTO)) {
                            if (--autoCountdown == 0) {
                                triggered = true;
                            }
                        }
                        if (triggered) {
                            captureBuffer.markTrigger();
                            state = STATE_TRIGGERED;
                            countdown = postTriggerSize;
                        }
                    }
                        break;

                    case STATE_TRIGGERED:
                        countdown -= 1;
                        if (countdown <= 0) {
                            state = STATE_IDLE;
                            switchBuffers();
                            fireCallback();
                        }
                        break;
                }
            }
        }
    }

    private void fireCallback() {
        if (callback != null) {
            callback.run();
        }
    }

    public float getSample(int bufferIndex, int sampleIndex) {
        return displayBuffer.getSample(bufferIndex, sampleIndex);
    }

    public int getTriggerIndex() {
        return displayBuffer.getTriggerIndex();
    }

    public int getFramesCaptured() {
        return displayBuffer.getFramesCaptured();
    }

    public int getFramesPerBuffer() {
        return FRAMES_PER_BUFFER;
    }

    public int getPostTriggerSize() {
        return postTriggerSize;
    }

}
