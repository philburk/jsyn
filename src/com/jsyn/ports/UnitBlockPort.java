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

/**
 * A port that contains multiple parts with blocks of data.
 * 
 * @author Phil Burk 2009 Mobileer Inc
 */
public class UnitBlockPort extends UnitPort {
    PortBlockPart[] parts;

    public UnitBlockPort(int numParts, String name, double defaultValue) {
        super(name);
        makeParts(numParts, defaultValue);
    }

    public UnitBlockPort(String name) {
        this(1, name, 0.0);
    }

    protected void makeParts(int numParts, double defaultValue) {
        parts = new PortBlockPart[numParts];
        for (int i = 0; i < numParts; i++) {
            parts[i] = new PortBlockPart(this, defaultValue);
        }
    }

    @Override
    public int getNumParts() {
        return parts.length;
    }

    /**
     * Convenience call to get(0).
     * 
     * @return value of 0th part as set
     */
    public double get() {
        return get(0);
    }

    public double getValue() {
        return getValue(0);
    }

    /**
     * This is used inside UnitGenerators to get the current values for a port. It works regardless
     * of whether the port is connected or not.
     * 
     * @return
     */
    public double[] getValues() {
        return parts[0].getValues();
    }

    /** Only for use in the audio thread when implementing UnitGenerators. */
    public double[] getValues(int partNum) {
        return parts[partNum].getValues();
    }

    /** Get the immediate current value of the port. */
    public double getValue(int partNum) {
        return parts[partNum].getValue();
    }

    public double get(int partNum) {
        return parts[partNum].get();
    }

    /** Only for use in the audio thread when implementing UnitGenerators. */
    protected void setValueInternal(int partNum, double value) {
        parts[partNum].setValue(value);
    }

    /** Only for use in the audio thread when implementing UnitGenerators. */
    public void setValueInternal(double value) {
        setValueInternal(0, value);
    }

    public boolean isConnected() {
        return isConnected(0);
    }

    public boolean isConnected(int partNum) {
        return parts[partNum].isConnected();
    }

    public void disconnectAll(int partNum) {
        parts[partNum].disconnectAll();
    }

    public void disconnectAll() {
        disconnectAll(0);
    }
}
