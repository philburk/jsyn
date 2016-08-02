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

package com.jsyn.ports;

import java.io.PrintStream;

import com.jsyn.Synthesizer;
import com.jsyn.unitgen.UnitGenerator;

/**
 * A UnitInputPort has an array of these, one for each part.
 *
 * @author Phil Burk 2009 Mobileer Inc
 */

public class InputMixingBlockPart extends PortBlockPart {
    private double[] mixer = new double[Synthesizer.FRAMES_PER_BLOCK];
    private double current;
    private UnitInputPort unitInputPort;

    InputMixingBlockPart(UnitInputPort unitInputPort, double defaultValue) {
        super(unitInputPort, defaultValue);
        this.unitInputPort = unitInputPort;
    }

    @Override
    public double getValue() {
        return current;
    }

    @Override
    protected void setValue(double value) {
        current = value;
        super.setValue(value);
    }

    @Override
    public double[] getValues() {
        double[] result;
        int numConnections = getConnectionCount();
        // System.out.println("numConnection = " + numConnections + " for " +
        // this );
        if (numConnections == 0) {
            // No connection so just use our own data.
            result = super.getValues();
        } else {
            // Mix all of the connected ports.
            double[] inputs;
            int jCon = 0;
            PortBlockPart otherPart;
            // Choose value to initialize the mixer array.
            if (unitInputPort.isValueAdded()) {
                inputs = super.getValues();  // prime mixer with the set() values
                jCon = 0;
            } else {
                otherPart = getConnection(jCon);
                inputs = otherPart.getValues(); // prime mixer with first connected
                jCon = 1;
            }
            for (int i = 0; i < mixer.length; i++) {
                mixer[i] = inputs[i];
            }
            // Now mix in the remaining inputs.
            for (; jCon < numConnections; jCon++) {
                otherPart = getConnection(jCon);
                inputs = otherPart.getValues();
                for (int i = 0; i < mixer.length; i++) {
                    mixer[i] += inputs[i];
                }
            }
            result = mixer;
        }
        current = result[0];
        return result;
    }

    private void printIndentation(PrintStream out, int level) {
        for (int i = 0; i < level; i++) {
            out.print("    ");
        }
    }

    private String portToString(UnitBlockPort port) {
        UnitGenerator ugen = port.getUnitGenerator();
        return ugen.getClass().getSimpleName() + "." + port.getName();
    }

    public void printConnections(PrintStream out, int level) {
        for (int i = 0; i < getConnectionCount(); i++) {
            PortBlockPart part = getConnection(i);

            printIndentation(out, level);
            out.println(portToString(getPort()) + " <--- " + portToString(part.getPort()));

            part.getPort().getUnitGenerator().printConnections(out, level + 1);
        }
    }
}
