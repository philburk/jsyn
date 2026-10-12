/*
 * Copyright 2026 Phil Burk, Mobileer Inc
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.jsyn.data.FloatSample;
import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.UnitInputPort;

/**
 * Tests the tuning, decay and auto disable of FilterComb and the string resonators.
 */
public class TestStringResonator extends NonRealTimeTestCase {

    /** Records every sample that arrives on its input. */
    static class Recorder extends UnitGenerator {
        final UnitInputPort input;
        final List<Double> recorded = new ArrayList<>();

        Recorder() {
            addPort(input = new UnitInputPort("Input"));
        }

        @Override
        public void generate(int start, int limit) {
            double[] values = input.getValues();
            for (int i = start; i < limit; i++) {
                recorded.add(values[i]);
            }
        }
    }

    /** Complex value of one frequency component. */
    static class Component {
        final double real;
        final double imaginary;

        Component(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }

        double magnitude() {
            return Math.hypot(real, imaginary);
        }

        double phase() {
            return Math.atan2(imaginary, real);
        }
    }

    /** Use a fresh engine, for example for each frequency in a loop. */
    private void resetEngine() {
        synthesisEngine.stop();
        synthesisEngine = new SynthesisEngine();
        synthesisEngine.setRealTime(false);
    }

    private void advance(double seconds) throws InterruptedException {
        synthesisEngine.sleepUntil(synthesisEngine.getCurrentTime() + seconds);
    }

    /** One period of zero-mean noise, like the classic Karplus-Strong excitation. */
    private FloatSample makeBurst(double frequency) {
        Random random = new Random(1234);
        int numFrames = (int) (synthesisEngine.getFrameRate() / frequency);
        float[] data = new float[numFrames];
        double sum = 0.0;
        for (int i = 0; i < numFrames; i++) {
            data[i] = random.nextFloat() - 0.5f;
            sum += data[i];
        }
        float mean = (float) (sum / numFrames);
        for (int i = 0; i < numFrames; i++) {
            data[i] -= mean;
        }
        return new FloatSample(data);
    }

    /**
     * Excite the resonator and record its output. The brightness is only used by a
     * StringResonator.
     */
    private List<Double> record(FilterComb resonator, double frequency, double decayTime,
            double brightness, double seconds) throws InterruptedException {
        FixedRateMonoReader exciter = new FixedRateMonoReader();
        Recorder recorder = new Recorder();
        synthesisEngine.add(exciter);
        synthesisEngine.add(resonator);
        synthesisEngine.add(recorder);
        exciter.output.connect(resonator.input);
        resonator.output.connect(recorder.input);

        resonator.frequency.set(frequency);
        resonator.decay.set(decayTime);
        if (resonator instanceof StringResonator) {
            ((StringResonator) resonator).brightness.set(brightness);
        }
        exciter.dataQueue.queue(makeBurst(frequency));

        synthesisEngine.start();
        recorder.start();
        advance(seconds);
        return recorder.recorded;
    }

    /**
     * Measure the component at the frequency using a Hann window that is several periods long.
     * The phase is relative to frame zero, so phases from different windows can be compared.
     */
    private Component measureComponent(List<Double> data, int startFrame, double frequency) {
        double frameRate = synthesisEngine.getFrameRate();
        int windowSize = (int) Math.round(8 * frameRate / frequency);
        double real = 0.0;
        double imaginary = 0.0;
        for (int n = 0; n < windowSize; n++) {
            double window = 0.5 - (0.5 * Math.cos(2.0 * Math.PI * n / windowSize));
            int frame = startFrame + n;
            double angle = 2.0 * Math.PI * frequency * frame / frameRate;
            double sample = data.get(frame) * window;
            real += sample * Math.cos(angle);
            imaginary -= sample * Math.sin(angle);
        }
        return new Component(real, imaginary);
    }

    /** Measure the frequency from how far the phase of the fundamental drifts between two windows. */
    private double measureCentsError(FilterComb resonator, double frequency)
            throws InterruptedException {
        List<Double> data = record(resonator, frequency, 5.0, 0.5, 0.5);
        double frameRate = synthesisEngine.getFrameRate();
        int start1 = (int) (0.05 * frameRate);
        int start2 = (int) (0.30 * frameRate);
        Component first = measureComponent(data, start1, frequency);
        Component second = measureComponent(data, start2, frequency);

        double phaseDrift = second.phase() - first.phase();
        while (phaseDrift > Math.PI) phaseDrift -= 2.0 * Math.PI;
        while (phaseDrift < -Math.PI) phaseDrift += 2.0 * Math.PI;
        double elapsed = (start2 - start1) / frameRate;
        double frequencyError = phaseDrift / (2.0 * Math.PI * elapsed);
        return 1200.0 * Math.log((frequency + frequencyError) / frequency) / Math.log(2.0);
    }

