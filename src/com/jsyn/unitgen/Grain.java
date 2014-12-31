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
 * A single Grain that is normally created and controlled by a GrainFarm.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class Grain implements GrainEnvelope {
    private double frameRate;
    private double amplitude = 1.0;

    private GrainSource source;
    private GrainEnvelope envelope;

    public Grain(GrainSource source, GrainEnvelope envelope) {
        this.source = source;
        this.envelope = envelope;
    }

    @Override
    public double next() {
        if (envelope.hasMoreValues()) {
            double env = envelope.next();
            return source.next() * env * amplitude;
        } else {
            return 0.0;
        }
    }

    @Override
    public boolean hasMoreValues() {
        return envelope.hasMoreValues();
    }

    @Override
    public void reset() {
        source.reset();
        envelope.reset();
    }

    public void setRate(double rate) {
        source.setRate(rate);
    }

    @Override
    public void setDuration(double duration) {
        envelope.setDuration(duration);
    }

    @Override
    public double getFrameRate() {
        return frameRate;
    }

    @Override
    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
        source.setFrameRate(frameRate);
        envelope.setFrameRate(frameRate);
    }

    public double getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    public GrainSource getSource() {
        return source;
    }
}
