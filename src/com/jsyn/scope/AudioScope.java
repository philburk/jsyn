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

package com.jsyn.scope;

import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.scope.swing.AudioScopeView;

// TODO Auto and Manual triggers.
// TODO Auto scaling of vertical.
// TODO Fixed size Y scale knobs.
// TODO Pan back and forth around trigger.
// TODO Continuous capture
/**
 * Digital oscilloscope for JSyn.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class AudioScope {
    public enum TriggerMode {
        AUTO, NORMAL // , MANUAL
    }

    public enum ViewMode {
        WAVEFORM, SPECTRUM
    }

    private AudioScopeView audioScopeView = null;
    private AudioScopeModel audioScopeModel;

    public AudioScope(Synthesizer synth) {
        audioScopeModel = new AudioScopeModel(synth);
    }

    public AudioScopeProbe addProbe(UnitOutputPort output) {
        return addProbe(output, 0);
    }

    public AudioScopeProbe addProbe(UnitOutputPort output, int partIndex) {
        return audioScopeModel.addProbe(output, partIndex);
    }

    public void start() {
        audioScopeModel.start();
    }

    public void stop() {
        audioScopeModel.stop();
    }

    public AudioScopeModel getModel() {
        return audioScopeModel;
    }

    public AudioScopeView getView() {
        if (audioScopeView == null) {
            audioScopeView = new AudioScopeView();
            audioScopeView.setModel(audioScopeModel);
        }
        return audioScopeView;
    }

    public void setTriggerMode(TriggerMode triggerMode) {
        audioScopeModel.setTriggerMode(triggerMode);
    }

    public void setTriggerSource(AudioScopeProbe probe) {
        audioScopeModel.setTriggerSource(probe);
    }

    public void setTriggerLevel(double level) {
        getModel().getTriggerModel().getLevelModel().setDoubleValue(level);
    }

    public double getTriggerLevel() {
        return getModel().getTriggerModel().getLevelModel().getDoubleValue();
    }

    /**
     * Not yet implemented.
     * @param viewMode
     */
    public void setViewMode(ViewMode viewMode) {
        // TODO Auto-generated method stub
    }

}
