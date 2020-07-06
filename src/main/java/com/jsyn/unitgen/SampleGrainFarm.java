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

import com.jsyn.data.FloatSample;
import com.jsyn.ports.UnitInputPort;

/**
 * A GrainFarm that uses a FloatSample as source material. In this example we load a FloatSample for
 * use as a source material.
 *
 * <pre><code>
	synth.add(sampleGrainFarm = new SampleGrainFarm());
	// Load a sample that we want to "granulate" from a file.
	sample = SampleLoader.loadFloatSample(sampleFile);
	sampleGrainFarm.setSample(sample);
	// Use a ramp to move smoothly within the file.
	synth.add(ramp = new ContinuousRamp());
	ramp.output.connect(sampleGrainFarm.position);
</code></pre>
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class SampleGrainFarm extends GrainFarm {
    private FloatSample sample;
    public UnitInputPort position;
    public UnitInputPort positionRange;

    public SampleGrainFarm() {
        super();
        addPort(position = new UnitInputPort("Position", 0.0));
        addPort(positionRange = new UnitInputPort("PositionRange", 0.0));
    }

    @Override
    public void allocate(int numGrains) {
        Grain[] grainArray = new Grain[numGrains];
        for (int i = 0; i < numGrains; i++) {
            Grain grain = new Grain(new SampleGrainSource(), new RaisedCosineEnvelope());
            grainArray[i] = grain;
        }
        setGrainArray(grainArray);
    }

    @Override
    public void setupGrain(Grain grain, int i) {
        SampleGrainSource sampleGrainSource = (SampleGrainSource) grain.getSource();
        sampleGrainSource.setSample(sample);
        sampleGrainSource.setPosition(position.getValues()[i]);
        sampleGrainSource.setPositionRange(positionRange.getValues()[i]);
        super.setupGrain(grain, i);
    }

    public void setSample(FloatSample sample) {
        this.sample = sample;
    }
}
