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
 * Simple stereo sample writer. Write two samples per audio frame with no interpolation. This can be
 * used to record audio or to build delay lines.
 *
 * Note that you must call start() on this unit because it does not have an output for pulling data.
 *
 * @see FixedRateMonoWriter
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class FixedRateStereoWriter extends SequentialDataWriter {

    public FixedRateStereoWriter() {
        addPort(input = new UnitInputPort(2, "Input"));
        dataQueue.setNumChannels(2);
    }

    @Override
    public void generate(int start, int limit) {
        double[] input0s = input.getValues(0);
        double[] input1s = input.getValues(1);

        for (int i = start; i < limit; i++) {
            if (dataQueue.hasMore()) {
                dataQueue.beginFrame(getFramePeriod());
                double value = input0s[i];
                dataQueue.writeCurrentChannelDouble(0, value);
                value = input1s[i];
                dataQueue.writeCurrentChannelDouble(1, value);
                dataQueue.endFrame();
            } else {
                if (dataQueue.testAndClearAutoStop()) {
                    autoStop();
                }
            }
            dataQueue.firePendingCallbacks();
        }

    }

}
