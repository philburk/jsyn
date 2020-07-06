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
 * Similar to MixerStereo but the gain, pan and amplitude ports are smoothed using short linear
 * ramps. So you can control them with knobs and not hear any zipper noise.
 * 
 * @author Phil Burk (C) 2014 Mobileer Inc
 */
public class MixerStereoRamped extends MixerStereo {
    private Unzipper[] gainUnzippers;
    private Unzipper[] panUnzippers;
    private Unzipper amplitudeUnzipper;

    public MixerStereoRamped(int numInputs) {
        super(numInputs);
        gainUnzippers = new Unzipper[numInputs];
        for (int i = 0; i < numInputs; i++) {
            gainUnzippers[i] = new Unzipper();
        }
        panUnzippers = new Unzipper[numInputs];
        for (int i = 0; i < numInputs; i++) {
            panUnzippers[i] = new Unzipper();
        }
        amplitudeUnzipper = new Unzipper();
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues(0);
        double[] outputs0 = output.getValues(0);
        double[] outputs1 = output.getValues(1);
        for (int i = start; i < limit; i++) {
            double sum0 = 0;
            double sum1 = 0;
            for (int n = 0; n < input.getNumParts(); n++) {
                double[] inputs = input.getValues(n);
                double[] gains = gain.getValues(n);
                double[] pans = pan.getValues(n);

                PanTracker panTracker = panTrackers[n];
                double smoothPan = panUnzippers[n].smooth(pans[i]);
                panTracker.update(smoothPan);

                double smoothGain = gainUnzippers[n].smooth(gains[i]);
                double scaledInput = inputs[i] * smoothGain;
                sum0 += scaledInput * panTracker.leftGain;
                sum1 += scaledInput * panTracker.rightGain;
            }
            double amp = amplitudeUnzipper.smooth(amplitudes[i]);
            outputs0[i] = sum0 * amp;
            outputs1[i] = sum1 * amp;
        }
    }

}
