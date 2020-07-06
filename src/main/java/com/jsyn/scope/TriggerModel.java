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

import javax.swing.DefaultComboBoxModel;

import com.jsyn.scope.AudioScope.TriggerMode;
import com.jsyn.swing.ExponentialRangeModel;

public class TriggerModel {
    private ExponentialRangeModel levelModel;
    private DefaultComboBoxModel<AudioScope.TriggerMode> modeModel;
    private AudioScopeProbe source;

    public TriggerModel() {
        modeModel = new DefaultComboBoxModel<AudioScope.TriggerMode>();
        modeModel.addElement(TriggerMode.AUTO);
        modeModel.addElement(TriggerMode.NORMAL);
        levelModel = new ExponentialRangeModel("TriggerLevel", 1000, 0.01, 2.0, 0.04);
    }

    public AudioScopeProbe getSource() {
        return source;
    }

    public void setSource(AudioScopeProbe source) {
        this.source = source;
    }

    public ExponentialRangeModel getLevelModel() {
        return levelModel;
    }

    public void setLevelModel(ExponentialRangeModel levelModel) {
        this.levelModel = levelModel;
    }

    public DefaultComboBoxModel<TriggerMode> getModeModel() {
        return modeModel;
    }

    public void setModeModel(DefaultComboBoxModel<TriggerMode> modeModel) {
        this.modeModel = modeModel;
    }

    public double getTriggerLevel() {
        return levelModel.getDoubleValue();
    }

    public TriggerMode getMode() {
        return (TriggerMode) modeModel.getSelectedItem();
    }
}
