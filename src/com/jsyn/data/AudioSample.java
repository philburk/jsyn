/*
 * Copyright 2010 Phil Burk, Mobileer Inc
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

package com.jsyn.data;

import java.util.ArrayList;

/**
 * Base class for FloatSample and ShortSample.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public abstract class AudioSample extends SequentialDataCommon {
    protected int numFrames;
    protected int channelsPerFrame = 1;
    private double frameRate = 44100.0;
    private double pitch;
    private ArrayList<SampleMarker> markers;

    public abstract void allocate(int numFrames, int channelsPerFrame);

    @Override
    public double getRateScaler(int index, double synthesisRate) {
        return 1.0;
    }

    public double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(double f) {
        this.frameRate = f;
    }

    @Override
    public int getNumFrames() {
        return numFrames;
    }

    @Override
    public int getChannelsPerFrame() {
        return channelsPerFrame;
    }

    public void setChannelsPerFrame(int channelsPerFrame) {
        this.channelsPerFrame = channelsPerFrame;
    }

    /**
     * Set the recorded pitch as a fractional MIDI semitone value where 60 is Middle C.
     *
     * @param pitch
     */
    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getPitch() {
        return pitch;
    }

    public int getMarkerCount() {
        if (markers == null)
            return 0;
        else
            return markers.size();
    }

    public SampleMarker getMarker(int index) {
        if (markers == null)
            return null;
        else
            return markers.get(index);
    }

    /**
     * Add a marker that will be stored sorted by position. This is normally used internally by the
     * SampleLoader.
     *
     * @param marker
     */
    public void addMarker(SampleMarker marker) {
        if (markers == null)
            markers = new ArrayList<SampleMarker>();
        int idx = markers.size();
        for (int k = 0; k < markers.size(); k++) {
            SampleMarker cue = markers.get(k);
            if (cue.position > marker.position) {
                idx = k;
                break;
            }
        }
        markers.add(idx, marker);
    }
}
