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

/**
 * Abstract base class for envelopes and samples that adds sustain and release loops.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public abstract class SequentialDataCommon implements SequentialData {
    protected int numFrames;
    protected int maxFrames;
    private int sustainBegin = -1;
    private int sustainEnd = -1;
    private int releaseBegin = -1;
    private int releaseEnd = -1;

    @Override
    public abstract void writeDouble(int index, double value);

    @Override
    public abstract double readDouble(int index);

    @Override
    public abstract double getRateScaler(int index, double synthesisRate);

    @Override
    public abstract int getChannelsPerFrame();

    /**
     * @return Maximum number of frames of data.
     */
    public int getMaxFrames() {
        return maxFrames;
    }

    /**
     * Set number of frames of data. Input will be clipped to maxFrames. This is useful when
     * changing the contents of a sample or envelope.
     */
    public void setNumFrames(int numFrames) {
        if (numFrames > maxFrames)
            numFrames = maxFrames;
        this.numFrames = numFrames;
    }

    @Override
    public int getNumFrames() {
        return numFrames;
    }

    // JavaDocs will be copied from SequentialData

    @Override
    public int getSustainBegin() {
        return this.sustainBegin;
    }

    @Override
    public int getSustainEnd() {
        return this.sustainEnd;
    }

    @Override
    public int getReleaseBegin() {
        return this.releaseBegin;
    }

    @Override
    public int getReleaseEnd() {
        return this.releaseEnd;
    }

    public void setSustainBegin(int sustainBegin) {
        this.sustainBegin = sustainBegin;
    }

    /**
     * SustainEnd value is the frame index of the frame just past the end of the loop. The number of
     * frames included in the loop is (SustainEnd - SustainBegin).
     * 
     * @param sustainEnd
     */
    public void setSustainEnd(int sustainEnd) {
        this.sustainEnd = sustainEnd;
    }

    public void setReleaseBegin(int releaseBegin) {
        this.releaseBegin = releaseBegin;
    }

    /**
     * ReleaseEnd value is the frame index of the frame just past the end of the loop. The number of
     * frames included in the loop is (ReleaseEnd - ReleaseBegin).
     * 
     * @param releaseEnd
     */

    public void setReleaseEnd(int releaseEnd) {
        this.releaseEnd = releaseEnd;
    }

}
