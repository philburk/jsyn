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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JToggleButton.ToggleButtonModel;

import com.jsyn.scope.WaveTraceModel;
import com.jsyn.swing.ExponentialRangeModel;

public class WaveTraceView {
    private static final double AUTO_DECAY = 0.95;
    private WaveTraceModel waveTraceModel;
    private Color color;
    private ExponentialRangeModel verticalScaleModel;
    private ToggleButtonModel autoScaleButtonModel;

    private double xScaler;
    private double yScalar;
    private int centerY;

    public WaveTraceView(ToggleButtonModel autoButtonModel, ExponentialRangeModel verticalRangeModel) {
        this.verticalScaleModel = verticalRangeModel;
        this.autoScaleButtonModel = autoButtonModel;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ExponentialRangeModel getVerticalRangeModel() {
        return verticalScaleModel;
    }

    public ToggleButtonModel getAutoButtonModel() {
        return autoScaleButtonModel;
    }

    public void setModel(WaveTraceModel waveTraceModel) {
        this.waveTraceModel = waveTraceModel;
    }

    public int convertRealToY(double r) {
        return centerY - (int) (yScalar * r);
    }

    public void drawWave(Graphics g, int width, int height) {
        double sampleMax = 0.0;
        double sampleMin = 0.0;
        g.setColor(color);
        int numSamples = waveTraceModel.getVisibleSize();
        if (numSamples > 0) {
            xScaler = (double) width / numSamples;
            // Scale by 0.5 because it is bipolar.
            yScalar = 0.5 * height / verticalScaleModel.getDoubleValue();
            centerY = height / 2;

            // Calculate position of first point.
            int x1 = 0;
            int offset = waveTraceModel.getStartIndex();
            double value = waveTraceModel.getSample(offset);
            int y1 = convertRealToY(value);

            // Draw lines to remaining points.
            for (int i = 1; i < numSamples; i++) {
                int x2 = (int) (i * xScaler);
                value = waveTraceModel.getSample(offset + i);
                int y2 = convertRealToY(value);
                g.drawLine(x1, y1, x2, y2);
                x1 = x2;
                y1 = y2;
                // measure min and max for auto
                if (value > sampleMax) {
                    sampleMax = value;
                } else if (value < sampleMin) {
                    sampleMin = value;
                }
            }

            autoScaleRange(sampleMax);
        }
    }

    // Autoscale the vertical range.
    private void autoScaleRange(double sampleMax) {
        if (autoScaleButtonModel.isSelected()) {
            double scaledMax = sampleMax * 1.1;
            double current = verticalScaleModel.getDoubleValue();
            if (scaledMax > current) {
                verticalScaleModel.setDoubleValue(scaledMax);
            } else {
                double decayed = current * AUTO_DECAY;
                if (decayed > verticalScaleModel.getMinimum()) {
                    if (scaledMax < decayed) {
                        verticalScaleModel.setDoubleValue(decayed);
                    }
                }
            }
        }
    }

}
