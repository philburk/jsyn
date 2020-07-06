/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

/**
 * Display multiple waveforms together in different colors.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class MultipleWaveDisplay extends JPanel {
    private static final long serialVersionUID = -5157397030540800373L;

    private ArrayList<WaveTraceView> waveTraceViews = new ArrayList<WaveTraceView>();
    private Color[] defaultColors = {
            Color.BLUE, Color.RED, Color.BLACK, Color.MAGENTA, Color.GREEN, Color.ORANGE
    };

    public MultipleWaveDisplay() {
        setBackground(Color.WHITE);
    }

    public void addWaveTrace(WaveTraceView waveTraceView) {
        if (waveTraceView.getColor() == null) {
            waveTraceView.setColor(defaultColors[waveTraceViews.size() % defaultColors.length]);
        }
        waveTraceViews.add(waveTraceView);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        for (WaveTraceView waveTraceView : waveTraceViews.toArray(new WaveTraceView[0])) {
            waveTraceView.drawWave(g, width, height);
        }
    }
}
