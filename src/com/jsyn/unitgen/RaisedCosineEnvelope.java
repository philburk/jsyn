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

/**
 * An envelope that can be used in a GrainFarm to shape the amplitude of a Grain. The envelope
 * starts at 0.0, rises to 1.0, then returns to 0.0 following a cosine curve.
 * 
 * <pre>
 * output = 0.5 - (0.5 * cos(phase))
 * </pre>
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 * @see GrainFarm
 */
public class RaisedCosineEnvelope extends GrainCommon implements GrainEnvelope {
    protected double phase;
    protected double phaseIncrement;

    public RaisedCosineEnvelope() {
        setFrameRate(44100);
        setDuration(0.1);
    }

    /**
     * @return next value of the envelope.
     */
    @Override
    public double next() {
        phase += phaseIncrement;
        if (phase > (2.0 * Math.PI)) {
            return 0.0;
        } else {
            return 0.5 - (0.5 * Math.cos(phase)); // TODO optimize using Taylor expansion
        }
    }

    /**
     * @return true if there are more envelope values left.
     */
    @Override
    public boolean hasMoreValues() {
        return (phase < (2.0 * Math.PI));
    }

    /**
     * Reset the envelope back to the beginning.
     */
    @Override
    public void reset() {
        phase = 0.0;
    }

    @Override
    public void setDuration(double duration) {
        phaseIncrement = 2.0 * Math.PI / (getFrameRate() * duration);
    }

}
