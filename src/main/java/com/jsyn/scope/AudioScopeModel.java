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

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.scope.AudioScope.TriggerMode;

public class AudioScopeModel implements Runnable {
    private static final int PRE_TRIGGER_SIZE = 32;
    private Synthesizer synthesisEngine;
    private ArrayList<AudioScopeProbe> probes = new ArrayList<AudioScopeProbe>();
    private CopyOnWriteArrayList<ChangeListener> changeListeners = new CopyOnWriteArrayList<ChangeListener>();
    private MultiChannelScopeProbeUnit probeUnit;
    private double timeToArm;
    private double period = 0.2;
    private TriggerModel triggerModel;

    public AudioScopeModel(Synthesizer synth) {
        this.synthesisEngine = synth;
        triggerModel = new TriggerModel();
    }

    public AudioScopeProbe addProbe(UnitOutputPort output, int partIndex) {
        AudioScopeProbe probe = new AudioScopeProbe(this, output, partIndex);
        DefaultWaveTraceModel waveTraceModel = new DefaultWaveTraceModel(this, probes.size());
        probe.setWaveTraceModel(waveTraceModel);
        probes.add(probe);
        if (triggerModel.getSource() == null) {
            triggerModel.setSource(probe);
        }
        return probe;
    }

    public void start() {
        stop();
        probeUnit = new MultiChannelScopeProbeUnit(probes.size(), triggerModel);
        synthesisEngine.add(probeUnit);
        for (int i = 0; i < probes.size(); i++) {
            AudioScopeProbe probe = probes.get(i);
            probe.getSource().connect(probe.getPartIndex(), probeUnit.input, i);
        }
        // Connect trigger signal to input of probe.
        triggerModel.getSource().getSource()
                .connect(triggerModel.getSource().getPartIndex(), probeUnit.trigger, 0);
        probeUnit.start();

        // Get synthesizer time in seconds.
        timeToArm = synthesisEngine.getCurrentTime();
        probeUnit.arm(timeToArm, this);
    }

    public void stop() {
        if (probeUnit != null) {
            for (int i = 0; i < probes.size(); i++) {
                probeUnit.input.disconnectAll(i);
            }
            probeUnit.trigger.disconnectAll();
            probeUnit.stop();
            synthesisEngine.remove(probeUnit);
            probeUnit = null;
        }
    }

    public AudioScopeProbe[] getProbes() {
        return probes.toArray(new AudioScopeProbe[0]);
    }

    public Synthesizer getSynthesizer() {
        return synthesisEngine;
    }

    @Override
    public void run() {
        fireChangeListeners();
        timeToArm = synthesisEngine.getCurrentTime();
        timeToArm += period;
        probeUnit.arm(timeToArm, this);
    }

    private void fireChangeListeners() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(changeEvent);
        }
        // debug();
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    public void setTriggerMode(TriggerMode triggerMode) {
        triggerModel.getModeModel().setSelectedItem(triggerMode);
    }

    public void setTriggerSource(AudioScopeProbe probe) {
        triggerModel.setSource(probe);
    }

    public double getSample(int bufferIndex, int i) {
        return probeUnit.getSample(bufferIndex, i);
    }

    public int getFramesPerBuffer() {
        return probeUnit.getFramesPerBuffer();
    }

    public int getFramesCaptured() {
        return probeUnit.getFramesCaptured();
    }

    public int getVisibleSize() {
        int size = 0;
        if (probeUnit != null) {
            size = probeUnit.getPostTriggerSize() + PRE_TRIGGER_SIZE;
            if (size > getFramesCaptured()) {
                size = getFramesCaptured();
            }
        }
        return size;
    }

    public int getStartIndex() {
        // TODO Add pan support here.
        return getFramesCaptured() - getVisibleSize();
    }

    public TriggerModel getTriggerModel() {
        return triggerModel;
    }

}
