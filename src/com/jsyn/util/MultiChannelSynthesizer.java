/*
 * Copyright 2016 Phil Burk, Mobileer Inc
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

import com.jsyn.Synthesizer;
import com.jsyn.engine.SynthesisEngine;
import com.jsyn.midi.MidiConstants;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.ExponentialRamp;
import com.jsyn.unitgen.LinearRamp;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.Pan;
import com.jsyn.unitgen.PowerOfTwo;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.TwoInDualOut;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitVoice;
import com.softsynth.math.AudioMath;
import com.softsynth.shared.time.TimeStamp;

/**
 * General purpose synthesizer with "channels"
 * that could be used to implement a MIDI synthesizer.
 *
 * Each channel has:
 * <pre><code>
 * lfo -&gt; pitchToLinear -&gt; [VOICES] -&gt; volume* -&gt; panner
 * bend --/
 * </code></pre>
 *
 * Note: this class is experimental and subject to change.
 *
 * @author Phil Burk (C) 2016 Mobileer Inc
 */
public class MultiChannelSynthesizer {
    private Synthesizer synth;
    private TwoInDualOut outputUnit;
    private ChannelContext[] channels;
    private final static int MAX_VELOCITY = 127;
    private double mMasterAmplitude = 0.25;

    private class ChannelGroupContext {
        private VoiceDescription voiceDescription;
        private UnitVoice[] voices;
        private VoiceAllocator allocator;

        ChannelGroupContext(int numVoices, VoiceDescription voiceDescription) {
            this.voiceDescription = voiceDescription;

            voices = new UnitVoice[numVoices];
            for (int i = 0; i < numVoices; i++) {
                UnitVoice voice = voiceDescription.createUnitVoice();
                UnitGenerator ugen = voice.getUnitGenerator();
                synth.add(ugen);
                voices[i] = voice;

            }
            allocator = new VoiceAllocator(voices);
        }
    }

    private class ChannelContext {
        private UnitOscillator lfo;
        private PowerOfTwo pitchToLinear;
        private LinearRamp timbreRamp;
        private LinearRamp pressureRamp;
        private ExponentialRamp volumeRamp;
        private Multiply volumeMultiplier;
        private Pan panner;
        private double vibratoRate = 5.0;
        private double bendRangeOctaves = 2.0 / 12.0;
        private int presetIndex;
        private ChannelGroupContext groupContext;
        VoiceOperation voiceOperation = new VoiceOperation() {
            @Override
            public void operate (UnitVoice voice) {
                voice.usePreset(presetIndex);
                connectVoice(voice);
            }
        };

        void setup(ChannelGroupContext groupContext) {
            this.groupContext = groupContext;
            synth.add(pitchToLinear = new PowerOfTwo());
            synth.add(lfo = new SineOscillator()); // TODO use a MorphingOscillator or switch
                                                   // between S&H etc.
            // Use a ramp to smooth out the timbre changes.
            // This helps reduce pops from changing filter cutoff too abruptly.
            synth.add(timbreRamp = new LinearRamp());
            timbreRamp.time.set(0.02);
            synth.add(pressureRamp = new LinearRamp());
            pressureRamp.time.set(0.02);
            synth.add(volumeRamp = new ExponentialRamp());
            volumeRamp.input.set(1.0);
            volumeRamp.time.set(0.02);
            synth.add(volumeMultiplier = new Multiply());
            synth.add(panner = new Pan());

            pitchToLinear.input.setValueAdded(true); // so we can sum pitch bend
            lfo.output.connect(pitchToLinear.input);
            lfo.amplitude.set(0.0);
            lfo.frequency.set(vibratoRate);

            volumeRamp.output.connect(volumeMultiplier.inputB);
            volumeMultiplier.output.connect(panner.input);
            panner.output.connect(0, outputUnit.inputA, 0); // Use MultiPassthrough
            panner.output.connect(1, outputUnit.inputB, 0);
        }

