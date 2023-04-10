/*
 * Copyright 2023 Phil Burk, Mobileer Inc
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

package com.jsyn.examples;

/**
 * Experiment with Cordic oscillators.
 * Implement using float and double values internally.
 */
class CordicOscillatorFloat {
    private float mCosPhi;
    private float x;
    private float y = -1.0f;
    private float mSinPhi;

    CordicOscillatorFloat() {
        setFrequency(441.0, 44100.0);
    }

    public void setFrequency(double frequency, double sampleRate) {
        double radians = frequency * Math.PI * 2.0 / sampleRate;
        mCosPhi = (float) Math.cos(radians);
        mSinPhi = (float) Math.sin(radians);
    }

    public double generate() {
        float x2 = x * mCosPhi - y * mSinPhi;
        float y2 = y * mCosPhi + x * mSinPhi;
        x = Math.min(x2, 1.0f);
        y = y2;
        return x;
    }
}


class CordicOscillator {
    private double mCosPhi;
    private double x;
    private double y = -1.0;
    private double mSinPhi;

    CordicOscillator() {
        setFrequency(441.0, 44100.0);
    }

    public void setFrequency(double frequency, double sampleRate) {
        double radians = frequency * Math.PI * 2.0 / sampleRate;
        mCosPhi = Math.cos(radians);
        mSinPhi = Math.sin(radians);
    }

    public double generate() {
        double x2 = x * mCosPhi - y * mSinPhi;
        double y2 = y * mCosPhi + x * mSinPhi;
        x = x2; // Math.min(x2, 1.0);
        y = y2;
        return x;
    }
}

public class InvestigateCordic
{
    public void test() {
        CordicOscillator oscillator = new CordicOscillator();
        oscillator.setFrequency(1.0, 44100.0);
        for (int i = 0; i < 100; i++) {
            double x = oscillator.generate();
            System.out.println("x = " + x);
        }
        double peak = 0.0;
        for (int n = 0; n < 200; n++) {
            peak = 0.0;
            for (int i = 0; i < 100000000; i++) {
                double x = oscillator.generate();
                if (x > peak) {
                    peak = x;
                }
            }
            System.out.println(n + ": peak = " + peak);
        }
    }

    public static void main(String[] args) {
        new InvestigateCordic().test();
        System.exit(0);
    }
}
