/*
 * Copyright 2002 Phil Burk, Mobileer Inc
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

import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jsyn.util.NumericOutput;

/**
 * Slider that takes a DoubleBoundedRangeModel. It displays the current value in a titled border.
 * 
 * @author Phil Burk, (C) 2002 SoftSynth.com, PROPRIETARY and CONFIDENTIAL
 */

public class DoubleBoundedRangeSlider extends JSlider {
    /**
	 * 
	 */
    private static final long serialVersionUID = -440390322602838998L;
    /** Places after decimal point for display. */
    private int places;

    public DoubleBoundedRangeSlider(DoubleBoundedRangeModel model) {
        this(model, 5);
    }

    public DoubleBoundedRangeSlider(DoubleBoundedRangeModel model, int places) {
        super(model);
        this.places = places;
        setBorder(BorderFactory.createTitledBorder(generateTitleText()));
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTitle();
            }
        });
    }

    protected void updateTitle() {
        TitledBorder border = (TitledBorder) getBorder();
        if (border != null) {
            border.setTitle(generateTitleText());
            repaint();
        }
    }

    String generateTitleText() {
        DoubleBoundedRangeModel model = (DoubleBoundedRangeModel) getModel();
        double val = model.getDoubleValue();
        String valText = NumericOutput.doubleToString(val, 0, places);
        return model.getName() + " = " + valText;
    }

    public void makeStandardLabels(int labelSpacing) {
        setMajorTickSpacing(labelSpacing / 2);
        setLabelTable(createStandardLabels(labelSpacing));
        setPaintTicks(true);
        setPaintLabels(true);
    }

    public double nextLabelValue(double current, double delta) {
        return current + delta;
    }

    public void makeLabels(double start, double delta, int places) {
        DoubleBoundedRangeModel model = (DoubleBoundedRangeModel) getModel();
        // Create the label table
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        double dval = start;
        while (dval <= model.getDoubleMaximum()) {
            int sliderValue = model.doubleToSlider(dval);
            String text = NumericOutput.doubleToString(dval, 0, places);
            labelTable.put(new Integer(sliderValue), new JLabel(text));
            dval = nextLabelValue(dval, delta);
        }
        setLabelTable(labelTable);
        setPaintLabels(true);
    }

}
