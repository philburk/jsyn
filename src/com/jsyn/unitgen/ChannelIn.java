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

package com.jsyn.unitgen;

import com.jsyn.ports.UnitOutputPort;

/**
 * Provides access to one specific channel of the audio input. For ChannelIn to work you must call
 * the Synthesizer start() method with numInputChannels &gt; 0.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @see ChannelOut
 * @see LineIn
 */
public class ChannelIn extends UnitGenerator {
    public UnitOutputPort output;
    private int channelIndex;

    public ChannelIn() {
        this(0);
    }

    public ChannelIn(int channelIndex) {
        addPort(output = new UnitOutputPort());
        setChannelIndex(channelIndex);
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    @Override
    public void generate(int start, int limit) {
        double[] outputs = output.getValues(0);
        double[] buffer = synthesisEngine.getInputBuffer(channelIndex);
        for (int i = start; i < limit; i++) {
            outputs[i] = buffer[i];
        }
    }

}