        private void connectVoice(UnitVoice voice) {
            UnitGenerator ugen = voice.getUnitGenerator();
            // Hook up some channel controllers to standard ports on the voice.
            UnitInputPort freqMod = (UnitInputPort) ugen
                    .getPortByName(UnitGenerator.PORT_NAME_FREQUENCY_SCALER);
            if (freqMod != null) {
                freqMod.disconnectAll();
                pitchToLinear.output.connect(freqMod);
            }
            UnitInputPort timbrePort = (UnitInputPort) ugen
                    .getPortByName(UnitGenerator.PORT_NAME_TIMBRE);
            if (timbrePort != null) {
                timbrePort.disconnectAll();
                timbreRamp.output.connect(timbrePort);
                timbreRamp.input.setup(timbrePort);
            }
            UnitInputPort pressurePort = (UnitInputPort) ugen
                    .getPortByName(UnitGenerator.PORT_NAME_PRESSURE);
            if (pressurePort != null) {
                pressurePort.disconnectAll();
                pressureRamp.output.connect(pressurePort);
                pressureRamp.input.setup(pressurePort);
            }
            voice.getOutput().disconnectAll();
            voice.getOutput().connect(volumeMultiplier.inputA); // mono mix all the voices
        }

        void programChange(int program) {
            int programWrapped = program % groupContext.voiceDescription.getPresetCount();
            String name = groupContext.voiceDescription.getPresetNames()[programWrapped];
            //System.out.println("Preset[" + program + "] = " + name);
            presetIndex = programWrapped;
        }

        void noteOff(int noteNumber, double amplitude) {
            groupContext.allocator.noteOff(noteNumber, synth.createTimeStamp());
        }

        void noteOff(int noteNumber, double amplitude, TimeStamp timeStamp) {
            groupContext.allocator.noteOff(noteNumber, timeStamp);
        }

        void noteOn(int noteNumber, double amplitude) {
            noteOn(noteNumber, amplitude, synth.createTimeStamp());
        }

        void noteOn(int noteNumber, double amplitude, TimeStamp timeStamp) {
            double frequency = AudioMath.pitchToFrequency(noteNumber);
            //System.out.println("noteOn(noteNumber) -> " + frequency + " Hz");
            groupContext.allocator.noteOn(noteNumber, frequency, amplitude, voiceOperation, timeStamp);
        }

        public void setPitchBend(double offset) {
            pitchToLinear.input.set(bendRangeOctaves * offset);
        }

        public void setBendRange(double semitones) {
            bendRangeOctaves = semitones / 12.0;
        }

        public void setVibratoDepth(double semitones) {
            lfo.amplitude.set(semitones);
        }

        public void setVolume(double volume) {
            double min = SynthesisEngine.DB96;
            double max = 1.0;
            double ratio = max / min;
            double value = min * Math.pow(ratio, volume);
            volumeRamp.input.set(value);
        }

        public void setPan(double pan) {
            panner.pan.set(pan);
        }

        /*
         * @param timbre normalized 0 to 1
         */
        public void setTimbre(double timbre) {
            double min = timbreRamp.input.getMinimum();
            double max = timbreRamp.input.getMaximum();
            double value = min + (timbre * (max - min));
            timbreRamp.input.set(value);
        }

        /*
         * @param pressure normalized 0 to 1
         */
        public void setPressure(double pressure) {
            double min = pressureRamp.input.getMinimum();
            double max = pressureRamp.input.getMaximum();
            double ratio = max / min;
            double value = min * Math.pow(ratio, pressure);
            pressureRamp.input.set(value);
        }
    }

    /**
     * Construct a synthesizer with a maximum of 16 channels like MIDI.
     */
    public MultiChannelSynthesizer() {
        this(MidiConstants.MAX_CHANNELS);
    }


