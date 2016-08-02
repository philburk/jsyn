package com.jsyn.unitgen;

import com.softsynth.math.AudioMath;

public class PitchToFrequency extends PowerOfTwo {

    public PitchToFrequency() {
        input.setup(0.0, 60.0, 127.0);
    }

    /**
     * Convert from MIDI pitch to an octave offset from Concert A.
     */
    @Override
    public double adjustInput(double in) {
        return (in - AudioMath.CONCERT_A_PITCH) * (1.0/12.0);
    }

    /**
     * Convert scaler to a frequency relative to Concert A.
     */
    @Override
    public double adjustOutput(double out) {
        return out * AudioMath.getConcertAFrequency();
    }
}
