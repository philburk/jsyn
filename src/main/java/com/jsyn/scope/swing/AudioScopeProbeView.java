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

package com.jsyn.scope.swing;

import com.jsyn.scope.AudioScopeProbe;

/**
 * Wave display associated with a probe.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class AudioScopeProbeView {
    private AudioScopeProbe probeModel;
    private WaveTraceView waveTrace;

    public AudioScopeProbeView(AudioScopeProbe probeModel) {
        this.probeModel = probeModel;
        waveTrace = new WaveTraceView(probeModel.getAutoScaleButtonModel(),
                probeModel.getVerticalScaleModel());
        waveTrace.setModel(probeModel.getWaveTraceModel());
    }

    public WaveTraceView getWaveTraceView() {
        return waveTrace;
    }

    public AudioScopeProbe getModel() {
        return probeModel;
    }

}
