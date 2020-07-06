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

import com.softsynth.shared.time.ScheduledCommand;
import com.softsynth.shared.time.TimeStamp;

/**
 * A port that is used to pass values into a UnitGenerator.
 *
 * @author Phil Burk 2009 Mobileer Inc
 */
public class UnitInputPort extends UnitBlockPort implements ConnectableInput, SettablePort {
    private double minimum = 0.0;
    private double maximum = 1.0;
    private double defaultValue = 0.0;
    private double[] setValues;
    private boolean valueAdded = false;

    /**
     * @param numParts typically 1, use 2 for stereo ports
     * @param name name that may be used in GUIs
     * @param defaultValue
     */
    public UnitInputPort(int numParts, String name, double defaultValue) {
        super(numParts, name, defaultValue);
        setDefault(defaultValue);
        setValues = new double[numParts];
        for (int i = 0; i < numParts; i++) {
            setValues[i] = defaultValue;
        }
    }

    public UnitInputPort(String name, double defaultValue) {
        this(1, name, defaultValue);
    }

    public UnitInputPort(String name) {
        this(1, name, 0.0);
    }

    public UnitInputPort(int numParts, String name) {
        this(numParts, name, 0.0);
    }

    @Override
    protected void makeParts(int numParts, double defaultValue) {
        parts = new InputMixingBlockPart[numParts];
        for (int i = 0; i < numParts; i++) {
            parts[i] = new InputMixingBlockPart(this, defaultValue);
        }
    }

    /**
     * This is used internally by the SynthesisEngine to execute units based on their connections.
     *
     * @param frameCount
     * @param start
     * @param limit
     */
    @Override
    public void pullData(long frameCount, int start, int limit) {
        for (PortBlockPart part : parts) {
            ((InputMixingBlockPart) part).pullData(frameCount, start, limit);
        }
    }

    @Override
    protected void setValueInternal(int partNum, double value) {
        super.setValueInternal(partNum, value);
        setValues[partNum] = value;
    }

    public void set(double value) {
        set(0, value);
    }

    public void set(final int partNum, final double value) {
        // Trigger exception now if out of range.
        setValues[partNum] = value;
        queueCommand(new ScheduledCommand() {
            @Override
            public void run() {
                setValueInternal(partNum, value);
            }
        });
    }

    public void set(double value, TimeStamp time) {
        set(0, value, time);
    }

    public void set(double value, double time) {
        set(0, value, time);
    }

    public void set(int partNum, double value, double time) {
        set(partNum, value, new TimeStamp(time));
    }

    @Override
    public void set(final int partNum, final double value, TimeStamp timeStamp) {
        // Trigger exception now if out of range.
        getValue(partNum);
        scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                setValueInternal(partNum, value);
            }
        });
    }

    /**
     * Value of a port based on the set() calls. Not affected by connected ports.
     *
     * @param partNum
     * @return value as set
     */
    @Override
    public double get(int partNum) {
        return setValues[partNum];
    }

    public double getMaximum() {
        return maximum;
    }

    /**
     * The minimum and maximum are only used when setting up knobs or other control systems. The
     * internal values are not clipped to this range.
     *
     * @param maximum
     */
    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getDefault() {
        return defaultValue;
    }

    public void setDefault(double defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Convenience function for setting limits on a port. These limits are recommended values when
     * setting up a GUI. It is possible to set a port to a value outside these limits.
     *
     * @param minimum
     * @param value default value, will be clipped to min/max
     * @param maximum
     */
    public void setup(double minimum, double value, double maximum) {
        setMinimum(minimum);
        setMaximum(maximum);
        setDefault(value);
        set(value);
    }

    // Grab min, max, default from another port.
    public void setup(UnitInputPort other) {
        setup(other.getMinimum(), other.getDefault(), other.getMaximum());
    }

    public boolean isValueAdded() {
        return valueAdded;
    }

    /**
     * If set false then the set() value will be ignored when other ports are connected to this port.
     * The sum of the connected port values will be used instead.
     *
     * If set true then the set() value will be added to the sum of the connected port values.
     * This is useful when you want to modulate the set value.
     *
     * The default is false.
     *
     * @param valueAdded
     */
    public void setValueAdded(boolean valueAdded) {
        this.valueAdded = valueAdded;
    }

    public void connect(int thisPartNum, UnitOutputPort otherPort, int otherPartNum,
            TimeStamp timeStamp) {
        otherPort.connect(otherPartNum, this, thisPartNum, timeStamp);
    }

    /** Connect an input to an output port. */
    public void connect(int thisPartNum, UnitOutputPort otherPort, int otherPartNum) {
        // Typically connections are made from output to input because it is
        // more intuitive.
        otherPort.connect(otherPartNum, this, thisPartNum);
    }

    public void connect(UnitOutputPort otherPort) {
        connect(0, otherPort, 0);
    }

    @Override
    public void connect(ConnectableOutput other) {
        other.connect(this);
    }

    public void disconnect(int thisPartNum, UnitOutputPort otherPort, int otherPartNum) {
        otherPort.disconnect(otherPartNum, this, thisPartNum);
    }

    @Override
    public PortBlockPart getPortBlockPart() {
        return parts[0];
    }

    public ConnectableInput getConnectablePart(int i) {
        return parts[i];
    }

    @Override
    public void disconnect(ConnectableOutput other) {
        other.disconnect(this);
    }

    public void printConnections(PrintStream out, int level) {
        for (PortBlockPart part : parts) {
            ((InputMixingBlockPart) part).printConnections(out, level);
        }
    }

}
