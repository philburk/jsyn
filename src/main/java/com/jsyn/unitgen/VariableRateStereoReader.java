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

package com.jsyn.unitgen;

import com.jsyn.ports.UnitOutputPort;

/**
 * This reader can play any SequentialData and will interpolate between adjacent values. It can play
 * both envelopes and samples.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class VariableRateStereoReader extends VariableRateDataReader {
    private double phase;
    private double baseIncrement;
    private double source0;
    private double current0;
    private double target0;
    private double source1;
    private double current1;
    private double target1;
    private boolean starved;
    private boolean ranout;

    public VariableRateStereoReader() {
        dataQueue.setNumChannels(2);
        addPort(output = new UnitOutputPort(2, "Output"));
        starved = true;
        baseIncrement = 1.0;
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues();
        double[] rates = rate.getValues();
        double[] output0s = output.getValues(0);
        double[] output1s = output.getValues(1);

        for (int i = start; i < limit; i++) {
            // Decrement phase and advance through queued data until phase back
            // in range.
            if (phase >= 1.0) {
                while (phase >= 1.0) {
                    source0 = target0;
                    source1 = target1;
                    phase -= 1.0;
                    baseIncrement = advanceToNextFrame();
                }
            } else if ((i == 0) && (starved || !dataQueue.isTargetValid())) {
                // A starved condition can only be cured at the beginning of a block.
                source0 = target0 = current0;
                source1 = target1 = current1;
                phase = 0.0;
                baseIncrement = advanceToNextFrame();
            }

            // Interpolate along line segment.
            current0 = ((target0 - source0) * phase) + source0;
            output0s[i] = current0 * amplitudes[i];
            current1 = ((target1 - source1) * phase) + source1;
            output1s[i] = current1 * amplitudes[i];

            double phaseIncrement = baseIncrement * rates[i];
            phase += limitPhaseIncrement(phaseIncrement);
        }

        if (ranout) {
            ranout = false;
            if (dataQueue.testAndClearAutoStop()) {
                autoStop();
            }
        }
    }

    public double limitPhaseIncrement(double phaseIncrement) {
        return phaseIncrement;
    }

    private double advanceToNextFrame() {
        dataQueue.firePendingCallbacks();
        if (dataQueue.hasMore()) {
            starved = false;

            dataQueue.beginFrame(getFramePeriod());
            target0 = dataQueue.readCurrentChannelDouble(0);
            target1 = dataQueue.readCurrentChannelDouble(1);
            dataQueue.endFrame();

            // calculate phase increment;
            return synthesisEngine.getFramePeriod() * dataQueue.getNormalizedRate();
        } else {
            starved = true;
            ranout = true;
            phase = 0.0;
            return 0.0;
        }
    }

}
