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

import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;

/**
 * FourWayFade unit.
 * <P>
 * Mix inputs 0-3 based on the value of two fade ports. You can think of the four inputs arranged
 * clockwise as follows.
 * <P>
 *
 * <PRE>
 *      input[0] ---- input[1]
 *        |             |
 *        |             |
 *        |             |
 *      input[3] ---- input[2]
 * </PRE>
 *
 * The "fade" port has two parts. Fade[0] fades between the pair of inputs (0,3) and the pair of
 * inputs (1,2). Fade[1] fades between the pair of inputs (0,1) and the pair of inputs (3,2).
 *
 * <PRE>
 *    Fade[0]    Fade[1]    Output
 *      -1         -1       Input[3]
 *      -1         +1       Input[0]
 *      +1         -1       Input[2]
 *      +1         +1       Input[1]
 *
 *
 *      -----Fade[0]-----&gt;
 *
 *         A
 *         |
 *         |
 *      Fade[1]
 *         |
 *         |
 * </PRE>
 * <P>
 *
 * @author (C) 1997-2009 Phil Burk, Mobileer Inc
 */
public class FourWayFade extends UnitGenerator {
    public UnitInputPort input;
    public UnitInputPort fade;
    public UnitOutputPort output;

    /* Define Unit Ports used by connect() and set(). */
    public FourWayFade() {
        addPort(input = new UnitInputPort(4, "Input"));
        addPort(fade = new UnitInputPort(2, "Fade"));
        addPort(output = new UnitOutputPort());
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputAs = input.getValues(0);
        double[] inputBs = input.getValues(1);
        double[] inputCs = input.getValues(2);
        double[] inputDs = input.getValues(3);
        double[] fadeLRs = fade.getValues(0);
        double[] fadeFBs = fade.getValues(1);
        double[] outputs = output.getValues(0);

        for (int i = start; i < limit; i++) {
            // Scale and offset to 0.0 to 1.0 range.
            double gainLR = (fadeLRs[i] * 0.5) + 0.5;
            double temp = 1.0 - gainLR;
            double mixFront = (inputAs[i] * temp) + (inputBs[i] * gainLR);
            double mixBack = (inputDs[i] * temp) + (inputCs[i] * gainLR);

            double gainFB = (fadeFBs[i] * 0.5) + 0.5;
            outputs[i] = (mixBack * (1.0 - gainFB)) + (mixFront * gainFB);
        }
    }
}
