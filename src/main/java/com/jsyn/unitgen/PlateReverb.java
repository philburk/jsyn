/*
 * Copyright 2022 Phil Burk
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

import com.jsyn.dsp.AllPassDelay;
import com.jsyn.dsp.SimpleDelay;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.util.PseudoRandom;

/**
 * Simple reverberation effect based on a "figure eight"
 * network of all-pass filters and delays.
 *
 * This reverb  does not have a pre-delay or early reflections.
 * It can be used as the "tail" of a more complex reverb that
 * adds those functions.
 *
 * The algorithm is based on
 * "Effect Design Part 1: Reverberator and Other Filters"
 * by Jon Dattorro, CCRMA, Stanford University 1996
 *
 * @see InterpolatingDelay
 */

public class PlateReverb extends UnitGenerator {

    /**
     * Mono input.
     */
    public UnitInputPort input;

    /**
     * Approximate time in seconds to decay by -60 dB.
     */
    public UnitInputPort time;

    /**
     * Damping factor for the feedback filters.
     * Must be between 0.0 and 1.0. Default is 0.5.
     */
    public UnitInputPort damping;

    /**
     * Stereo output.
     */
    public UnitOutputPort output;

    private static final double MAX_DECAY = 0.98;
    // These default values are based on table-1 of the paper by Jon Dattorro.
    private static final float DECAY_DIFFUSION_1 = 0.70f;
    private static final float DECAY_DIFFUSION_2 = 0.50f;
    private static final float INPUT_DIFFUSION_1 = 0.75f;
    private static final float INPUT_DIFFUSION_2 = 0.625f;
    private static final float DAMPING = 0.5f; // Must match default comment above for damping port.
    private static final float BANDWIDTH = 0.99995f;

    private static class FastSineOscillator {
        private float mPhaseIncrement = 0.0001f;
        private float mPhaseDelta = mPhaseIncrement;
        private float mPhase; // ranges from -PI/2 to PI/2
        private static final float PHASE_LIMIT = (float) Math.PI * 0.5f;

        void setFrequency(float frequency, float sampleRate) {
            mPhaseIncrement = (float) (frequency * Math.PI / sampleRate);
        }

        float generate() {
            // Generate a triangle wave
            mPhase += mPhaseDelta;
            if (mPhase > PHASE_LIMIT) {
                mPhase = PHASE_LIMIT - (mPhase - PHASE_LIMIT);
                mPhaseDelta = -mPhaseIncrement; // reverse direction
            } else if (mPhase < -PHASE_LIMIT) {
                mPhase = -PHASE_LIMIT + (-PHASE_LIMIT - mPhase);
                mPhaseDelta = mPhaseIncrement; // reverse direction
            }

            // Factorial constants so code is easier to read.
            final float IF3 = 1.0f / (2 * 3);
            final float IF5 = IF3 / (4 * 5);
            final float IF7 = IF5 / (6 * 7);
            final float IF9 = IF7 / (8 * 9);
            final float IF11 = IF9 / (10 * 11);

            float x = mPhase;
            float x2 = (x * x);
            /* Taylor expansion factored into multiply-adds */
            // TODO use fewer factors cuz just modulation
            return x
                    * (x2 * (x2 * (x2 * (x2 * ((x2 * (-IF11)) + IF9) - IF7) + IF5) - IF3) + 1);
        }
    }

    private static class RandomModulator {
        private PseudoRandom randomNum = new PseudoRandom();;
        protected float prevNoise, currNoise;
        private float mPhase;
        private float mPhaseIncrement;

        void setFrequency(float frequency, float sampleRate) {
            mPhaseIncrement = frequency / sampleRate;
        }

        // Generate ramps between random points between -1.0 and +1.0.
        public float generate() {
            mPhase += mPhaseIncrement;

            // calculate new random value whenever phase passes 1.0
            if (mPhase > 1.0) {
                prevNoise = currNoise;
                currNoise = (float) randomNum.nextRandomDouble();
                // reset phase for interpolation
                mPhase -= 1.0;
            }

            // interpolate current
            return prevNoise + (mPhase * (currNoise - prevNoise));
        }
    }

    /**
     * Allpass delay modulated by a random ramp.
     */
    private static class VariableAllPassDelay {
        RandomModulator mModulator = new RandomModulator();
        private float[] mBuffer;
        private int mLength;
        private int mCursor;
        private int mModulationDepth;
        private float mCoefficient = 0.65f;

