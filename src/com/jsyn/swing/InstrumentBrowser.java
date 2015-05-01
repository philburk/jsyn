/*
 * Copyright 2012 Phil Burk, Mobileer Inc
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

package com.jsyn.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jsyn.util.InstrumentLibrary;
import com.jsyn.util.VoiceDescription;

/**
 * Display a list of VoiceDescriptions and their associated presets. Notify PresetSelectionListeners
 * when a preset is selected.
 * 
 * @author Phil Burk (C) 2012 Mobileer Inc
 */
@SuppressWarnings("serial")
public class InstrumentBrowser extends JPanel {
    private InstrumentLibrary library;
    private JScrollPane listScroller2;
    private VoiceDescription voiceDescription;
    private ArrayList<PresetSelectionListener> listeners = new ArrayList<PresetSelectionListener>();

    public InstrumentBrowser(InstrumentLibrary library) {
        this.library = library;
        JPanel horizontalPanel = new JPanel();
        horizontalPanel.setLayout(new GridLayout(1, 2));

        final JList<VoiceDescription> instrumentList = new JList<VoiceDescription>(library.getVoiceDescriptions());
        setupList(instrumentList);
        instrumentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    int n = instrumentList.getSelectedIndex();
                    if (n >= 0) {
                        showPresetList(n);
                    }
                }
            }
        });

        JScrollPane listScroller1 = new JScrollPane(instrumentList);
        listScroller1.setPreferredSize(new Dimension(250, 120));
        add(listScroller1);

        instrumentList.setSelectedIndex(0);
    }

    public void addPresetSelectionListener(PresetSelectionListener listener) {
        listeners.add(listener);
    }

    public void removePresetSelectionListener(PresetSelectionListener listener) {
        listeners.remove(listener);
    }

    private void firePresetSelectionListeners(VoiceDescription voiceDescription, int presetIndex) {
        for (PresetSelectionListener listener : listeners) {
            listener.presetSelected(voiceDescription, presetIndex);
        }
    }

    private void showPresetList(int n) {
        if (listScroller2 != null) {
            remove(listScroller2);
        }
        voiceDescription = library.getVoiceDescriptions()[n];
        final JList<String> presetList = new JList<String>(voiceDescription.getPresetNames());
        setupList(presetList);
        presetList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    int n = presetList.getSelectedIndex();
                    if (n >= 0) {
                        firePresetSelectionListeners(voiceDescription, n);
                    }
                }
            }
        });

        listScroller2 = new JScrollPane(presetList);
        listScroller2.setPreferredSize(new Dimension(250, 120));
        add(listScroller2);
        presetList.setSelectedIndex(0);
        validate();
    }

    private void setupList(@SuppressWarnings("rawtypes") JList list) {
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
    }
}
