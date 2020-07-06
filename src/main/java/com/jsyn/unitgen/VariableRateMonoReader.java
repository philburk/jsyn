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
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.data.ShortSample;
import com.jsyn.ports.UnitOutputPort;

/**
 * This reader can play any SequentialData and will interpolate between adjacent values. It can play
 * both {@link SegmentedEnvelope envelopes} and {@link FloatSample samples}.
 *
 * <pre><code>
	// Queue an envelope to the dataQueue port.
	ampEnv.dataQueue.queue(ampEnvelope);
</code></pre>
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @see FloatSample
 * @see ShortSample
 * @see SegmentedEnvelope
 */
public class VariableRateMonoReader extends VariableRateDataReader {
    private double phase; // ranges from 0.0 to 1.0
    private double baseIncrement;
    private double source;
    private double current;
    private double target;
    private boolean starved;
    private boolean ranout;

    public VariableRateMonoReader() {
        super();
        addPort(output = new UnitOutputPort("Output"));
        starved = true;
        baseIncrement = 1.0;
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues();
        double[] rates = rate.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            // Decrement phase and advance through queued data until phase back
            // in range.
            if (phase >= 1.0) {
                while (phase >= 1.0) {
                    source = target;
                    phase -= 1.0;
                    baseIncrement = advanceToNextFrame();
                }
            } else if ((i == 0) && (starved || !dataQueue.isTargetValid())) {
                // A starved condition can only be cured at the beginning of a
                // block.
                source = target = current;
                phase = 0.0;
                baseIncrement = advanceToNextFrame();
            }

            // Interpolate along line segment.
            current = ((target - source) * phase) + source;
            outputs[i] = current * amplitudes[i];

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
        // Fire callbacks before getting next value because we already got the
        // target value previously.
        dataQueue.firePendingCallbacks();
        if (dataQueue.hasMore()) {
            starved = false;
            target = dataQueue.readNextMonoDouble(getFramePeriod());

            // calculate phase increment;
            return getFramePeriod() * dataQueue.getNormalizedRate();
        } else {
            starved = true;
            ranout = true;
            phase = 0.0;
            return 0.0;
        }
    }

}