        VariableAllPassDelay(int length, float coefficient) {
            mLength = length;
            mBuffer = new float[2 * length];
            mCoefficient = coefficient;
            setModulationDepth(40);
        }

        void setModulationDepth(int depthInFrames) {
            mModulationDepth = Math.min(depthInFrames, mLength / 3);
        }

        void setFrequency(float frequency, float sampleRate) {
            mModulator.setFrequency(frequency, sampleRate);
        }

        private float process(float input) {
            int readCursor = mCursor - mLength;
            readCursor += (int)(mModulator.generate() * mModulationDepth);
            if (readCursor < 0) readCursor += mBuffer.length;

            float z = mBuffer[readCursor];

            float x = input - (z * mCoefficient );
            mBuffer[mCursor] = x;
            mCursor++;
            if (mCursor >= mBuffer.length) mCursor = 0;
            return z + (x * mCoefficient);
        }
    }

    // y = x*c + y*(1-c)
    private static class OnePoleLowPassFilter {
        private float mDelay;
        private float mCoefficient;

        OnePoleLowPassFilter(float coefficient) {
            mCoefficient = coefficient;
        }

        private float process(float input) {
            float output = (input * mCoefficient)
                    + (mDelay * (1.0f - mCoefficient));
            mDelay = output;
            return output;
        }

        public void setCoefficient(float coefficient) {
            mCoefficient = coefficient;
        }
    }

    // One side of the figure eight.
    private class ReverbSide {
        VariableAllPassDelay variableDelay;
        OnePoleLowPassFilter mLowPass = new OnePoleLowPassFilter(1.0f - DAMPING);
        SimpleDelay mDelay1;
        AllPassDelay mAllPassDelay;
        SimpleDelay  mDelay2;
        private float outputScaler = 0.6f;
        private float mOutput;

        ReverbSide(int d1, int d2, int d3, int d4) {
            // This all pass reverses the signs.
            variableDelay = new VariableAllPassDelay(d1, 0.0f - DECAY_DIFFUSION_1);
            mDelay1 = new SimpleDelay(d2);
            mAllPassDelay = new AllPassDelay(d3, DECAY_DIFFUSION_2);
            mDelay2 = new SimpleDelay(d4);
        }

        public void setFrequency(float frequency, float sampleRate) {
            variableDelay.setFrequency(frequency, sampleRate);
        }

        private float process(float input) {
            float temp = variableDelay.process(input);
            mOutput = temp;
            temp = mDelay1.process(temp);
            mOutput -= temp;
            temp = mLowPass.process(temp);
            temp *= mDecay;
            temp = mAllPassDelay.process(temp);
            mOutput += temp;
            temp = mDelay2.process(temp);
            temp *= mDecay;
            mOutput -= temp;
            return temp;
        }

        private float getOutput() {
            return mOutput * outputScaler;
        }

        public void setDamping(float damping) {
            mLowPass.setCoefficient(1.0f - damping);
        }
    }

    private float mDecay;
    private float mLeftFeedback;
    private float mRightFeedback;
    private double mSize = 1.0;
    private double mPreviousTime = -1.0;

    private OnePoleLowPassFilter mBandwidthLowPass = new OnePoleLowPassFilter(BANDWIDTH);
    private AllPassDelay mDiffusion1 = new AllPassDelay(142, INPUT_DIFFUSION_1);
    private AllPassDelay mDiffusion2 = new AllPassDelay(107, INPUT_DIFFUSION_1);
    private AllPassDelay mDiffusion3 = new AllPassDelay(379, INPUT_DIFFUSION_2);
    private AllPassDelay mDiffusion4 = new AllPassDelay(277, INPUT_DIFFUSION_2);
    private ReverbSide mLeftSide;
    private ReverbSide mRightSide;


    /**
     * Create a PlateReverb with a default size of 1.0.
     */
    public PlateReverb() {
        this(1.0);
    }

