/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

package com.jsyn.util;

import com.jsyn.unitgen.UnitVoice;

/**
 * Describe a voice so that a user can pick it out of an InstrumentLibrary.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 * @see PolyphonicInstrument
 */
public abstract class VoiceDescription {
    private String name;
    private String[] presetNames;

    public VoiceDescription(String name, String[] presetNames) {
        this.name = name;
        this.presetNames = presetNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPresetCount() {
        return presetNames.length;
    }

    public String[] getPresetNames() {
        return presetNames;
    }

    public abstract String[] getTags(int presetIndex);

    /**
     * Instantiate one of these voices. You may want to call usePreset(n) on the voice after
     * instantiating it.
     * 
     * @return a voice
     */
    public abstract UnitVoice createUnitVoice();

    public abstract String getVoiceClassName();

    @Override
    public String toString() {
        return name + "[" + getPresetCount() + "]";
    }
}
