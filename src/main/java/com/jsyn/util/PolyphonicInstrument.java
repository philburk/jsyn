/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

package com.jsyn.util;

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitPort;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitSource;
import com.jsyn.unitgen.UnitVoice;
import com.softsynth.shared.time.TimeStamp;

/**
 * The API for this class is likely to change. Please comment on its usefulness.
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 */

public class PolyphonicInstrument extends Circuit implements UnitSource, Instrument {
    private Multiply mixer;
    private UnitVoice[] voices;
    private VoiceAllocator voiceAllocator;
    public UnitInputPort amplitude;

    public PolyphonicInstrument(UnitVoice[] voices) {
        this.voices = voices;
        voiceAllocator = new VoiceAllocator(voices);
        add(mixer = new Multiply());
        // Mix all the voices to one output.
        for (UnitVoice voice : voices) {
            UnitGenerator unit = voice.getUnitGenerator();
            boolean wasEnabled = unit.isEnabled();
            // This overrides the enabled property of the voice.
            add(unit);
            voice.getOutput().connect(mixer.inputA);
            // restore
            unit.setEnabled(wasEnabled);
        }

        addPort(amplitude = mixer.inputB, "Amplitude");
        amplitude.setup(0.0001, 0.4, 2.0);
        exportAllInputPorts();
    }

    /**
     * Connect a PassThrough unit to the input ports of the voices so that they can be controlled
     * together using a single port. Note that this will prevent their individual use. So the
     * "Frequency" and "Amplitude" ports are excluded. Note that this method is a bit funky and is
     * likely to change.
     */
    public void exportAllInputPorts() {
        // Iterate through the ports.
        for (UnitPort port : voices[0].getUnitGenerator().getPorts()) {
            if (port instanceof UnitInputPort) {
                UnitInputPort inputPort = (UnitInputPort) port;
                String voicePortName = inputPort.getName();
                // FIXME Need better way to identify ports that are per note.
                if (!voicePortName.equals("Frequency") && !voicePortName.equals("Amplitude")) {
                    exportNamedInputPort(voicePortName);
                }
            }
        }
    }

    /**
     * Create a UnitInputPort for the circuit that is connected to the named port on each voice
     * through a PassThrough unit. This allows you to control all of the voices at once.
     *
     * @param portName
     * @see exportAllInputPorts
     */
    public void exportNamedInputPort(String portName) {
        UnitInputPort voicePort = null;
        PassThrough fanout = new PassThrough();
        for (UnitVoice voice : voices) {
            voicePort = (UnitInputPort) voice.getUnitGenerator().getPortByName(portName);
            fanout.output.connect(voicePort);
        }
        if (voicePort != null) {
            addPort(fanout.input, portName);
            fanout.input.setup(voicePort);
        }
    }

    @Override
    public UnitOutputPort getOutput() {
        return mixer.output;
    }

    @Override
    public void usePreset(int presetIndex) {
        usePreset(presetIndex, getSynthesisEngine().createTimeStamp());
    }

    // FIXME - no timestamp on UnitVoice
    @Override
    public void usePreset(int presetIndex, TimeStamp timeStamp) {
        // Apply preset to all voices.
        for (UnitVoice voice : voices) {
            voice.usePreset(presetIndex);
        }
        // Then copy values from first voice to instrument.
        for (UnitPort port : voices[0].getUnitGenerator().getPorts()) {
            if (port instanceof UnitInputPort) {
                UnitInputPort inputPort = (UnitInputPort) port;
                // FIXME Need better way to identify ports that are per note.
                UnitInputPort fanPort = (UnitInputPort) getPortByName(inputPort.getName());
                if ((fanPort != null) && (fanPort != amplitude)) {
                    fanPort.set(inputPort.get());
                }
            }
        }
    }

    @Override
    public void noteOn(int tag, double frequency, double amplitude, TimeStamp timeStamp) {
        voiceAllocator.noteOn(tag, frequency, amplitude, timeStamp);
    }

    @Override
    public void noteOff(int tag, TimeStamp timeStamp) {
        voiceAllocator.noteOff(tag, timeStamp);
    }

    @Override
    public void setPort(int tag, String portName, double value, TimeStamp timeStamp) {
        voiceAllocator.setPort(tag, portName, value, timeStamp);
    }

    @Override
    public void allNotesOff(TimeStamp timeStamp) {
        voiceAllocator.allNotesOff(timeStamp);
    }

    public synchronized boolean isOn(int tag) {
        return voiceAllocator.isOn(tag);
    }
}
