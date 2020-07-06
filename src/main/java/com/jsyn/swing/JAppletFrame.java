/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JApplet;
import javax.swing.JFrame;

/**
 * Frame that allows a program to be run as either an Application or an Applet. Used by JSyn example
 * programs.
 * 
 * @author (C) 1997 Phil Burk, SoftSynth.com
 */

public class JAppletFrame extends JFrame {
    private static final long serialVersionUID = -6047247494856379114L;
    JApplet applet;

    public JAppletFrame(String frameTitle, final JApplet pApplet) {
        super(frameTitle);
        this.applet = pApplet;
        getContentPane().add(applet);
        repaint();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                applet.stop();
                applet.destroy();
                try {
                    System.exit(0);
                } catch (SecurityException exc) {
                    System.err.println("System.exit(0) not allowed by Java VM.");
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }
        });
    }

    public void test() {
        applet.init();
        applet.start();
    }

}
