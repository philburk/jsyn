/*
 * Copyright 1998 Phil Burk, Mobileer Inc
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

package com.softsynth.math;

/**
 * Miscellaneous math functions useful in Audio
 *
 * @author (C) 1998 Phil Burk
 */
public class AudioMath {
    // use scalar to convert natural log to log_base_10
    private final static double a2dScalar = 20.0 / Math.log(10.0);
    public static final int CONCERT_A_PITCH = 69;
    public static final double CONCERT_A_FREQUENCY = 440.0;
    private static double mConcertAFrequency = CONCERT_A_FREQUENCY;

    /**
     * Convert amplitude to decibels. 1.0 is zero dB. 0.5 is -6.02 dB.
     */
    public static double amplitudeToDecibels(double amplitude) {
        double db = Math.log(amplitude) * a2dScalar;
        return db;
    }

    /**
     * Convert decibels to amplitude. Zero dB is 1.0 and -6.02 dB is 0.5.
     */
    public static double decibelsToAmplitude(double decibels) {
        double amp = Math.pow(10.0, decibels / 20.0);
        return amp;
    }

    /**
     * Calculate MIDI pitch based on frequency in Hertz. Middle C is 60.0.
     */
    public static double frequencyToPitch(double frequency) {
        return CONCERT_A_PITCH + 12 * Math.log(frequency / mConcertAFrequency) / Math.log(2.0);
    }

    /**
     * Calculate frequency in Hertz based on MIDI pitch. Middle C is 60.0. You can use fractional
     * pitches so 60.5 would give you a pitch half way between C and C#.
     */
    public static double pitchToFrequency(double pitch) {
        return mConcertAFrequency * Math.pow(2.0, ((pitch - CONCERT_A_PITCH) * (1.0 / 12.0)));
    }

    /**
     * This can be used to globally adjust the tuning in JSyn from Concert A at 440.0 Hz to
     * a slightly different frequency. Some orchestras use a higher frequency, eg. 441.0.
     * This value will be used by pitchToFrequency() and frequencyToPitch().
     *
     * @param concertAFrequency
     */
    public static void setConcertAFrequency(double concertAFrequency) {
        mConcertAFrequency = concertAFrequency;
    }

    public static double getConcertAFrequency() {
        return mConcertAFrequency;
    }

    /** Convert a delta value in semitones to a frequency multiplier.
     * @param semitones
     * @return scaler For example 2.0 for an input of 12.0 semitones.
     */
    public static double semitonesToFrequencyScaler(double semitones) {
        return Math.pow(2.0, semitones / 12.0);
    }
}