    private void checkTuning(String name, Supplier<FilterComb> create)
            throws InterruptedException {
        double[] frequencies = {
                55.0, 110.0, 220.0, 445.5, 880.0, 1234.5, 2000.0
        };
        for (double frequency : frequencies) {
            resetEngine();
            double cents = measureCentsError(create.get(), frequency);
            System.out.println(name + " at " + frequency + " Hz: error = " + cents + " cents");
            assertTrue(Math.abs(cents) < 0.1,
                    name + " at " + frequency + " Hz is " + cents + " cents out of tune");
        }
    }

    @Test
    public void testFixedTuning() throws InterruptedException {
        checkTuning("Fixed", FixedStringResonator::new);
    }

    @Test
    public void testVariableTuning() throws InterruptedException {
        checkTuning("Variable", VariableStringResonator::new);
    }

    @Test
    public void testCombAllPassTuning() throws InterruptedException {
        checkTuning("Comb AllPass", () -> new FilterComb(FilterComb.Interpolation.ALL_PASS));
    }

    @Test
    public void testCombLagrangeTuning() throws InterruptedException {
        checkTuning("Comb Lagrange", () -> new FilterComb(FilterComb.Interpolation.LAGRANGE));
    }

    /**
     * The fundamental should drop 30 dB in half of the decay time. The decay should not depend on
     * the brightness.
     */
    private void checkDecay(String name, double[] brightnessValues, Supplier<FilterComb> create)
            throws InterruptedException {
        double frequency = 220.0;
        double[] decayTimes = {
                0.5, 3.0
        };
        for (double decayTime : decayTimes) {
            for (double brightness : brightnessValues) {
                resetEngine();
                List<Double> data = record(create.get(), frequency, decayTime, brightness,
                        0.15 + (decayTime / 2));
                double frameRate = synthesisEngine.getFrameRate();
                int start1 = (int) (0.05 * frameRate);
                int start2 = start1 + (int) (0.5 * decayTime * frameRate);
                double before = measureComponent(data, start1, frequency).magnitude();
                double after = measureComponent(data, start2, frequency).magnitude();
                double dropDb = 20.0 * Math.log10(before / after);
                System.out.println(name + " decay " + decayTime + ", brightness " + brightness
                        + ": fundamental dropped " + dropDb + " dB in " + (decayTime / 2)
                        + " seconds");
                assertTrue(Math.abs(dropDb - 30.0) < 0.2, name + " with decay " + decayTime
                        + " and brightness " + brightness + " dropped " + dropDb
                        + " dB, expected 30");
            }
        }
    }

    private static final double[] ALL_BRIGHTNESS = {
            0.0, 0.5, 1.0
    };

    /** A comb has no brightness, so only run it once per decay time. */
    private static final double[] NO_BRIGHTNESS = {
            0.5
    };

    @Test
    public void testFixedDecay() throws InterruptedException {
        checkDecay("Fixed", ALL_BRIGHTNESS, FixedStringResonator::new);
    }

    @Test
    public void testVariableDecay() throws InterruptedException {
        checkDecay("Variable", ALL_BRIGHTNESS, VariableStringResonator::new);
    }

    @Test
    public void testCombDecay() throws InterruptedException {
        checkDecay("Comb AllPass", NO_BRIGHTNESS,
                () -> new FilterComb(FilterComb.Interpolation.ALL_PASS));
        checkDecay("Comb Lagrange", NO_BRIGHTNESS,
                () -> new FilterComb(FilterComb.Interpolation.LAGRANGE));
    }

    @Test
    public void testAutoDisable() throws InterruptedException {
        final FixedRateMonoReader exciter = new FixedRateMonoReader();
        final FixedStringResonator resonator = new FixedStringResonator();
        Recorder recorder = new Recorder();
        synthesisEngine.add(exciter);
        synthesisEngine.add(resonator);
        synthesisEngine.add(recorder);
        exciter.output.connect(resonator.input);
        resonator.output.connect(recorder.input);

        resonator.frequency.set(220.0);
        resonator.decay.set(0.5);
        resonator.setupAutoDisable();
        assertFalse(resonator.isEnabled(), "Resonator should start disabled");

        synthesisEngine.start();
        recorder.start();
        advance(0.05);
        assertFalse(resonator.isEnabled(), "Resonator should stay disabled until excited");

        // Enable and excite together, like a noteOn would.
        final FloatSample burst = makeBurst(220.0);
        synthesisEngine.queueCommand(() -> {
            resonator.setEnabled(true);
            exciter.dataQueue.queue(burst);
        });
        advance(0.2);
        assertTrue(resonator.isEnabled(), "Resonator should stay enabled while ringing");

        // The level falls below -90 dB after about 1.5 * decay.
        advance(1.0);
        assertFalse(resonator.isEnabled(), "Resonator should disable itself when quiet");

        // It wakes up for the next note.
        recorder.recorded.clear();
        synthesisEngine.queueCommand(() -> {
            resonator.setEnabled(true);
            exciter.dataQueue.queue(burst);
        });
        advance(0.1);
        assertTrue(resonator.isEnabled(), "Resonator should ring again after being enabled");
        double peak = 0.0;
        for (double value : recorder.recorded) {
            peak = Math.max(peak, Math.abs(value));
        }
        assertTrue(peak > 0.1, "Resonator should make sound after being enabled, peak = " + peak);
    }
}
