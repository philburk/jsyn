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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;

/**
 * Checks that units inside a started Circuit are generated only once per block when their outputs
 * are also pulled by a downstream unit.
 * <p>
 * UnitGenerator.pullData() guards against running a unit twice in one block using lastFrameCount.
 * Circuit.generate() used to call generate() directly on its subunits, bypassing that guard.
 */
public class TestCircuitPullData extends NonRealTimeTestCase {

    /** Counts how many times generate() is called. */
    static class GenerateCounter extends UnitGenerator {
        final UnitOutputPort output;
        int count;

        GenerateCounter() {
            addPort(output = new UnitOutputPort("Output"));
        }

        @Override
        public void generate(int start, int limit) {
            count++;
        }
    }

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

    static class CounterCircuit extends Circuit {
        final GenerateCounter counter;

        CounterCircuit() {
            add(counter = new GenerateCounter());
        }
    }

    static class ReaderCircuit extends Circuit {
        final VariableRateMonoReader reader;

        ReaderCircuit() {
            add(reader = new VariableRateMonoReader());
        }
    }

    private void runBlocks(int numBlocks) throws InterruptedException {
        long endFrame = synthesisEngine.getFrameCount()
                + ((long) numBlocks * Synthesizer.FRAMES_PER_BLOCK);
        synthesisEngine.sleepUntil(endFrame / (double) synthesisEngine.getFrameRate());
    }

    @Test
    public void testUnitInStartedCircuitGeneratesOncePerBlock() throws InterruptedException {
        CounterCircuit circuit = new CounterCircuit();
        Recorder recorder = new Recorder();
        synthesisEngine.add(circuit);
        synthesisEngine.add(recorder);
        circuit.counter.output.connect(recorder.input);

        synthesisEngine.start();
        circuit.start();
        recorder.start();
        runBlocks(4); // let the start commands execute

        int countBefore = circuit.counter.count;
        long frameBefore = synthesisEngine.getFrameCount();
        runBlocks(100);
        // The engine may run a whole buffer of blocks at a time, so measure what actually ran.
        int numBlocks = (int) ((synthesisEngine.getFrameCount() - frameBefore)
                / Synthesizer.FRAMES_PER_BLOCK);
        int calls = circuit.counter.count - countBefore;

        assertEquals(numBlocks, calls, "Unit inside a started Circuit should generate once per block,"
                + " but generated " + calls + " times in " + numBlocks + " blocks.");
    }

    /** Control case: the same reader outside a Circuit plays continuously. */
    @Test
    public void testReaderOutsideCircuitIsContinuous() throws InterruptedException {
        VariableRateMonoReader reader = new VariableRateMonoReader();
        Recorder recorder = new Recorder();
        synthesisEngine.add(reader);
        synthesisEngine.add(recorder);
        reader.output.connect(recorder.input);

        checkRampPlayback(reader, recorder);
    }

    /** A reader inside a started Circuit used to skip 8 frames at every block boundary. */
    @Test
    public void testReaderInStartedCircuitIsContinuous() throws InterruptedException {
        ReaderCircuit circuit = new ReaderCircuit();
        Recorder recorder = new Recorder();
        synthesisEngine.add(circuit);
        synthesisEngine.add(recorder);
        circuit.reader.output.connect(recorder.input);

        synthesisEngine.start();
        circuit.start();
        checkRampPlayback(circuit.reader, recorder);
    }

    /**
     * Play a ramp at its natural rate. Each output frame should be exactly one ramp step above
     * the previous one.
     */
    private void checkRampPlayback(VariableRateMonoReader reader, Recorder recorder)
            throws InterruptedException {
        int numFrames = 4096;
        float step = 1.0f / numFrames;
        float[] data = new float[numFrames];
        for (int i = 0; i < numFrames; i++) {
            data[i] = i * step;
        }
        FloatSample sample = new FloatSample(data);

        reader.rate.set(sample.getFrameRate());
        reader.dataQueue.queue(sample);

        synthesisEngine.start();
        recorder.start();
        runBlocks(numFrames / Synthesizer.FRAMES_PER_BLOCK / 4);

        List<Double> recorded = recorder.recorded;
        // Skip the silence before the queued sample starts.
        int first = -1;
        for (int i = 0; i < recorded.size(); i++) {
            if (recorded.get(i) > 0.0) {
                first = i;
                break;
            }
        }
        assertTrue(first >= 0, "Reader produced no output.");
        int end = Math.min(recorded.size(), first + 256);
        assertTrue(end - first > 64, "Not enough output recorded.");

        for (int i = first + 1; i < end; i++) {
            double delta = recorded.get(i) - recorded.get(i - 1);
            assertTrue(Math.abs(delta - step) < step * 0.01, "Discontinuity at recorded frame " + i
                    + ": jumped " + (delta / step) + " ramp steps, expected 1.");
        }
    }
}
