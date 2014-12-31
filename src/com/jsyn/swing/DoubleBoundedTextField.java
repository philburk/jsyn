/*
 * Copyright 2000 Phil Burk, Mobileer Inc
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

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * TextField that turns pink when modified, and white when the value is entered.
 * 
 * @author (C) 2000-2010 Phil Burk, Mobileer Inc
 * @version 16
 */

public class DoubleBoundedTextField extends JTextField {
    private static final long serialVersionUID = 6882779668177620812L;
    boolean modified = false;
    int numCharacters;
    private DoubleBoundedRangeModel model;

    public DoubleBoundedTextField(DoubleBoundedRangeModel pModel, int numCharacters) {
        super(numCharacters);
        this.model = pModel;
        this.numCharacters = numCharacters;
        setHorizontalAlignment(SwingConstants.LEADING);
        setValue(model.getDoubleValue());
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    model.setDoubleValue(getValue());
                } else {
                    markDirty();
                }
            }
        });
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setValue(model.getDoubleValue());
            }
        });
    }

    private void markDirty() {
        modified = true;
        setBackground(Color.pink);
        repaint();
    }

    private void markClean() {
        modified = false;
        setBackground(Color.white);
        setCaretPosition(0);
        repaint();
    }

    @Override
    public void setText(String text) {
        markDirty();
        super.setText(text);
    }

    private double getValue() throws NumberFormatException {
        double val = Double.valueOf(getText()).doubleValue();
        markClean();
        return val;
    }

    private void setValue(double value) {
        super.setText(String.format("%6.4f", value));
        markClean();
    }
}
