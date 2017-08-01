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

import java.util.ArrayList;

import com.jsyn.Synthesizer;
import com.jsyn.engine.SynthesisEngine;
import com.softsynth.shared.time.ScheduledCommand;
import com.softsynth.shared.time.TimeStamp;

/**
 * Part of a multi-part port, for example, the left side of a stereo port.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class PortBlockPart implements ConnectableOutput, ConnectableInput {
    private double[] values = new double[Synthesizer.FRAMES_PER_BLOCK];
    private ArrayList<PortBlockPart> connections = new ArrayList<PortBlockPart>();
    private UnitBlockPort unitBlockPort;

    protected PortBlockPart(UnitBlockPort unitBlockPort, double defaultValue) {
        this.unitBlockPort = unitBlockPort;
        setValue(defaultValue);
    }

    public double[] getValues() {
        return values;
    }

    public double getValue() {
        return values[0];
    }

    public double get() {
        return values[0];
    }

    protected void setValue(double value) {
        for (int i = 0; i < values.length; i++) {
            values[i] = value;
        }
    }

    protected boolean isConnected() {
        return (connections.size() > 0);
    }

    private void addConnection(PortBlockPart otherPart) {
        // System.out.println("addConnection from " + this + " to " + otherPart
        // );
        if (connections.contains(otherPart)) {
            System.out.println("addConnection already had connection from " + this + " to "
                    + otherPart);
        } else {
            connections.add(otherPart);
        }
    }

    private void removeConnection(PortBlockPart otherPart) {
        // System.out.println("removeConnection from " + this + " to " +
        // otherPart );
        connections.remove(otherPart);
    }

    private void connectNow(PortBlockPart otherPart) {
        addConnection(otherPart);
        otherPart.addConnection(this);
    }

    private void disconnectNow(PortBlockPart otherPart) {
        removeConnection(otherPart);
        otherPart.removeConnection(this);
    }

    private void disconnectAllNow() {
        for (PortBlockPart part : connections) {
            part.removeConnection(this);
        }
        connections.clear();
    }

    public PortBlockPart getConnection(int i) {
        return connections.get(i);
    }

    public int getConnectionCount() {
        return connections.size();
    }

    /** Set all values to the last value. */
    protected void flatten() {
        double lastValue = values[values.length - 1];
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = lastValue;
        }
    }

    protected UnitBlockPort getPort() {
        return unitBlockPort;
    }

    private void checkConnection(PortBlockPart destination) {
        SynthesisEngine sourceSynth = unitBlockPort.getSynthesisEngine();
        SynthesisEngine destSynth = destination.unitBlockPort.getSynthesisEngine();
        if ((sourceSynth != destSynth) && (sourceSynth != null) && (destSynth != null)) {
            throw new RuntimeException("Connection between units on different synths.");
        }
    }

    protected void connect(final PortBlockPart destination) {
        checkConnection(destination);
        unitBlockPort.queueCommand(new ScheduledCommand() {
            @Override
            public void run() {
                connectNow(destination);
            }
        });
    }

    protected void connect(final PortBlockPart destination, TimeStamp timeStamp) {
        unitBlockPort.scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                connectNow(destination);
            }
        });
    }

    protected void disconnect(final PortBlockPart destination) {
        unitBlockPort.queueCommand(new ScheduledCommand() {
            @Override
            public void run() {
                disconnectNow(destination);
            }
        });
    }

    protected void disconnect(final PortBlockPart destination, TimeStamp timeStamp) {
        unitBlockPort.scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                disconnectNow(destination);
            }
        });
    }

    protected void disconnectAll() {
        unitBlockPort.queueCommand(new ScheduledCommand() {
            @Override
            public void run() {
                disconnectAllNow();
            }
        });
    }

    @Override
    public void connect(ConnectableInput other) {
        connect(other.getPortBlockPart());
    }

    @Override
    public void connect(ConnectableOutput other) {
        other.connect(this);
    }

    @Override
    public void disconnect(ConnectableOutput other) {
        other.disconnect(this);
    }

    @Override
    public void disconnect(ConnectableInput other) {
        disconnect(other.getPortBlockPart());
    }

    /** To implement ConnectableInput */
    @Override
    public PortBlockPart getPortBlockPart() {
        return this;
    }

    @Override
    public void pullData(long frameCount, int start, int limit) {
        for (int i = 0; i < getConnectionCount(); i++) {
            PortBlockPart part = getConnection(i);
            part.getPort().getUnitGenerator().pullData(frameCount, start, limit);
        }
    }

}
