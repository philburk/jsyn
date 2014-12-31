/*
 * Copyright 2014 Phil Burk, Mobileer Inc
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

import com.jsyn.ports.UnitInputPort;

/**
 * Mixer with monophonic inputs and two channels of output. Each signal can be panned left or right
 * using an equal power curve. The "left" signal will be on output part zero. The "right" signal
 * will be on output part one.
 * 
 * @author Phil Burk (C) 2014 Mobileer Inc
 * @see MixerMono
 * @see MixerStereoRamped
 */
public class MixerStereo extends MixerMono {
    /**
     * Set to -1.0 for all left channel, 0.0 for center, or +1.0 for all right. Or anywhere in
     * between.
     */
    public UnitInputPort pan;
    protected PanTracker[] panTrackers;

    static class PanTracker {
        double previousPan = Double.MAX_VALUE; // so we update immediately
        double leftGain;
        double rightGain;

        public void update(double pan) {
            if (pan != previousPan) {
                // fastSine range is -1.0 to +1.0 for full cycle.
                // We want a quarter cycle. So map -1.0 to +1.0 into 0.0 to 0.5
                double phase = pan * 0.25 + 0.25;
                leftGain = SineOscillator.fastSin(0.5 - phase);
                rightGain = SineOscillator.fastSin(phase);
                previousPan = pan;
            }
        }
    }

    public MixerStereo(int numInputs) {
        super(numInputs);
        addPort(pan = new UnitInputPort(numInputs, "Pan"));
        pan.setup(-1.0, 0.0, 1.0);
        panTrackers = new PanTracker[numInputs];
        for (int i = 0; i < numInputs; i++) {
            panTrackers[i] = new PanTracker();
        }
    }

    @Override
    public int getNumOutputs() {
        return 2;
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues(0);
        double[] outputs0 = output.getValues(0);
        double[] outputs1 = output.getValues(1);
        for (int i = start; i < limit; i++) {
            double sum0 = 0.0;
            double sum1 = 0.0;
            for (int n = 0; n < input.getNumParts(); n++) {
                double[] inputs = input.getValues(n);
                double[] gains = gain.getValues(n);
                double[] pans = pan.getValues(n);
                PanTracker panTracker = panTrackers[n];
                panTracker.update(pans[i]);
                double scaledInput = inputs[i] * gains[i];
                sum0 += scaledInput * panTracker.leftGain;
                sum1 += scaledInput * panTracker.rightGain;
            }
            double amp = amplitudes[i];
            outputs0[i] = sum0 * amp;
            outputs1[i] = sum1 * amp;
        }
    }

}
