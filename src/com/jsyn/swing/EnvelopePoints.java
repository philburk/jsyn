/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

package com.jsyn.swing;

import java.util.Vector;

import com.jsyn.data.SegmentedEnvelope;

/**
 * Vector that contains duration,value pairs. Used by EnvelopeEditor
 * 
 * @author (C) 1997 Phil Burk, SoftSynth.com
 */

/* ========================================================================== */
public class EnvelopePoints extends Vector {
    private String name = "";
    private double maximumValue = 1.0;
    private int sustainBegin = -1;
    private int sustainEnd = -1;
    private int releaseBegin = -1;
    private int releaseEnd = -1;
    private boolean dirty = false;

    /**
     * Update only if points or loops were modified.
     */
    public void updateEnvelopeIfDirty(SegmentedEnvelope envelope) {
        if (dirty) {
            updateEnvelope(envelope);
        }
    }

    /**
     * The editor works on a vector of points, not a real envelope. The data must be written to a
     * real SynthEnvelope in order to use it.
     */
    public void updateEnvelope(SegmentedEnvelope envelope) {
        int numFrames = size();
        for (int i = 0; i < numFrames; i++) {
            envelope.write(i, getPoint(i), 0, 1);
        }
        envelope.setSustainBegin(getSustainBegin());
        envelope.setSustainEnd(getSustainEnd());
        envelope.setReleaseBegin(getReleaseBegin());
        envelope.setReleaseEnd(getReleaseEnd());
        envelope.setNumFrames(numFrames);
        dirty = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMaximumValue(double maximumValue) {
        this.maximumValue = maximumValue;
    }

    public double getMaximumValue() {
        return maximumValue;
    }

    public void add(double dur, double value) {
        double dar[] = {
                dur, value
        };
        addElement(dar);
        dirty = true;
    }

    /**
     * Insert point without changing total duration by reducing next points duration.
     */
    public void insert(int index, double dur, double y) {
        double dar[] = {
                dur, y
        };
        if (index < size()) {
            ((double[]) elementAt(index))[0] -= dur;
        }
        insertElementAt(dar, index);

        if (index <= sustainBegin)
            sustainBegin += 1;
        if (index <= sustainEnd)
            sustainEnd += 1;
        if (index <= releaseBegin)
            releaseBegin += 1;
        if (index <= releaseEnd)
            releaseEnd += 1;
        dirty = true;
    }

    /**
     * Remove indexed point and update sustain and release loops if necessary. Did not name this
     * "remove()" because of conflicts with new JDK 1.3 method with the same name.
     */
    public void removePoint(int index) {
        super.removeElementAt(index);
        // move down loop if points below or inside loop removed
        if (index < sustainBegin)
            sustainBegin -= 1;
        if (index <= sustainEnd)
            sustainEnd -= 1;
        if (index < releaseBegin)
            releaseBegin -= 1;
        if (index <= releaseEnd)
            releaseEnd -= 1;

        // was entire loop removed?
        if (sustainBegin > sustainEnd) {
            sustainBegin = -1;
            sustainEnd = -1;
        }
        // was entire loop removed?
        if (releaseBegin > releaseEnd) {
            releaseBegin = -1;
            releaseEnd = -1;
        }
        dirty = true;
    }

    public double getDuration(int index) {
        return ((double[]) elementAt(index))[0];
    }

    public double getValue(int index) {
        return ((double[]) elementAt(index))[1];
    }

    public double[] getPoint(int index) {
        return (double[]) elementAt(index);
    }

    public double getTotalDuration() {
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            double dar[] = (double[]) elementAt(i);
            sum += dar[0];
        }
        return sum;
    }

    /**
     * Set location of Sustain Loop in units of Frames. Set SustainBegin to -1 if no Sustain Loop.
     * SustainEnd value is the frame index of the frame just past the end of the loop. The number of
     * frames included in the loop is (SustainEnd - SustainBegin).
     */
    public void setSustainLoop(int startFrame, int endFrame) {
        this.sustainBegin = startFrame;
        this.sustainEnd = endFrame;
        dirty = true;
    }

    /***
     * @return Beginning of sustain loop or -1 if no loop.
     */
    public int getSustainBegin() {
        return this.sustainBegin;
    }

    /***
     * @return End of sustain loop or -1 if no loop.
     */
    public int getSustainEnd() {
        return this.sustainEnd;
    }

    /***
     * @return Size of sustain loop in frames, 0 if no loop.
     */
    public int getSustainSize() {
        return (this.sustainEnd - this.sustainBegin);
    }

    /**
     * Set location of Release Loop in units of Frames. Set ReleaseBegin to -1 if no ReleaseLoop.
     * ReleaseEnd value is the frame index of the frame just past the end of the loop. The number of
     * frames included in the loop is (ReleaseEnd - ReleaseBegin).
     */
    public void setReleaseLoop(int startFrame, int endFrame) {
        this.releaseBegin = startFrame;
        this.releaseEnd = endFrame;
        dirty = true;
    }

    /***
     * @return Beginning of release loop or -1 if no loop.
     */
    public int getReleaseBegin() {
        return this.releaseBegin;
    }

    /***
     * @return End of release loop or -1 if no loop.
     */
    public int getReleaseEnd() {
        return this.releaseEnd;
    }

    /***
     * @return Size of release loop in frames, 0 if no loop.
     */
    public int getReleaseSize() {
        return (this.releaseEnd - this.releaseBegin);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean b) {
        dirty = b;
    }

}
