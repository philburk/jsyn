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

public class UnitSpectralOutputPort extends UnitPort implements ConnectableOutput {
    private Spectrum spectrum;
    private boolean available;

    public UnitSpectralOutputPort() {
        this("Output");
    }

    public UnitSpectralOutputPort(int size) {
        this("Output", size);
    }

    public UnitSpectralOutputPort(String name) {
        super(name);
        spectrum = new Spectrum();
    }

    public UnitSpectralOutputPort(String name, int size) {
        super(name);
        spectrum = new Spectrum(size);
    }

    public void setSize(int size) {
        spectrum.setSize(size);
    }

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public void advance() {
        available = true;
    }

    @Override
    public void connect(ConnectableInput other) {
        other.connect(this);
    }

    @Override
    public void disconnect(ConnectableInput other) {
        other.disconnect(this);
    }

    public boolean isAvailable() {
        return available;
    }

}
