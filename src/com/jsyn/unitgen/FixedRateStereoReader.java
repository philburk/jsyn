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
 * Simple stereo sample player. Play one sample per audio frame with no interpolation.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class FixedRateStereoReader extends SequentialDataReader {
    public FixedRateStereoReader() {
        addPort(output = new UnitOutputPort(2, "Output"));
        dataQueue.setNumChannels(2);
    }

    @Override
    public void generate(int start, int limit) {
        double[] amplitudes = amplitude.getValues();
        double[] output0s = output.getValues(0);
        double[] output1s = output.getValues(1);

        for (int i = start; i < limit; i++) {
            if (dataQueue.hasMore()) {
                dataQueue.beginFrame(getFramePeriod());
                double fdata = dataQueue.readCurrentChannelDouble(0);
                // System.out.println("SampleReader_16F2: left = " + fdata );
                double amp = amplitudes[i];
                output0s[i] = fdata * amp;
                fdata = dataQueue.readCurrentChannelDouble(1);
                // System.out.println("SampleReader_16F2: right = " + fdata );
                output1s[i] = fdata * amp;
                dataQueue.endFrame();
            } else {
                output0s[i] = 0.0;
                output1s[i] = 0.0;
                if (dataQueue.testAndClearAutoStop()) {
                    autoStop();
                }
            }
            dataQueue.firePendingCallbacks();
        }
    }
}
