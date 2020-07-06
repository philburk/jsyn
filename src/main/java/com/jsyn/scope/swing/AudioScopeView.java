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
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jsyn.scope.AudioScopeModel;
import com.jsyn.scope.AudioScopeProbe;

public class AudioScopeView extends JPanel {
    private static final long serialVersionUID = -7507986850757860853L;
    private AudioScopeModel audioScopeModel;
    private ArrayList<AudioScopeProbeView> probeViews = new ArrayList<AudioScopeProbeView>();
    private MultipleWaveDisplay multipleWaveDisplay;
    private boolean showControls = false;
    private ScopeControlPanel controlPanel = null;

    public AudioScopeView() {
        setBackground(Color.GREEN);
    }

    public void setModel(AudioScopeModel audioScopeModel) {
        this.audioScopeModel = audioScopeModel;
        // Create a view for each probe.
        probeViews.clear();
        for (AudioScopeProbe probeModel : audioScopeModel.getProbes()) {
            AudioScopeProbeView audioScopeProbeView = new AudioScopeProbeView(probeModel);
            probeViews.add(audioScopeProbeView);
        }
        setupGUI();

        // Listener for signal change events.
        audioScopeModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                multipleWaveDisplay.repaint();
            }
        });

    }

    private void setupGUI() {
        removeAll();
        setLayout(new BorderLayout());
        multipleWaveDisplay = new MultipleWaveDisplay();

        for (AudioScopeProbeView probeView : probeViews) {
            multipleWaveDisplay.addWaveTrace(probeView.getWaveTraceView());
            probeView.getModel().setColor(probeView.getWaveTraceView().getColor());
        }

        add(multipleWaveDisplay, BorderLayout.CENTER);

        setMinimumSize(new Dimension(400, 200));
        setPreferredSize(new Dimension(600, 250));
        setMaximumSize(new Dimension(1200, 300));
    }

    /** @deprecated Use setControlsVisible() instead. */
    @Deprecated
    public void setShowControls(boolean show) {
        setControlsVisible(show);
    }

    public void setControlsVisible(boolean show) {
        if (this.showControls) {
            if (!show && (controlPanel != null)) {
                remove(controlPanel);
            }
        } else {
            if (show) {
                if (controlPanel == null) {
                    controlPanel = new ScopeControlPanel(this);
                }
                add(controlPanel, BorderLayout.EAST);
                validate();
            }
        }

        this.showControls = show;
    }

    public AudioScopeModel getModel() {
        return audioScopeModel;
    }

    public AudioScopeProbeView[] getProbeViews() {
        return probeViews.toArray(new AudioScopeProbeView[0]);
    }

}
