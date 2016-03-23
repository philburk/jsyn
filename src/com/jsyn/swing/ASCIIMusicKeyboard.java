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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Support for playing musical scales on the ASCII keyboard of a computer. Has a Sustain checkbox
 * that simulates a sustain pedal. Auto-repeat keys are detected and suppressed.
 *
 * @author Phil Burk (C) 2012 Mobileer Inc
 */
@SuppressWarnings("serial")
public abstract class ASCIIMusicKeyboard extends JPanel {
    private final JCheckBox sustainBox;
    private final JButton focusButton;
    public static final String PENTATONIC_KEYS = "zxcvbasdfgqwert12345";
    public static final String SEPTATONIC_KEYS = "zxcvbnmasdfghjqwertyu1234567890";
    private String keyboardLayout = SEPTATONIC_KEYS; /* default music keyboard layout */
    private int basePitch = 48;
    private final KeyListener keyListener;
    private final JLabel countLabel;
    private int onCount;
    private int offCount;
    private int pressedCount;
    private int releasedCount;
    private final HashSet<Integer> pressedKeys = new HashSet<Integer>();
    private final HashSet<Integer> onKeys = new HashSet<Integer>();

    public ASCIIMusicKeyboard() {
        focusButton = new JButton("Click here to play ASCII keys.");
        focusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            }
        });
        keyListener = new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyChar();
                int idx = keyboardLayout.indexOf(key);
                System.out.println("keyPressed " + idx);
                if (idx >= 0) {
                    if (!pressedKeys.contains(idx)) {
                        keyOn(convertIndexToPitch(idx));
                        onCount++;
                        pressedKeys.add(idx);
                        onKeys.add(idx);
                    }
                }
                pressedCount++;
                updateCountLabel();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyChar();
                int idx = keyboardLayout.indexOf(key);
                System.out.println("keyReleased " + idx);
                if (idx >= 0) {
                    if (!sustainBox.isSelected()) {
                        noteOffInternal(idx);
                        onKeys.remove(idx);
                    }
                    pressedKeys.remove(idx);
                }
                releasedCount++;
                updateCountLabel();
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        };
        focusButton.addKeyListener(keyListener);
        add(focusButton);

        sustainBox = new JCheckBox("sustain");
        sustainBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!sustainBox.isSelected()) {
                    for (Integer noteIndex : onKeys) {
                        noteOffInternal(noteIndex);
                    }
                    onKeys.clear();
                }
            }
        });
        add(sustainBox);
        sustainBox.addKeyListener(keyListener);

        countLabel = new JLabel("0");
        add(countLabel);
    }

    private void noteOffInternal(int idx) {
        keyOff(convertIndexToPitch(idx));
        offCount++;
    }

    protected void updateCountLabel() {
        countLabel.setText(onCount + "/" + offCount + ", " + pressedCount + "/" + releasedCount);
    }

    /**
     * Convert index to a MIDI noteNumber in a major scale. Result will be offset by the basePitch.
     */
    public int convertIndexToPitch(int keyIndex) {
        int scale[] = {
                0, 2, 4, 5, 7, 9, 11
        };
        int octave = keyIndex / scale.length;
        int idx = keyIndex % scale.length;
        int pitch = (octave * 12) + scale[idx];
        return pitch + basePitch;
    }

    /**
     * This will be called when a key is released. It may also be called for sustaining notes when
     * the Sustain check box is turned off.
     *
     * @param keyIndex
     */
    public abstract void keyOff(int keyIndex);

    /**
     * This will be called when a key is pressed.
     *
     * @param keyIndex
     */
    public abstract void keyOn(int keyIndex);

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    /**
     * Specify the keys that will be active for music.
     * For example "qwertyui".
     * If the first character in the layout is
     * pressed then keyOn() will be called with 0. Default is SEPTATONIC_KEYS.
     *
     * @param keyboardLayout defines order of playable keys
     */
    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    public int getBasePitch() {
        return basePitch;
    }

    /**
     * Define offset used by convertIndexToPitch().
     *
     * @param basePitch
     */
    public void setBasePitch(int basePitch) {
        this.basePitch = basePitch;
    }

    /**
     * @return
     */
    public KeyListener getKeyListener() {
        return keyListener;
    }
}
