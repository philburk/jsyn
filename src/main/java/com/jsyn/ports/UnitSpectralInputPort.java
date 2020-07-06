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

import com.jsyn.data.Spectrum;

public class UnitSpectralInputPort extends UnitPort implements ConnectableInput {
    private UnitSpectralOutputPort other;

    private Spectrum spectrum;

    public UnitSpectralInputPort() {
        this("Output");
    }

    public UnitSpectralInputPort(String name) {
        super(name);
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
    }

    public Spectrum getSpectrum() {
        if (other == null) {
            return spectrum;
        } else {
            return other.getSpectrum();
        }
    }

    @Override
    public void connect(ConnectableOutput other) {
        if (other instanceof UnitSpectralOutputPort) {
            this.other = (UnitSpectralOutputPort) other;
        } else {
            throw new RuntimeException(
                    "Can only connect UnitSpectralOutputPort to UnitSpectralInputPort!");
        }
    }

    @Override
    public void disconnect(ConnectableOutput other) {
        if (this.other == other) {
            this.other = null;
        }
    }

    @Override
    public PortBlockPart getPortBlockPart() {
        return null;
    }

    @Override
    public void pullData(long frameCount, int start, int limit) {
        if (other != null) {
            other.getUnitGenerator().pullData(frameCount, start, limit);
        }
    }

    public boolean isAvailable() {
        if (other != null) {
            return other.isAvailable();
        } else {
            return (spectrum != null);
        }
    }

}
