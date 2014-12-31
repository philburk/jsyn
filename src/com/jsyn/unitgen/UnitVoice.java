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

package com.jsyn.unitgen;

import com.jsyn.util.Instrument;
import com.jsyn.util.VoiceDescription;
import com.softsynth.shared.time.TimeStamp;

/**
 * A voice that can be allocated and played by the VoiceAllocator.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 * @see VoiceDescription
 * @see Instrument
 */
public interface UnitVoice extends UnitSource {
    /**
     * Play whatever you consider to be a note on this voice. Do not be constrained by traditional
     * definitions of notes or music.
     * 
     * @param frequency in Hz related to the perceived pitch of the note.
     * @param amplitude generally between 0.0 and 1.0
     * @param timeStamp when to play the note
     */
    void noteOn(double frequency, double amplitude, TimeStamp timeStamp);

    void noteOff(TimeStamp timeStamp);

    /**
     * Typically a UnitVoice will be a subclass of UnitGenerator, which just returns "this".
     */
    @Override
    public UnitGenerator getUnitGenerator();

    /**
     * Looks up a port using its name and sets the value.
     * 
     * @param portName
     * @param value
     * @param timeStamp
     */
    void setPort(String portName, double value, TimeStamp timeStamp);

    void usePreset(int presetIndex);
}
