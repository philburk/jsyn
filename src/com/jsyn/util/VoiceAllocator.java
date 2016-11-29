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

import com.jsyn.Synthesizer;
import com.jsyn.unitgen.UnitVoice;
import com.softsynth.shared.time.ScheduledCommand;
import com.softsynth.shared.time.TimeStamp;

/**
 * Allocate voices based on an integer tag. The tag could, for example, be a MIDI note number. Or a
 * tag could be an int that always increments. Use the same tag to refer to a voice for noteOn() and
 * noteOff(). If no new voices are available then a voice in use will be stolen.
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class VoiceAllocator implements Instrument {
    private int maxVoices;
    private VoiceTracker[] trackers;
    private long tick;
    private Synthesizer synthesizer;
    private static final int UNASSIGNED_PRESET = -1;
    private int mPresetIndex = UNASSIGNED_PRESET;

    /**
     * Create an allocator for the array of UnitVoices. The array must be full of instantiated
     * UnitVoices that are connected to some kind of mixer.
     *
     * @param voices
     */
    public VoiceAllocator(UnitVoice[] voices) {
        maxVoices = voices.length;
        trackers = new VoiceTracker[maxVoices];
        for (int i = 0; i < maxVoices; i++) {
            trackers[i] = new VoiceTracker();
            trackers[i].voice = voices[i];
        }
    }

    public Synthesizer getSynthesizer() {
        if (synthesizer == null) {
            synthesizer = trackers[0].voice.getUnitGenerator().getSynthesizer();
        }
        return synthesizer;
    }

    private class VoiceTracker {
        UnitVoice voice;
        int tag = -1;
        int presetIndex = UNASSIGNED_PRESET;
        long when;
        boolean on;

        public void off() {
            on = false;
            when = tick++;
        }
    }

    /**
     * @return number of UnitVoices passed to the allocator.
     */
    public int getVoiceCount() {
        return maxVoices;
    }

    private VoiceTracker findVoice(int tag) {
        for (VoiceTracker tracker : trackers) {
            if (tracker.tag == tag) {
                return tracker;
            }
        }
        return null;
    }

    private VoiceTracker stealVoice() {
        VoiceTracker bestOff = null;
        VoiceTracker bestOn = null;
        for (VoiceTracker tracker : trackers) {
            if (tracker.voice == null) {
                return tracker;
            }
            // If we have a bestOff voice then don't even bother with on voices.
            else if (bestOff != null) {
                // Older off voice?
                if (!tracker.on && (tracker.when < bestOff.when)) {
                    bestOff = tracker;
                }
            } else if (tracker.on) {
                if (bestOn == null) {
                    bestOn = tracker;
                } else if (tracker.when < bestOn.when) {
                    bestOn = tracker;
                }
            } else {
                bestOff = tracker;
            }
        }
        if (bestOff != null) {
            return bestOff;
        } else {
            return bestOn;
        }
    }

    /**
     * Allocate a Voice associated with this tag. It will first pick a voice already assigned to
     * that tag. Next it will pick the oldest voice that is off. Next it will pick the oldest voice
     * that is on. If you are using timestamps to play the voice in the future then you should use
     * the noteOn() noteOff() and setPort() methods.
     *
     * @param tag
     * @return Voice that is most available.
     */
    protected synchronized UnitVoice allocate(int tag) {
        VoiceTracker tracker = allocateTracker(tag);
        return tracker.voice;
    }

    private VoiceTracker allocateTracker(int tag) {
        VoiceTracker tracker = findVoice(tag);
        if (tracker == null) {
            tracker = stealVoice();
        }
        tracker.tag = tag;
        tracker.when = tick++;
        tracker.on = true;
        return tracker;
    }

    protected synchronized boolean isOn(int tag) {
        VoiceTracker tracker = findVoice(tag);
        if (tracker != null) {
            return tracker.on;
        }
        return false;
    }

    protected synchronized UnitVoice off(int tag) {
        VoiceTracker tracker = findVoice(tag);
        if (tracker != null) {
            tracker.off();
            return tracker.voice;
        }
        return null;
    }

    /** Turn off all the note currently on. */
    @Override
    public void allNotesOff(TimeStamp timeStamp) {
        getSynthesizer().scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                for (VoiceTracker tracker : trackers) {
                    if (tracker.on) {
                        tracker.voice.noteOff(getSynthesizer().createTimeStamp());
                        tracker.off();
                    }
                }
            }
        });
    }

    /**
     * Play a note on the voice and associate it with the given tag. if needed a new voice will be
     * allocated and an old voice may be turned off.
     */
    @Override
    public void noteOn(final int tag, final double frequency, final double amplitude,
            TimeStamp timeStamp) {
        getSynthesizer().scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                VoiceTracker voiceTracker = allocateTracker(tag);
                if (voiceTracker.presetIndex != mPresetIndex) {
                    voiceTracker.voice.usePreset(mPresetIndex);
                    voiceTracker.presetIndex = mPresetIndex;
                }
                voiceTracker.voice.noteOn(frequency, amplitude, getSynthesizer().createTimeStamp());
            }
        });
    }

    /**
     * Play a note on the voice and associate it with the given tag. if needed a new voice will be
     * allocated and an old voice may be turned off.
     * Apply an operation to the voice.
     */
    public void noteOn(final int tag,
            final double frequency,
            final double amplitude,
            final VoiceOperation operation,
            TimeStamp timeStamp) {
        getSynthesizer().scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                VoiceTracker voiceTracker = allocateTracker(tag);
                operation.operate(voiceTracker.voice);
                voiceTracker.voice.noteOn(frequency, amplitude, getSynthesizer().createTimeStamp());
            }
        });
    }

    /** Turn off the voice associated with the given tag if allocated. */
    @Override
    public void noteOff(final int tag, TimeStamp timeStamp) {
        getSynthesizer().scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                VoiceTracker voiceTracker = findVoice(tag);
                if (voiceTracker != null) {
                    voiceTracker.voice.noteOff(getSynthesizer().createTimeStamp());
                    off(tag);
                }
            }
        });
    }

    /** Set a port on the voice associated with the given tag if allocated. */
    @Override
    public void setPort(final int tag, final String portName, final double value,
            TimeStamp timeStamp) {
        getSynthesizer().scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                VoiceTracker voiceTracker = findVoice(tag);
                if (voiceTracker != null) {
                    voiceTracker.voice.setPort(portName, value, getSynthesizer().createTimeStamp());
                }
            }
        });
    }

    @Override
    public void usePreset(final int presetIndex, TimeStamp timeStamp) {
        getSynthesizer().scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                mPresetIndex = presetIndex;
            }
        });
    }

}
