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

/**
 * Similar to MixerMono but the gain and amplitude ports are smoothed using short linear ramps. So
 * you can control them with knobs and not hear any zipper noise.
 * 
 * @author Phil Burk (C) 2014 Mobileer Inc
 */
public class MixerMonoRamped extends MixerMono {
    private Unzipper[] unzippers;
    private Unzipper amplitudeUnzipper;

    public MixerMonoRamped(int numInputs) {
        super(numInputs);
        unzippers = new Unzipper[numInputs];
        for (int i = 0; i < numInputs; i++) {
            unzippers[i] = new Unzipper();
        }
        amplitudeUnzipper = new Unzipper();
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues(0);
        double[] outputs = output.getValues(0);
        for (int i = start; i < limit; i++) {
            double sum = 0;
            for (int n = 0; n < input.getNumParts(); n++) {
                double[] inputs = input.getValues(n);
                double[] gains = gain.getValues(n);
                double smoothGain = unzippers[n].smooth(gains[i]);
                sum += inputs[i] * smoothGain;
            }
            outputs[i] = sum * amplitudeUnzipper.smooth(amplitudes[i]);
        }
    }

}
