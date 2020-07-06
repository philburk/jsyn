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

public class DefaultWaveTraceModel implements WaveTraceModel {
    private AudioScopeModel audioScopeModel;
    private int bufferIndex;

    public DefaultWaveTraceModel(AudioScopeModel audioScopeModel, int bufferIndex) {
        this.audioScopeModel = audioScopeModel;
        this.bufferIndex = bufferIndex;
    }

    @Override
    public double getSample(int i) {
        return audioScopeModel.getSample(bufferIndex, i);
    }

    @Override
    public int getSize() {
        return audioScopeModel.getFramesCaptured();
    }

    @Override
    public int getStartIndex() {
        return audioScopeModel.getStartIndex();
    }

    @Override
    public int getVisibleSize() {
        return audioScopeModel.getVisibleSize();
    }

}