    /**
     * This reverb uses multiple delay lines. The size parameter
     * scales the allocated size. A value of 1.0 is the default.
     * At low values the reverb will sound more metallic, like a comb filter.
     * At larger values it will sound more echoey.
     *
     * The size value will be clipped between 0.05 and 5.0.
     *
     * @param size adjust internal delay sizes
     */
    public PlateReverb(double size) {

        addPort(input = new UnitInputPort("Input"));

        size = Math.max(0.05, Math.min(5.0, size));
        mSize = size;
        addPort(time = new UnitInputPort("Time"));
        time.setup(0.01, 2.0, 30.0);
        addPort(damping = new UnitInputPort("Damping"));
        damping.setup(0.0001, DAMPING, 1.0);

        addPort(output = new UnitOutputPort(2,"Output"));

        // delay line sizes
        // These are from the original paper.
        // int[] zs = {142, 107, 379, 277, // diffusion
        //     672, 4453, 1800, 3720, // left
        //     908, 4217, 2656, 3163}; // right
        // These are aligned to nearby primes.
        int[] zs = {149, 107, 379, 277, // diffusion
            677, 4453, 1801, 3727, // left
            911, 4217, 2657, 3169}; // right

        mDiffusion1 = new AllPassDelay((int)(zs[0] * size), INPUT_DIFFUSION_1);
        mDiffusion2 = new AllPassDelay((int)(zs[1] * size), INPUT_DIFFUSION_1);
        mDiffusion3 = new AllPassDelay((int)(zs[2] * size), INPUT_DIFFUSION_2);
        mDiffusion4 = new AllPassDelay((int)(zs[3] * size), INPUT_DIFFUSION_2);
        mLeftSide = new ReverbSide((int)(zs[4] * size), (int)(zs[5] * size),
                (int)(zs[6] * size), (int)(zs[7] * size));
        mRightSide = new ReverbSide((int)(zs[8] * size), (int)(zs[9] * size),
                (int)(zs[10] * size), (int)(zs[11] * size));
        mLeftSide.setFrequency(0.7f, 44100.0f); // TODO use actual sample rate
         mRightSide.setFrequency(1.2f, 44100.0f); // TODO use actual sample rate
    }

    // Unfortunately, Java does not have a simple duple support.
    // So we return void and then get teh two values from the left and
    // right sides.
    private void process(float x) {
        x = mBandwidthLowPass.process(x);
        x = mDiffusion1.process(x);
        x = mDiffusion2.process(x);
        x = mDiffusion3.process(x);
        x = mDiffusion4.process(x);
        // left side of the figure eight uses right side feedback
        float leftSum = x + mRightFeedback;
        mLeftFeedback = mLeftSide.process(leftSum);
        // right side of the figure eight uses left side feedback
        float rightSum = x + mLeftFeedback;
        mRightFeedback = mRightSide.process(rightSum);
    }


    // This equation was derived from measuring the actual RT60 as a function
    // of size and decay.
    // time = size * (0.52 - (4.7 *  Math.log(1.0001 - (decay * decay))));
    // time/size = 0.52 - (4.7 *  Math.log(1.0001 - (decay * decay)))
    // time/size - 0.52 = -4.7 *  Math.log(1.0001 - (decay * decay))
    // (0.52 - (time/size))/ 4.7 = Math.log(1.0001 - (decay * decay))
    // Math.exp((0.52 - (time/size))/ 4.7) = 1.0001 - (decay * decay)
    // 1.001 - Math.exp((0.52 - (time/size))/ 4.7) = decay * decay
    // decay = Math.sqrt(1.001 - Math.exp((0.52 - (time/size))/ 4.7))
    private double convertTimeToDecay(double size, double time) {
        double exponent = (0.52 - (time / size))/ 4.7;
        double square = 1.001 - Math.exp(exponent); // TODO optimize
        double decay = Math.sqrt(Math.max(0.0, square)); // avoid sqrt(negative)
        return Math.min(MAX_DECAY, decay);
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] leftOutputs = output.getValues(0);
        double[] rightOutputs = output.getValues(1);

        double timeValue = (float) time.getValues()[0];
        if (timeValue != mPreviousTime) {
            mDecay = (float) convertTimeToDecay(mSize, timeValue);
            mPreviousTime = timeValue;
        }
        float dampingValue = (float) damping.getValues()[0];
        mLeftSide.setDamping(dampingValue);
        mRightSide.setDamping(dampingValue);
        for (int i = start; i < limit; i++) {
            process((float) inputs[i]);
            leftOutputs[i] = mLeftSide.getOutput();
            rightOutputs[i] = mRightSide.getOutput();
        }
    }

}
