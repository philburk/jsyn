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

import com.jsyn.data.FloatSample;

public class SampleGrainSource extends GrainCommon implements GrainSource {
    private FloatSample sample;
    private double position; // ranges from -1.0 to 1.0
    private double positionRange;
    private double phase; // ranges from 0.0 to 1.0
    private double phaseIncrement;
    private int numFramesGuarded;
    private static final double MAX_PHASE = 0.9999999999;

    @Override
    public double next() {
        phase += phaseIncrement;
        if (phase > MAX_PHASE) {
            phase = MAX_PHASE;
        }
        double fractionalIndex = phase * numFramesGuarded;
        return sample.interpolate(fractionalIndex);
    }

    @Override
    public void setRate(double rate) {
        phaseIncrement = rate * sample.getFrameRate() / (getFrameRate() * numFramesGuarded);
    }

    public void setSample(FloatSample sample) {
        this.sample = sample;
        numFramesGuarded = sample.getNumFrames() - 1;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    @Override
    public void reset() {
        double randomPosition = position + (positionRange * (Math.random() - 0.5));
        phase = (randomPosition * 0.5) + 0.5;
        if (phase < 0.0) {
            phase = 0.0;
        } else if (phase > MAX_PHASE) {
            phase = MAX_PHASE;
        }
    }

    public void setPositionRange(double positionRange) {
        this.positionRange = positionRange;
    }

}
