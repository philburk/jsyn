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

import com.jsyn.ports.UnitInputPort;

/**
 * Simple sample writer. Write one sample per audio frame with no interpolation. This can be used to
 * record audio or to build delay lines.
 *
 * Note that you must call start() on this unit because it does not have an output for pulling data.
 *
 * @see FixedRateStereoWriter
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class FixedRateMonoWriter extends SequentialDataWriter {

    public FixedRateMonoWriter() {
        addPort(input = new UnitInputPort("Input"));
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();

        for (int i = start; i < limit; i++) {
            if (dataQueue.hasMore()) {
                double value = inputs[i];
                dataQueue.writeNextDouble(value);
            } else {
                if (dataQueue.testAndClearAutoStop()) {
                    autoStop();
                }
            }
            dataQueue.firePendingCallbacks();
        }

    }

}
