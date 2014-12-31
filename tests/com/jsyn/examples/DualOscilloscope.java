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

package com.jsyn.examples;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JComboBox;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.JAppletFrame;
import com.jsyn.unitgen.ChannelIn;
import com.jsyn.unitgen.PassThrough;

/**
 * Two channel oscilloscope that demonstrates the use of audio input.
 * 
 * @author Phil Burk (C) 2012 Mobileer Inc
 */
public class DualOscilloscope extends JApplet {
    private static final long serialVersionUID = -2704222221111608377L;
    private Synthesizer synth;
    private ChannelIn channel1;
    private ChannelIn channel2;
    private PassThrough pass1;
    private PassThrough pass2;
    private AudioScope scope;
    private AudioDeviceManager audioManager;
    private int defaultInputId;
    private ArrayList<String> deviceNames = new ArrayList<String>();
    private ArrayList<Integer> deviceMaxInputs = new ArrayList<Integer>();
    private ArrayList<Integer> deviceIds = new ArrayList<Integer>();
    private int defaultSelection;
    private JComboBox deviceComboBox;

    @Override
    public void init() {
        audioManager = AudioDeviceFactory.createAudioDeviceManager(true);
        synth = JSyn.createSynthesizer(audioManager);

        int numDevices = audioManager.getDeviceCount();
        defaultInputId = audioManager.getDefaultInputDeviceID();
        for (int i = 0; i < numDevices; i++) {
            int maxInputs = audioManager.getMaxInputChannels(i);
            if (maxInputs > 0) {
                String deviceName = audioManager.getDeviceName(i);
                String itemName = maxInputs + ", " + deviceName + " (#" + i + ")";
                if (i == defaultInputId) {
                    defaultSelection = deviceNames.size();
                    itemName += " (Default)";
                }
                deviceNames.add(itemName);
                deviceMaxInputs.add(maxInputs);
                deviceIds.add(i);
            }
        }

        synth.add(channel1 = new ChannelIn());
        channel1.setChannelIndex(0);
        synth.add(channel2 = new ChannelIn());
        channel2.setChannelIndex(1);

        // Use PassThrough so we can easily disconnect input channels from the scope.
        synth.add(pass1 = new PassThrough());
        synth.add(pass2 = new PassThrough());

        setupGUI();
    }

    private void setupGUI() {
        setLayout(new BorderLayout());

        deviceComboBox = new JComboBox(deviceNames.toArray(new String[0]));
        deviceComboBox.setSelectedIndex(defaultSelection);
        add(deviceComboBox, BorderLayout.NORTH);
        deviceComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopAudio();
                int itemIndex = deviceComboBox.getSelectedIndex();
                startAudio(itemIndex);
            }
        });

        scope = new AudioScope(synth);

        scope.addProbe(pass1.output);
        scope.addProbe(pass2.output);

        scope.setTriggerMode(AudioScope.TriggerMode.AUTO);
        scope.getView().setControlsVisible(true);
        add(scope.getView(), BorderLayout.CENTER);
        validate();
    }

    protected void startAudio(int itemIndex) {
        // Both stereo.
        int numInputChannels = deviceMaxInputs.get(itemIndex);
        if (numInputChannels > 2)
            numInputChannels = 2;
        int inputDeviceIndex = deviceIds.get(itemIndex);
        synth.start(44100, inputDeviceIndex, numInputChannels,
                AudioDeviceManager.USE_DEFAULT_DEVICE, 0);

        channel1.output.connect(pass1.input);
        // Only connect second channel if more than one input channel.
        if (numInputChannels > 1) {
            channel2.output.connect(pass2.input);
        }

        // We only need to start the LineOut. It will pull data from the
        // channels.
        scope.start();
    }

    @Override
    public void start() {
        startAudio(defaultSelection);
    }

    public void stopAudio() {
        pass1.input.disconnectAll();
        pass2.input.disconnectAll();
        scope.stop();
        synth.stop();
    }

    @Override
    public void stop() {
        stopAudio();
    }

    /* Can be run as either an application or as an applet. */
    public static void main(String args[]) {
        DualOscilloscope applet = new DualOscilloscope();
        JAppletFrame frame = new JAppletFrame("Dual Oscilloscope", applet);
        frame.setSize(640, 400);
        frame.setVisible(true);
        frame.test();
    }

}
