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

package com.jsyn.swing;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Combine a RotaryController and a DoubleBoundedTextField into a convenient package.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class RotaryTextController extends JPanel {
    private static final long serialVersionUID = -2931828326251895375L;
    private RotaryController rotary;
    private DoubleBoundedTextField textField;

    public RotaryTextController(DoubleBoundedRangeModel pModel, int numDigits) {
        rotary = new RotaryController(pModel);
        textField = new DoubleBoundedTextField(pModel, numDigits);
        setLayout(new BorderLayout());
        add(rotary, BorderLayout.CENTER);
        add(textField, BorderLayout.SOUTH);
    }

    /** Display the title in a border. */
    public void setTitle(String label) {
        setBorder(BorderFactory.createTitledBorder(label));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        rotary.setEnabled(enabled);
        textField.setEnabled(enabled);
    }
}
