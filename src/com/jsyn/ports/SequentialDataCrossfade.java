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

import com.jsyn.data.SequentialData;
import com.jsyn.data.SequentialDataCommon;

/**
 * A SequentialData object that will crossfade between two other SequentialData objects. The
 * crossfade is linear. This could, for example, be used to create a smooth transition between two
 * samples, or between two arbitrary regions in one sample. As an example, consider a sample that
 * has a length of 200000 frames. You could specify a sample loop that started arbitrarily at frame
 * 50000 and with a size of 30000 frames. Unless you got lucky with the zero crossings, it is likely
 * that you will hear a pop when this sample loops. To prevent the pop you could crossfade the
 * beginning of the loop with the region immediately after the end of the loop. To crossfade with
 * 5000 samples after the loop:
 * 
 * <pre>
 * SequentialDataCrossfade xfade = new SequentialDataCrossfade(sample, (50000 + 30000), 5000, sample,
 *         50000, 30000);
 * </pre>
 * 
 * After the crossfade you will hear the rest of the target at full volume. There are two regions
 * that determine what is returned from readDouble()
 * <ol>
 * <li>Crossfade region with size crossFadeFrames. It fades smoothly from source to target.</li>
 * <li>Steady region that is simply the target values with size (numFrames-crossFadeFrames).</li>
 * </ol>
 * 
 * <pre>
 *     "Crossfade Region"      "Steady Region"
 * |-- source fading out --|
 * |-- target fading in  --|-- remainder of target at original volume --|
 * </pre>
 * 
 * @author Phil Burk
 */
class SequentialDataCrossfade extends SequentialDataCommon {
    private SequentialData source;
    private int sourceStartIndex;

    private SequentialData target;
    private int targetStartIndex;

    private int crossFadeFrames;
    private double frameScaler;

    /**
     * @param source SequentialData that will be at full volume at the beginning of the crossfade
     *            region.
     * @param sourceStartFrame Frame in source to begin the crossfade.
     * @param crossFadeFrames Number of frames in the crossfaded region.
     * @param target SequentialData that will be at full volume at the end of the crossfade region.
     * @param targetStartFrame Frame in target to begin the crossfade.
     * @param numFrames total number of frames in this data object.
     */
    public void setup(SequentialData source, int sourceStartFrame, int crossFadeFrames,
            SequentialData target, int targetStartFrame, int numFrames) {

        assert ((sourceStartFrame + crossFadeFrames) <= source.getNumFrames());
        assert ((targetStartFrame + numFrames) <= target.getNumFrames());

        // System.out.println( "WARNING! sourceStartFrame = " + sourceStartFrame
        // + ", crossFadeFrames = " + crossFadeFrames + ", maxFrame = "
        // + source.getNumFrames() + ", source = " + source );
        // System.out.println( "  targetStartFrame = " + targetStartFrame
        // + ", numFrames = " + numFrames + ", maxFrame = "
        // + target.getNumFrames() + ", target = " + target );

        // There is a danger that we might nest SequentialDataCrossfades deeply
        // as source. If past crossfade region then pull out the target.
        if (source instanceof SequentialDataCrossfade) {
            SequentialDataCrossfade crossfade = (SequentialDataCrossfade) source;
            // If we are starting past the crossfade region then just use the
            // target.
            if (sourceStartFrame >= crossfade.crossFadeFrames) {
                source = crossfade.target;
                sourceStartFrame += crossfade.targetStartIndex / source.getChannelsPerFrame();
            }
        }

        if (target instanceof SequentialDataCrossfade) {
            SequentialDataCrossfade crossfade = (SequentialDataCrossfade) target;
            target = crossfade.target;
            targetStartFrame += crossfade.targetStartIndex / target.getChannelsPerFrame();
        }

        this.source = source;
        this.target = target;
        this.sourceStartIndex = sourceStartFrame * source.getChannelsPerFrame();
        this.crossFadeFrames = crossFadeFrames;
        this.targetStartIndex = targetStartFrame * target.getChannelsPerFrame();

        frameScaler = (crossFadeFrames == 0) ? 1.0 : (1.0 / crossFadeFrames);
        this.numFrames = numFrames;
    }

    @Override
    public void writeDouble(int index, double value) {
    }

    @Override
    public double readDouble(int index) {
        int frame = index / source.getChannelsPerFrame();
        if (frame < crossFadeFrames) {
            double factor = frame * frameScaler;
            double value = (1.0 - factor) * source.readDouble(index + sourceStartIndex);
            value += (factor * target.readDouble(index + targetStartIndex));
            return value;
        } else {
            return target.readDouble(index + targetStartIndex);
        }
    }

    @Override
    public double getRateScaler(int index, double synthesisRate) {
        return target.getRateScaler(index, synthesisRate);
    }

    @Override
    public int getChannelsPerFrame() {
        return target.getChannelsPerFrame();
    }

}
