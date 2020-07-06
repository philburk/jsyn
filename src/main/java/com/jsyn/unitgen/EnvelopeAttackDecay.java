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

package com.jsyn.unitgen;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.UnitInputPort;

/**
 * Two stage Attack/Decay envelope that is triggered by an input level greater than THRESHOLD. This
 * does not sustain.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class EnvelopeAttackDecay extends UnitGate {
    public static final double THRESHOLD = 0.01;
    private static final double MIN_DURATION = (1.0 / 100000.0);

    /**
     * Time in seconds for the rising stage of the envelope to go from 0.0 to 1.0. The attack is a
     * linear ramp.
     */
    public UnitInputPort attack;
    /**
     * Time in seconds for the falling stage to go from 0 dB to -90 dB.
     */
    public UnitInputPort decay;

    public UnitInputPort amplitude;

    private enum State {
        IDLE, ATTACKING, DECAYING
    }

    private State state = State.IDLE;
    private double scaler = 1.0;
    private double level;
    private double increment;

    public EnvelopeAttackDecay() {
        super();
        addPort(attack = new UnitInputPort("Attack"));
        attack.setup(0.001, 0.05, 8.0);
        addPort(decay = new UnitInputPort("Decay"));
        decay.setup(0.001, 0.2, 8.0);
        addPort(amplitude = new UnitInputPort("Amplitude", 1.0));
        startIdle();
    }

    public void export(Circuit circuit, String prefix) {
        circuit.addPort(attack, prefix + attack.getName());
        circuit.addPort(decay, prefix + decay.getName());
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit;) {
            boolean triggered = input.checkGate(i);
            switch (state) {
                case IDLE:
                    for (; i < limit; i++) {
                        outputs[i] = level;
                        if (triggered) {
                            startAttack(i);
                            break;
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
                            startDecay(i);
                            break;
                        }
                        outputs[i] = level * amplitudes[i];
                    }
                    break;
                case DECAYING:
                    for (; i < limit; i++) {
                        outputs[i] = level * amplitudes[i];
                        level *= scaler;
                        if (triggered) {
                            startAttack(i);
                            break;
                        } else if (level < SynthesisEngine.DB90) {
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

    private void startAttack(int i) {
        double[] attacks = attack.getValues();
        double duration = attacks[i];
        if (duration < MIN_DURATION) {
            level = 1.0;
            startDecay(i);
        } else {
            // assume going from 0.0 to 1.0 even if retriggering
            increment = getFramePeriod() / duration;
            state = State.ATTACKING;
        }
    }

    private void startDecay(int i) {
        double[] decays = decay.getValues();
        double duration = decays[i];
        if (duration < MIN_DURATION) {
            startIdle();
        } else {
            scaler = getSynthesisEngine().convertTimeToExponentialScaler(duration);
            state = State.DECAYING;
        }
    }

}
