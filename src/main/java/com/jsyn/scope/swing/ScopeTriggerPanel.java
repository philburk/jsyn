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

package com.jsyn.scope.swing;

import java.awt.BorderLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.jsyn.scope.AudioScopeModel;
import com.jsyn.scope.TriggerModel;
import com.jsyn.scope.AudioScope.TriggerMode;
import com.jsyn.swing.RotaryTextController;

public class ScopeTriggerPanel extends JPanel {
    private static final long serialVersionUID = 4511589171299298548L;
    private JComboBox<DefaultComboBoxModel<TriggerMode>> triggerModeComboBox;
    private RotaryTextController triggerLevelKnob;

    public ScopeTriggerPanel(AudioScopeModel audioScopeModel) {
        setLayout(new BorderLayout());
        TriggerModel triggerModel = audioScopeModel.getTriggerModel();
        triggerModeComboBox = new JComboBox(triggerModel.getModeModel());
        add(triggerModeComboBox, BorderLayout.NORTH);

        triggerLevelKnob = new RotaryTextController(triggerModel.getLevelModel(), 5);

        add(triggerLevelKnob, BorderLayout.CENTER);
        triggerLevelKnob.setTitle("Trigger Level");
    }

}
