/*
 * Copyright 2026 Phil Burk
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

package com.jsyn.dsp;

/**
 * Delay line that can delay by a fractional number of frames. Use it one frame at a time inside a
 * feedback loop: {@link #read()} the delayed value, then {@link #write(float)} the new one.
 *
 * @see AllPassFractionalDelay
 * @see LagrangeFractionalDelay
 */
public abstract class FractionalDelay {
    /** Smallest delay that can be interpolated. */
    public static final double MIN_DELAY = 2.0;

    /** Extra frames for the interpolation taps. */
    private static final int EXTRA_FRAMES = 4;

    private final int maxDelay;
    private final SimpleDelay delayLine;

    /**
     * @param maxDelay largest delay in frames
     */
    public FractionalDelay(int maxDelay) {
        this.maxDelay = maxDelay;
        delayLine = new SimpleDelay(maxDelay + EXTRA_FRAMES);
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * @return true if the delay can be changed on every frame without clicks. Otherwise set the
     *     delay only when it changes, for example once per block.
     */
    public abstract boolean isSmooth();

    /**
     * @param delay in frames, limited to between MIN_DELAY and getMaxDelay()
     * @param omega radian frequency where the delay should be exact,
     *     2 * PI * frequency / frameRate. Only needed by interpolators whose delay varies with
     *     frequency.
     */
    public abstract void setDelay(double delay, double omega);

    /** @return the value written the delay set by setDelay() frames ago */
    public abstract float read();

    /** Write the newest value and advance. Call once per frame after {@link #read()}. */
    public void write(float input) {
        delayLine.write(input);
        delayLine.advance();
    }

    /** Set all of the delayed values to zero. */
    public void clear() {
        delayLine.clear();
    }

    /** @return the value written this many frames ago */
    protected float tap(int position) {
        return delayLine.read(position);
    }

    protected double limitDelay(double delay) {
        return Math.max(MIN_DELAY, Math.min(delay, maxDelay));
    }
}
