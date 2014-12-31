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

package com.jsyn.examples;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.ports.QueueDataCommand;
import com.jsyn.ports.QueueDataEvent;
import com.jsyn.ports.UnitDataQueueCallback;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateDataReader;
import com.jsyn.unitgen.VariableRateMonoReader;

/**
 * Use a UnitDataQueueCallback to notify us of the envelope's progress. Modulate the amplitude of an
 * oscillator using a segmented envelope.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class PlaySegmentedEnvelopeCallback {
    private Synthesizer synth;
    private UnitOscillator osc;
    private LineOut lineOut;
    private SegmentedEnvelope envelope;
    private VariableRateDataReader envelopePlayer;

    private void test() {
        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();
        // Add a tone generator.
        synth.add(osc = new SawtoothOscillatorBL());
        // Add an envelope player.
        synth.add(envelopePlayer = new VariableRateMonoReader());

        // Create an envelope consisting of (duration,value) pairs.
        double[] pairs = {
                0.1, 1.0, 0.2, 0.5, 0.6, 0.0
        };
        envelope = new SegmentedEnvelope(pairs);

        // Add an output mixer.
        synth.add(lineOut = new LineOut());
        envelopePlayer.output.connect(osc.amplitude);
        // Connect the oscillator to the output.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the other
        // units.
        lineOut.start();

        try {
            // Queue an envelope with callbacks.
            QueueDataCommand command = envelopePlayer.dataQueue.createQueueDataCommand(envelope, 0,
                    envelope.getNumFrames());
            // Create an object to be called when the queued data is done.
            TestQueueCallback callback = new TestQueueCallback();
            command.setCallback(callback);
            command.setNumLoops(2);
            envelopePlayer.rate.set(0.2);
            synth.queueCommand(command);
            synth.sleepFor(20.0);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Stop everything.
        synth.stop();
    }

    class TestQueueCallback implements UnitDataQueueCallback {
        @Override
        public void started(QueueDataEvent event) {
            System.out.println("CALLBACK: Envelope started.");
        }

        @Override
        public void looped(QueueDataEvent event) {
            System.out.println("CALLBACK: Envelope looped.");
        }

        @Override
        public void finished(QueueDataEvent event) {
            System.out.println("CALLBACK: Envelope finished.");
            // Queue the envelope again at a faster rate.
            // (If this hangs we may have hit a deadlock.)
            envelopePlayer.rate.set(2.0);
            envelopePlayer.dataQueue.queue(envelope);
        }
    }

    public static void main(String[] args) {
        new PlaySegmentedEnvelopeCallback().test();
    }
}
