/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

import com.jsyn.util.PseudoRandom;

/**
 * RedNoise unit. This unit interpolates straight line segments between pseudo-random numbers to
 * produce "red" noise. It is a grittier alternative to the white generator WhiteNoise. It is also
 * useful as a slowly changing random control generator for natural sounds. Frequency port controls
 * the number of times per second that a new random number is chosen.
 * 
 * @author (C) 1997 Phil Burk, SoftSynth.com
 * @see WhiteNoise
 */
public class RedNoise extends UnitOscillator {
    private PseudoRandom randomNum;
    protected double prevNoise, currNoise;

    /* Define Unit Ports used by connect() and set(). */
    public RedNoise() {
        super();
        randomNum = new PseudoRandom();
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues();
        double[] frequencies = frequency.getValues();
        double[] outputs = output.getValues();
        double currPhase = phase.getValue();
        double phaseIncrement, currOutput;

        double framePeriod = getFramePeriod();

        for (int i = start; i < limit; i++) {
            // compute phase
            phaseIncrement = frequencies[i] * framePeriod;

            // verify that phase is within minimums and is not negative
            if (phaseIncrement < 0.0) {
                phaseIncrement = 0.0 - phaseIncrement;
            }
            if (phaseIncrement > 1.0) {
                phaseIncrement = 1.0;
            }

            currPhase += phaseIncrement;

            // calculate new random whenever phase passes 1.0
            if (currPhase > 1.0) {
                prevNoise = currNoise;
                currNoise = randomNum.nextRandomDouble();
                // reset phase for interpolation
                currPhase -= 1.0;
            }

            // interpolate current
            currOutput = prevNoise + (currPhase * (currNoise - prevNoise));
            outputs[i] = currOutput * amplitudes[i];
        }

        // store new phase
        phase.setValue(currPhase);
    }
}
