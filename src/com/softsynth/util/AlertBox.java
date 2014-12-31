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

package com.softsynth.util;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AlertBox class to display error messages.
 * 
 * @author (C) 2000 Phil Burk, SoftSynth.com
 */

public class AlertBox extends Dialog {
    TextArea textArea;
    Button okButton;
    Button abortButton;

    public AlertBox(Frame frame, String title) {
        super(frame, title, true);
        setLayout(new BorderLayout());
        setSize(620, 200);
        textArea = new TextArea("", 6, 80, TextArea.SCROLLBARS_BOTH);
        add("Center", textArea);

        Panel panel = new Panel();
        add("South", panel);

        panel.add(okButton = new Button("Acknowledge"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        panel.add(abortButton = new Button("Abort"));
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        validate();
    }

    /**
     * Search up the component hierarchy to find the first Frame containing the component.
     */
    public static Frame getFrame(Component c) {
        do {
            if (c instanceof Frame)
                return (Frame) c;
        } while ((c = c.getParent()) != null);
        return null;
    }

    public void showError(String msg) {
        textArea.setText(msg);
        show();
    }
}
