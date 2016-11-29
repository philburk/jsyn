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

package com.jsyn.unitgen;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;

/**
 * Six stage envelope similar to an ADSR. DAHDSR is like an ADSR but with an additional Delay stage
 * before the attack, and a Hold stage after the Attack. If Delay and Hold are both set to zero then
 * it will act like an ADSR. The envelope is triggered when the input goes above THRESHOLD. The
 * envelope is released when the input goes below THRESHOLD. The THRESHOLD is currently 0.01 but may
 * change so it would be best to use an input signal that went from 0 to 1. Mathematically an
 * exponential Release will never reach 0.0. But when it reaches -96 dB the DAHDSR just sets its
 * output to 0.0 and stops. There is an example program in the ZIP archive called HearDAHDSR. It
 * drives a DAHDSR with a square wave.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 * @see SegmentedEnvelope
 */
public class EnvelopeDAHDSR extends UnitGate implements UnitSource {
    private static final double MIN_DURATION = (1.0 / 100000.0);

    /**
     * Time in seconds for first stage of the envelope, before the attack. Typically zero.
     */
    public UnitInputPort delay;
    /**
     * Time in seconds for the rising stage of the envelope to go from 0.0 to 1.0. The attack is a
     * linear ramp.
     */
    public UnitInputPort attack;
    /** Time in seconds for the plateau between the attack and decay stages. */
    public UnitInputPort hold;
    /**
     * Time in seconds for the falling stage to go from 0 dB to -90 dB. The decay stage will stop at
     * the sustain level. But we calculate the time to fall to -90 dB so that the decay
     * <em>rate</em> will be unaffected by the sustain level.
     */
    public UnitInputPort decay;
    /**
     * Level for the sustain stage. The envelope will hold here until the input goes to zero or
     * less. This should be set between 0.0 and 1.0.
     */
    public UnitInputPort sustain;
    /**
     * Time in seconds to go from 0 dB to -90 dB. This stage is triggered when the input goes to
     * zero or less. The release stage will start from the sustain level. But we calculate the time
     * to fall from full amplitude so that the release <em>rate</em> will be unaffected by the
     * sustain level.
     */
    public UnitInputPort release;
    public UnitInputPort amplitude;

    enum State {
        IDLE, DELAYING, ATTACKING, HOLDING, DECAYING, SUSTAINING, RELEASING
    }

    private State state = State.IDLE;
    private double countdown;
    private double scaler = 1.0;
    private double level;
    private double increment;

    public EnvelopeDAHDSR() {
        super();
        addPort(delay = new UnitInputPort("Delay", 0.0));
        delay.setup(0.0, 0.0, 2.0);
        addPort(attack = new UnitInputPort("Attack", 0.1));
        attack.setup(0.01, 0.1, 8.0);
        addPort(hold = new UnitInputPort("Hold", 0.0));
        hold.setup(0.0, 0.0, 2.0);
        addPort(decay = new UnitInputPort("Decay", 0.2));
        decay.setup(0.01, 0.2, 8.0);
        addPort(sustain = new UnitInputPort("Sustain", 0.5));
        sustain.setup(0.0, 0.5, 1.0);
        addPort(release = new UnitInputPort("Release", 0.3));
        release.setup(0.01, 0.3, 8.0);
        addPort(amplitude = new UnitInputPort("Amplitude", 1.0));
    }

