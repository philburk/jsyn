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

import java.awt.Color;

import javax.swing.JToggleButton.ToggleButtonModel;

import com.jsyn.ports.UnitOutputPort;
import com.jsyn.swing.ExponentialRangeModel;

/**
 * Collect data from the source and make it available to the scope.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class AudioScopeProbe {
    // private UnitOutputPort output;
    private WaveTraceModel waveTraceModel;
    private AudioScopeModel audioScopeModel;
    private UnitOutputPort source;
    private int partIndex;
    private Color color;
    private ExponentialRangeModel verticalScaleModel;
    private ToggleButtonModel autoScaleButtonModel;
    private double MIN_RANGE = 0.01;
    private double MAX_RANGE = 100.0;

    public AudioScopeProbe(AudioScopeModel audioScopeModel, UnitOutputPort source, int partIndex) {
        this.audioScopeModel = audioScopeModel;
        this.source = source;
        this.partIndex = partIndex;

        verticalScaleModel = new ExponentialRangeModel("VScale", 1000, MIN_RANGE, MAX_RANGE,
                MIN_RANGE);
        autoScaleButtonModel = new ToggleButtonModel();
        autoScaleButtonModel.setSelected(true);
    }

    public WaveTraceModel getWaveTraceModel() {
        return waveTraceModel;
    }

    public void setWaveTraceModel(WaveTraceModel waveTraceModel) {
        this.waveTraceModel = waveTraceModel;
    }

    public UnitOutputPort getSource() {
        return source;
    }

    public int getPartIndex() {
        return partIndex;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setAutoScaleEnabled(boolean enabled) {
        autoScaleButtonModel.setSelected(enabled);
    }

    public void setVerticalScale(double max) {
        verticalScaleModel.setDoubleValue(max);
    }

    public ExponentialRangeModel getVerticalScaleModel() {
        return verticalScaleModel;
    }

    public ToggleButtonModel getAutoScaleButtonModel() {
        return autoScaleButtonModel;
    }

}