    public MultiChannelSynthesizer(int maxChannels) {
        channels = new ChannelContext[maxChannels];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new ChannelContext();
        }
    }

    /**
     * Specify a VoiceDescription to use with multiple channels.
     *
     * @param synth
     * @param startChannel channel index is zero based
     * @param numChannels
     * @param voicesPerChannel
     * @param voiceDescription
     */
    public void setup(Synthesizer synth, int startChannel, int numChannels, int voicesPerChannel,
            VoiceDescription voiceDescription) {
        this.synth = synth;
        if (outputUnit == null) {
            synth.add(outputUnit = new TwoInDualOut());
        }
        ChannelGroupContext groupContext = new ChannelGroupContext(voicesPerChannel,
                voiceDescription);
        for (int i = 0; i < numChannels; i++) {
            channels[startChannel + i].setup(groupContext);
        }
    }

    public void programChange(int channel, int program) {
        ChannelContext channelContext = channels[channel];
        channelContext.programChange(program);
    }


    /**
     * Turn off a note.
     * @param channel
     * @param noteNumber
     * @param velocity between 0 and 127, will be scaled by masterAmplitude
     */
    public void noteOff(int channel, int noteNumber, int velocity) {
        double amplitude = velocity * (1.0 / MAX_VELOCITY);
        noteOff(channel, noteNumber, amplitude);
    }

    /**
     * Turn off a note.
     * @param channel
     * @param noteNumber
     * @param amplitude between 0 and 1.0, will be scaled by masterAmplitude
     */
    public void noteOff(int channel, int noteNumber, double amplitude) {
        ChannelContext channelContext = channels[channel];
        channelContext.noteOff(noteNumber, amplitude * mMasterAmplitude);
    }

    /**
     * Turn off a note.
     * @param channel
     * @param noteNumber
     * @param amplitude between 0 and 1.0, will be scaled by masterAmplitude
     */
    public void noteOff(int channel, int noteNumber, double amplitude, TimeStamp timeStamp) {
        ChannelContext channelContext = channels[channel];
        channelContext.noteOff(noteNumber, amplitude * mMasterAmplitude, timeStamp);
    }

    /**
     * Turn on a note.
     * @param channel
     * @param noteNumber
     * @param velocity between 0 and 127, will be scaled by masterAmplitude
     */
    public void noteOn(int channel, int noteNumber, int velocity) {
        double amplitude = velocity * (1.0 / MAX_VELOCITY);
        noteOn(channel, noteNumber, amplitude);
    }

    /**
     * Turn on a note.
     * @param channel
     * @param noteNumber
     * @param amplitude between 0 and 1.0, will be scaled by masterAmplitude
     */
    public void noteOn(int channel, int noteNumber, double amplitude, TimeStamp timeStamp) {
        ChannelContext channelContext = channels[channel];
        channelContext.noteOn(noteNumber, amplitude * mMasterAmplitude, timeStamp);
    }

    /**
     * Turn on a note.
     * @param channel
     * @param noteNumber
     * @param amplitude between 0 and 1.0, will be scaled by masterAmplitude
     */
    public void noteOn(int channel, int noteNumber, double amplitude) {
        ChannelContext channelContext = channels[channel];
        channelContext.noteOn(noteNumber, amplitude * mMasterAmplitude);
    }

    /**
     * Set a pitch offset that will be scaled by the range for the channel.
     *
     * @param channel
     * @param offset ranges from -1.0 to +1.0
     */
    public void setPitchBend(int channel, double offset) {
        //System.out.println("setPitchBend[" + channel + "] = " + offset);
        ChannelContext channelContext = channels[channel];
        channelContext.setPitchBend(offset);
    }

    public void setBendRange(int channel, double semitones) {
        ChannelContext channelContext = channels[channel];
        channelContext.setBendRange(semitones);
    }

    public void setPressure(int channel, double pressure) {
        ChannelContext channelContext = channels[channel];
        channelContext.setPressure(pressure);
    }

    public void setVibratoDepth(int channel, double semitones) {
        ChannelContext channelContext = channels[channel];
        channelContext.setVibratoDepth(semitones);
    }

    public void setTimbre(int channel, double timbre) {
        ChannelContext channelContext = channels[channel];
        channelContext.setTimbre(timbre);
    }

    /**
     * Set volume for entire channel.
     *
     * @param channel
     * @param volume normalized between 0.0 and 1.0
     */
    public void setVolume(int channel, double volume) {
        ChannelContext channelContext = channels[channel];
        channelContext.setVolume(volume);
    }

    /**
     * Pan from left to right.
     *
     * @param channel
     * @param pan ranges from -1.0 to +1.0
     */
    public void setPan(int channel, double pan) {
        ChannelContext channelContext = channels[channel];
        channelContext.setPan(pan);
    }

    /**
     * @return stereo output port
     */
    public UnitOutputPort getOutput() {
        return outputUnit.output;
    }

    /**
     * Set amplitude for a single voice when the velocity is 127.
     * @param masterAmplitude
     */
    public void setMasterAmplitude(double masterAmplitude) {
        mMasterAmplitude = masterAmplitude;
    }
    public double getMasterAmplitude() {
        return mMasterAmplitude;
    }
}