    @Override
    public void generate(int start, int limit) {
        double[] sustains = sustain.getValues();
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit;) {
            boolean triggered = input.checkGate(i);
            switch (state) {
                case IDLE:
                    for (; i < limit; i++) {
                        outputs[i] = level * amplitudes[i];
                        if (triggered) {
                            startDelay(i);
                            break;
                        }
                    }
                    break;

                case DELAYING:
                    for (; i < limit; i++) {
                        outputs[i] = level * amplitudes[i];
                        if (input.isOff()) {
                            startRelease(i);
                            break;
                        } else {
                            countdown -= 1;
                            if (countdown <= 0) {
                                startAttack(i);
                                break;
                            }
                        }
                    }
                    break;

                case ATTACKING:
                    for (; i < limit; i++) {
                        // Increment first so we can render fast attacks.
                        level += increment;
                        if (level >= 1.0) {
                            level = 1.0;
                            outputs[i] = level * amplitudes[i];
                            startHold(i);
                            break;
                        } else {
                            outputs[i] = level * amplitudes[i];
                            if (input.isOff()) {
                                startRelease(i);
                                break;
                            }
                        }
                    }
                    break;

                case HOLDING:
                    for (; i < limit; i++) {
                        outputs[i] = amplitudes[i]; // level is 1.0
                        countdown -= 1;
                        if (countdown <= 0) {
                            startDecay(i);
                            break;
                        } else if (input.isOff()) {
                            startRelease(i);
                            break;
                        }
                    }
                    break;

                case DECAYING:
                    for (; i < limit; i++) {
                        outputs[i] = level * amplitudes[i];
                        level *= scaler; // exponential decay
                        if (triggered) {
                            startDelay(i);
                            break;
                        } else if (level < sustains[i]) {
                            level = sustains[i];
                            startSustain(i);
                            break;
                        } else if (level < SynthesisEngine.DB96) {
                            input.checkAutoDisable();
                            startIdle();
                            break;
                        } else if (input.isOff()) {
                            startRelease(i);
                            break;
                        }
                    }
                    break;

                case SUSTAINING:
                    for (; i < limit; i++) {
                        level = sustains[i];
                        outputs[i] = level * amplitudes[i];
                        if (triggered) {
                            startDelay(i);
                            break;
                        } else if (input.isOff()) {
                            startRelease(i);
                            break;
                        }
                    }
                    break;

                case RELEASING:
                    for (; i < limit; i++) {
                        outputs[i] = level * amplitudes[i];
                        level *= scaler; // exponential decay
                        if (triggered) {
                            startDelay(i);
                            break;
                        } else if (level < SynthesisEngine.DB96) {
                            input.checkAutoDisable();
                            startIdle();
                            break;
                        }
                    }
                    break;
            }
        }
    }

    private void startIdle() {
        state = State.IDLE;
        level = 0.0;
    }

    private void startDelay(int i) {
        double[] delays = delay.getValues();
        if (delays[i] <= 0.0) {
            startAttack(i);
        } else {
            countdown = (int) (delays[i] * getFrameRate());
            state = State.DELAYING;
        }
    }

    private void startAttack(int i) {
        double[] attacks = attack.getValues();
        double duration = attacks[i];
        if (duration < MIN_DURATION) {
            level = 1.0;
            startHold(i);
        } else {
            increment = getFramePeriod() / duration;
            state = State.ATTACKING;
        }
    }

    private void startHold(int i) {
        double[] holds = hold.getValues();
        if (holds[i] <= 0.0) {
            startDecay(i);
        } else {
            countdown = (int) (holds[i] * getFrameRate());
            state = State.HOLDING;
        }
    }

    private void startDecay(int i) {
        double[] decays = decay.getValues();
        double duration = decays[i];
        if (duration < MIN_DURATION) {
            startSustain(i);
        } else {
            scaler = getSynthesisEngine().convertTimeToExponentialScaler(duration);
            state = State.DECAYING;
        }
    }

    private void startSustain(int i) {
        state = State.SUSTAINING;
    }

    private void startRelease(int i) {
        double[] releases = release.getValues();
        double duration = releases[i];
        if (duration < MIN_DURATION) {
            duration = MIN_DURATION;
        }
        scaler = getSynthesisEngine().convertTimeToExponentialScaler(duration);
        state = State.RELEASING;
    }

    public void export(Circuit circuit, String prefix) {
        circuit.addPort(attack, prefix + attack.getName());
        circuit.addPort(decay, prefix + decay.getName());
        circuit.addPort(sustain, prefix + sustain.getName());
        circuit.addPort(release, prefix + release.getName());
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

}
