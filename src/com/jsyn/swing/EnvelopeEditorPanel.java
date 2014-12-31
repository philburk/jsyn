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

package com.jsyn.swing;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EnvelopeEditorPanel extends JPanel {
    EnvelopeEditorBox editor;
    Checkbox pointsBox;
    Checkbox sustainBox;
    Checkbox releaseBox;
    Checkbox autoBox;
    Button onButton;
    Button offButton;
    Button clearButton;
    Button yUpButton;
    Button yDownButton;
    DoubleBoundedTextField zoomField;

    public EnvelopeEditorPanel(EnvelopePoints points, int maxFrames) {
        setSize(600, 300);

        setLayout(new BorderLayout());
        editor = new EnvelopeEditorBox();
        editor.setMaxPoints(maxFrames);
        editor.setBackground(Color.cyan);
        editor.setPoints(points);
        editor.setMinimumSize(new Dimension(500, 300));

        add(editor, "Center");

        JPanel buttonPanel = new JPanel();
        add(buttonPanel, "South");

        CheckboxGroup cbg = new CheckboxGroup();
        pointsBox = new Checkbox("points", cbg, true);
        pointsBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                editor.setMode(EnvelopeEditorBox.EDIT_POINTS);
            }
        });
        buttonPanel.add(pointsBox);

        sustainBox = new Checkbox("onLoop", cbg, false);
        sustainBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                editor.setMode(EnvelopeEditorBox.SELECT_SUSTAIN);
            }
        });
        buttonPanel.add(sustainBox);

        releaseBox = new Checkbox("offLoop", cbg, false);
        releaseBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                editor.setMode(EnvelopeEditorBox.SELECT_RELEASE);
            }
        });
        buttonPanel.add(releaseBox);

        autoBox = new Checkbox("AutoStop", false);
        /*
         * buttonPanel.add( onButton = new Button( "On" ) ); onButton.addActionListener( module );
         * buttonPanel.add( offButton = new Button( "Off" ) ); offButton.addActionListener( module
         * ); buttonPanel.add( clearButton = new Button( "Clear" ) ); clearButton.addActionListener(
         * module );
         */
        buttonPanel.add(yUpButton = new Button("Y*2"));
        yUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaleEnvelopeValues(2.0);
            }
        });

        buttonPanel.add(yDownButton = new Button("Y/2"));
        yDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaleEnvelopeValues(0.5);
            }
        });

        /* Add a TextField for setting the Y scale. */
        double max = getMaxEnvelopeValue(editor.getPoints());
        editor.setMaxWorldY(max);
        buttonPanel.add(new Label("YMax ="));
        final DoubleBoundedRangeModel model = new DoubleBoundedRangeModel("YMax", 100000, 1.0,
                100001.0, 1.0);
        buttonPanel.add(zoomField = new DoubleBoundedTextField(model, 8));
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    double val = model.getDoubleValue();
                    editor.setMaxWorldY(val);
                    editor.repaint();
                } catch (NumberFormatException exp) {
                    zoomField.setText("ERROR");
                    zoomField.selectAll();
                }
            }
        });

        validate();
    }

    /**
     * Multiply all the values in the envelope by scalar.
     */
    double getMaxEnvelopeValue(EnvelopePoints points) {
        double max = 1.0;
        for (int i = 0; i < points.size(); i++) {
            double value = points.getValue(i);
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * Multiply all the values in the envelope by scalar.
     */
    void scaleEnvelopeValues(double scalar) {
        EnvelopePoints points = editor.getPoints();
        for (int i = 0; i < points.size(); i++) {
            double[] dar = points.getPoint(i);
            dar[1] = dar[1] * scalar; // scale value
        }
        points.setDirty(true);
        editor.repaint();
    }
}
