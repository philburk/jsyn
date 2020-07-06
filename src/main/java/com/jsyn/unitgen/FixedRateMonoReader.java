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

import com.jsyn.ports.UnitOutputPort;

/**
 * Simple sample player. Play one sample per audio frame with no interpolation.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class FixedRateMonoReader extends SequentialDataReader {

    public FixedRateMonoReader() {
        addPort(output = new UnitOutputPort());
    }

    @Override
    public void generate(int start, int limit) {

        double[] amplitudes = amplitude.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            if (dataQueue.hasMore()) {
                double fdata = dataQueue.readNextMonoDouble(getFramePeriod());
                outputs[i] = fdata * amplitudes[i];
            } else {
                outputs[i] = 0.0;
                if (dataQueue.testAndClearAutoStop()) {
                    autoStop();
                }
            }
            dataQueue.firePendingCallbacks();
        }
    }

}
