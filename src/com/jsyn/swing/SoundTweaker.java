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

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jsyn.Synthesizer;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitPort;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.Instrument;
import com.softsynth.math.AudioMath;

@SuppressWarnings("serial")
public class SoundTweaker extends JPanel {
    private UnitSource source;
    private ASCIIMusicKeyboard keyboard;
    private Synthesizer synth;

    static Logger logger = Logger.getLogger(SoundTweaker.class.getName());

    public SoundTweaker(Synthesizer synth, String title, UnitSource source) {
        this.synth = synth;
        this.source = source;

        setLayout(new GridLayout(0, 2));

        UnitGenerator ugen = source.getUnitGenerator();
        ArrayList<Component> sliders = new ArrayList<Component>();

        add(new JLabel(title));

        if (source instanceof Instrument) {
            add(keyboard = createPolyphonicKeyboard());
        } else if (source instanceof UnitVoice) {
            add(keyboard = createMonophonicKeyboard());
        }

        // Arrange the faders in a stack.
        // Iterate through the ports.
        for (UnitPort port : ugen.getPorts()) {
            if (port instanceof UnitInputPort) {
                UnitInputPort inputPort = (UnitInputPort) port;
                Component slider;
                // Use an exponential slider if it seems appropriate.
                if ((inputPort.getMinimum() > 0.0)
                        && ((inputPort.getMaximum() / inputPort.getMinimum()) > 4.0)) {
                    slider = PortControllerFactory.createExponentialPortSlider(inputPort);
                } else {
                    slider = PortControllerFactory.createPortSlider(inputPort);

                }
                add(slider);
                sliders.add(slider);
            }
        }

        if (keyboard != null) {
            for (Component slider : sliders) {
                slider.addKeyListener(keyboard.getKeyListener());
            }
        }
        validate();
    }

    @SuppressWarnings("serial")
    private ASCIIMusicKeyboard createPolyphonicKeyboard() {
        ASCIIMusicKeyboard keyboard = new ASCIIMusicKeyboard() {
            @Override
            public void keyOff(int pitch) {
                ((Instrument) source).noteOff(pitch, synth.createTimeStamp());
            }

            @Override
            public void keyOn(int pitch) {
                double freq = AudioMath.pitchToFrequency(pitch);
                ((Instrument) source).noteOn(pitch, freq, 0.5, synth.createTimeStamp());
            }
        };
        return keyboard;
    }

    @SuppressWarnings("serial")
    private ASCIIMusicKeyboard createMonophonicKeyboard() {
        ASCIIMusicKeyboard keyboard = new ASCIIMusicKeyboard() {
            @Override
            public void keyOff(int pitch) {
                ((UnitVoice) source).noteOff(synth.createTimeStamp());
            }

            @Override
            public void keyOn(int pitch) {
                double freq = AudioMath.pitchToFrequency(pitch);
                ((UnitVoice) source).noteOn(freq, 0.5, synth.createTimeStamp());
            }
        };
        return keyboard;
    }

}
