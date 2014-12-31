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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton.ToggleButtonModel;

import com.jsyn.scope.AudioScopeProbe;
import com.jsyn.swing.RotaryTextController;

public class ScopeProbePanel extends JPanel {
    private static final long serialVersionUID = 4511589171299298548L;
    private AudioScopeProbeView audioScopeProbeView;
    private AudioScopeProbe audioScopeProbe;
    private RotaryTextController verticalScaleKnob;
    private JCheckBox autoBox;
    private ToggleButtonModel autoScaleModel;

    public ScopeProbePanel(AudioScopeProbeView probeView) {
        this.audioScopeProbeView = probeView;
        setLayout(new BorderLayout());

        setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));

        // Add a colored box to match the waveform color.
        JPanel colorPanel = new JPanel();
        colorPanel.setMinimumSize(new Dimension(40, 40));
        audioScopeProbe = probeView.getModel();
        colorPanel.setBackground(audioScopeProbe.getColor());
        add(colorPanel, BorderLayout.NORTH);

        // Knob for tweaking vertical range.
        verticalScaleKnob = new RotaryTextController(audioScopeProbeView.getWaveTraceView()
                .getVerticalRangeModel(), 5);
        add(verticalScaleKnob, BorderLayout.CENTER);
        verticalScaleKnob.setTitle("YScale");

        // Auto ranging checkbox.
        autoBox = new JCheckBox("Auto");
        autoScaleModel = audioScopeProbeView.getWaveTraceView().getAutoButtonModel();
        autoScaleModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ToggleButtonModel model = (ToggleButtonModel) e.getSource();
                boolean enabled = !model.isSelected();
                System.out.println("Knob enabled = " + enabled);
                verticalScaleKnob.setEnabled(!model.isSelected());
            }
        });
        autoBox.setModel(autoScaleModel);
        add(autoBox, BorderLayout.SOUTH);

        verticalScaleKnob.setEnabled(!autoScaleModel.isSelected());

        setMinimumSize(new Dimension(80, 100));
        setPreferredSize(new Dimension(80, 150));
        setMaximumSize(new Dimension(120, 200));
    }

}
