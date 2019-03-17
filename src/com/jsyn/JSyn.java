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

package com.jsyn;

import java.sql.Date;
import java.util.GregorianCalendar;

import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.engine.SynthesisEngine;

/**
 * JSyn Synthesizer for Java. Use this factory class to create a synthesizer. This code demonstrates
 * how to start playing a sine wave:
 *
 * <pre><code>
	// Create a context for the synthesizer.
	synth = JSyn.createSynthesizer();

	// Start synthesizer using default stereo output at 44100 Hz.
	synth.start();

	// Add a tone generator.
	synth.add( osc = new SineOscillator() );
	// Add a stereo audio output unit.
	synth.add( lineOut = new LineOut() );

	// Connect the oscillator to both channels of the output.
	osc.output.connect( 0, lineOut.input, 0 );
	osc.output.connect( 0, lineOut.input, 1 );

	// Set the frequency and amplitude for the sine wave.
	osc.frequency.set( 345.0 );
	osc.amplitude.set( 0.6 );

	// We only need to start the LineOut. It will pull data from the oscillator.
	lineOut.start();
</code> </pre>
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class JSyn {
    // Update these for every release.
    private final static int VERSION_MAJOR = 16;
    private final static int VERSION_MINOR = 8;
    private final static int VERSION_REVISION = 0;
    public final static int BUILD_NUMBER = 463;
    private final static long BUILD_TIME = new GregorianCalendar(2017,
            GregorianCalendar.OCTOBER, 16).getTime().getTime();

    public final static String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "."
            + VERSION_REVISION;
    public final static int VERSION_CODE = (VERSION_MAJOR << 16) + (VERSION_MINOR << 8)
            + VERSION_REVISION;
    public final static String VERSION_TEXT = "V" + VERSION + " (build " + BUILD_NUMBER + ", "
            + (new Date(BUILD_TIME)) + ")";

    public static Synthesizer createSynthesizer() {
        return new SynthesisEngine();
    }

    public static Synthesizer createSynthesizer(AudioDeviceManager audioDeviceManager) {
        return new SynthesisEngine(audioDeviceManager);
    }
}
