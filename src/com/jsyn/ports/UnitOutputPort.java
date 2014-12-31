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

import com.jsyn.unitgen.UnitSink;
import com.softsynth.shared.time.TimeStamp;

/**
 * Units write to their output port blocks. Other multiple connected input ports read from them.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 */

public class UnitOutputPort extends UnitBlockPort implements ConnectableOutput, GettablePort {
    public UnitOutputPort() {
        this("Output");
    }

    public UnitOutputPort(String name) {
        this(1, name, 0.0);
    }

    public UnitOutputPort(int numParts, String name) {
        this(numParts, name, 0.0);
    }

    public UnitOutputPort(int numParts, String name, double defaultValue) {
        super(numParts, name, defaultValue);
    }

    public void flatten() {
        for (PortBlockPart part : parts) {
            part.flatten();
        }
    }

    public void connect(int thisPartNum, UnitInputPort otherPort, int otherPartNum) {
        PortBlockPart source = parts[thisPartNum];
        PortBlockPart destination = otherPort.parts[otherPartNum];
        source.connect(destination);
    }

    public void connect(int thisPartNum, UnitInputPort otherPort, int otherPartNum,
            TimeStamp timeStamp) {
        PortBlockPart source = parts[thisPartNum];
        PortBlockPart destination = otherPort.parts[otherPartNum];
        source.connect(destination, timeStamp);
    }

    public void connect(UnitInputPort input) {
        connect(0, input, 0);
    }

    @Override
    public void connect(ConnectableInput input) {
        parts[0].connect(input);
    }

    public void connect(UnitSink sink) {
        connect(0, sink.getInput(), 0);
    }

    public void disconnect(int thisPartNum, UnitInputPort otherPort, int otherPartNum) {
        PortBlockPart source = parts[thisPartNum];
        PortBlockPart destination = otherPort.parts[otherPartNum];
        source.disconnect(destination);
    }

    public void disconnect(int thisPartNum, UnitInputPort otherPort, int otherPartNum,
            TimeStamp timeStamp) {
        PortBlockPart source = parts[thisPartNum];
        PortBlockPart destination = otherPort.parts[otherPartNum];
        source.disconnect(destination, timeStamp);
    }

    public void disconnect(UnitInputPort otherPort) {
        disconnect(0, otherPort, 0);
    }

    @Override
    public void disconnect(ConnectableInput input) {
        parts[0].disconnect(input);
    }

    public ConnectableOutput getConnectablePart(int i) {
        return parts[i];
    }

}
