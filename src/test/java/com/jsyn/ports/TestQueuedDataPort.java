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

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.data.SequentialData;
import com.jsyn.data.ShortSample;
import com.jsyn.unitgen.FixedRateMonoReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test sample and envelope queuing and looping.
 *
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TestQueuedDataPort {

    private static Synthesizer synth;
    private static final float[] floatData = {
            0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f
    };
    private static FloatSample floatSample;
    private static FixedRateMonoReader reader;

    @BeforeAll
    private static void setUp() {
        synth = JSyn.createSynthesizer();
        synth.setRealTime(false);
        synth.start();
    }

    @AfterAll
    private static void tearDown() {
        synth.stop();
    }

    private void queueDirect(UnitDataQueuePort port, SequentialData data, int startFrame,
            int numFrames) {
        queueDirect(port, data, startFrame, numFrames, 0);
    }

    private void queueDirect(UnitDataQueuePort port, SequentialData data, int startFrame,
            int numFrames, int numLoops) {
        QueueDataCommand command = port.createQueueDataCommand(data, startFrame, numFrames);
        command.setNumLoops(numLoops);
        port.addQueuedBlock(command);
    }

    @Test
    public void testQueueSingleShort() {
        short[] data = {
                234, -9876, 4567
        };
        ShortSample sample = new ShortSample(data.length, 1);
        sample.write(data);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        assertFalse(dataQueue.hasMore(), "start empty");

        queueDirect(dataQueue, sample, 0, data.length);
        checkQueuedData(data, dataQueue, 0, data.length);

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testQueueSingleFloat() {
        float[] data = {
                0.4f, 1.9f, 22.7f
        };
        FloatSample sample = new FloatSample(data.length, 1);
        sample.write(data);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        assertFalse(dataQueue.hasMore(), "start empty");

        queueDirect(dataQueue, sample, 0, data.length);
        checkQueuedData(data, dataQueue, 0, data.length);

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testQueueOutOfBounds() {
        float[] data = {
                0.4f, 1.9f, 22.7f
        };
        FloatSample sample = new FloatSample(data.length, 1);
        sample.write(data);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        boolean caught = false;
        try {
            queueDirect(dataQueue, sample, 0, sample.getNumFrames() + 1); // should cause an error!
        } catch(IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "expect exception when we go past end of the array");

        caught = false;
        try {
            queueDirect(dataQueue, sample, 1, sample.getNumFrames()); // should cause an error!
        } catch(IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "expect exception when we go past end of the array");

        caught = false;
        try {
            queueDirect(dataQueue, sample, -1, sample.getNumFrames()); // should cause an error!
        } catch(IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "expect exception when we start before beginning of the array");
    }

    @Test
    public void testQueueMultiple() {
        short[] data = {
                234, 17777, -9876, 4567, -14287
        };
        ShortSample sample = new ShortSample(data.length, 1);
        sample.write(data);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        assertFalse(dataQueue.hasMore(), "start empty");

        queueDirect(dataQueue, sample, 1, 3);
        queueDirect(dataQueue, sample, 0, 5);
        queueDirect(dataQueue, sample, 2, 2);

        checkQueuedData(data, dataQueue, 1, 3);
        checkQueuedData(data, dataQueue, 0, 5);
        checkQueuedData(data, dataQueue, 2, 2);

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testQueueNoLoops() throws InterruptedException {
        UnitDataQueuePort dataQueue = setupFloatSample();

        dataQueue.queueOn(floatSample, synth.createTimeStamp());
        // Advance synth so that the queue command propagates to the engine.
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        // play entire sample
        checkQueuedData(floatData, dataQueue, 0, floatData.length);

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testQueueLoopForever() throws InterruptedException {

        UnitDataQueuePort dataQueue = setupFloatSample();

        dataQueue.queue(floatSample, 0, 3);
        dataQueue.queueLoop(floatSample, 3, 4);

        // Advance synth so that the queue commands propagate to the engine.
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 0, 3);
        checkQueuedData(floatData, dataQueue, 3, 4);
        checkQueuedData(floatData, dataQueue, 3, 4);
        checkQueuedData(floatData, dataQueue, 3, 4);
        checkQueuedData(floatData, dataQueue, 3, 1);

        // queue final release
        dataQueue.queue(floatSample, 3, 5);
        synth.sleepUntil(synth.getCurrentTime() + 0.01);
        // current loop will finish
        checkQueuedData(floatData, dataQueue, 4, 3);
        // release portion will play
        checkQueuedData(floatData, dataQueue, 3, 5);

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testQueueLoopAtLeastOnce() throws InterruptedException {

        UnitDataQueuePort dataQueue = setupFloatSample();

        dataQueue.queue(floatSample, 0, 3);
        dataQueue.queueLoop(floatSample, 3, 2); // this should play at least once
        dataQueue.queue(floatSample, 5, 2);

        // Advance synth so that the queue commands propagate to the engine.
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 0, 3);
        checkQueuedData(floatData, dataQueue, 3, 2);
        checkQueuedData(floatData, dataQueue, 5, 2);

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testQueueNumLoops() throws InterruptedException {
        UnitDataQueuePort dataQueue = setupFloatSample();

        dataQueue.queue(floatSample, 0, 2);

        int numLoopsA = 5;
        dataQueue.queueLoop(floatSample, 2, 3, numLoopsA);

        dataQueue.queue(floatSample, 4, 2);

        int numLoopsB = 3;
        dataQueue.queueLoop(floatSample, 3, 4, numLoopsB);

        dataQueue.queue(floatSample, 5, 2);

        // Advance synth so that the queue commands propagate to the engine.
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 0, 2);
        for (int i = 0; i < (numLoopsA + 1); i++) {
            System.out.println("loop A #" + i);
            checkQueuedData(floatData, dataQueue, 2, 3);
        }
        checkQueuedData(floatData, dataQueue, 4, 2);
        for (int i = 0; i < (numLoopsB + 1); i++) {
            System.out.println("loop B #" + i);
            checkQueuedData(floatData, dataQueue, 3, 4);
        }

        checkQueuedData(floatData, dataQueue, 5, 2);

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    private UnitDataQueuePort setupFloatSample() {
        floatSample = new FloatSample(floatData.length, 1);
        floatSample.write(floatData);

        synth.add(reader = new FixedRateMonoReader());
        UnitDataQueuePort dataQueue = reader.dataQueue;
        assertFalse(dataQueue.hasMore(), "start empty");
        return dataQueue;
    }

    @Test
    public void testQueueSustainLoop() throws InterruptedException {

        UnitDataQueuePort dataQueue = setupFloatSample();

        // set up sustain loops ===========================
        floatSample.setSustainBegin(2);
        floatSample.setSustainEnd(4);
        floatSample.setReleaseBegin(-1);
        floatSample.setReleaseEnd(-1);

        dataQueue.queueOn(floatSample, synth.createTimeStamp());
        // Advance synth so that the queue command propagates to the engine.
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 0, 2);
        checkQueuedData(floatData, dataQueue, 2, 2);
        checkQueuedData(floatData, dataQueue, 2, 2);
        checkQueuedData(floatData, dataQueue, 2, 1); // looping

        dataQueue.queueOff(floatSample, true); // queue off in middle of loop
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 3, 5); // release
        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testQueueReleaseLoop() throws InterruptedException {
        UnitDataQueuePort dataQueue = setupFloatSample();

        // set up sustain loops ===========================
        floatSample.setSustainBegin(-1);
        floatSample.setSustainEnd(-1);
        floatSample.setReleaseBegin(4);
        floatSample.setReleaseEnd(6);

        dataQueue.queueOn(floatSample, synth.createTimeStamp());
        // Advance synth so that the queue command propagates to the engine.
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 0, 4);
        checkQueuedData(floatData, dataQueue, 4, 2);
        checkQueuedData(floatData, dataQueue, 4, 2);
        checkQueuedData(floatData, dataQueue, 4, 2); // looping in release cuz no
                                                     // sustain loop

        dataQueue.queueOff(floatSample, true); // queue off in middle of loop
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 4, 2);
        checkQueuedData(floatData, dataQueue, 4, 2); // still looping
        assertTrue(dataQueue.hasMore(), "end full");
    }

    @Test
    public void testQueueSustainReleaseLoops() throws InterruptedException {
        UnitDataQueuePort dataQueue = setupFloatSample();

        // set up sustain loops ===========================
        floatSample.setSustainBegin(2);
        floatSample.setSustainEnd(4);
        floatSample.setReleaseBegin(5);
        floatSample.setReleaseEnd(7);

        dataQueue.queueOn(floatSample, synth.createTimeStamp());
        // Advance synth so that the queue command propagates to the engine.
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 0, 4);
        checkQueuedData(floatData, dataQueue, 2, 2);
        checkQueuedData(floatData, dataQueue, 2, 1); // middle of sustain loop

        dataQueue.queueOff(floatSample, true); // queue off in middle of loop
        synth.sleepUntil(synth.getCurrentTime() + 0.01);

        checkQueuedData(floatData, dataQueue, 3, 2);
        checkQueuedData(floatData, dataQueue, 5, 2); // release loop
        checkQueuedData(floatData, dataQueue, 5, 2); // release loop
        assertTrue(dataQueue.hasMore(), "end full");
    }

    @Test
    private void checkQueuedData(short[] data, UnitDataQueuePort dataQueue, int offset,
            int numFrames) {
        for (int i = 0; i < numFrames; i++) {
            assertTrue(dataQueue.hasMore(), "got data");
            double value = dataQueue.readNextMonoDouble(synth.getFramePeriod());
            assertEquals(data[i + offset] / 32768.0, value, 0.0001, "data matches");
        }
    }

    private void checkQueuedData(float[] data, UnitDataQueuePort dataQueue, int offset,
            int numFrames) {
        for (int i = 0; i < numFrames; i++) {
            assertTrue(dataQueue.hasMore(), "got data");
            double value = dataQueue.readNextMonoDouble(synth.getFramePeriod());
            assertEquals(data[i + offset], value, 0.0001, "data matches");
        }
    }

    static class TestQueueCallback implements UnitDataQueueCallback {
        boolean gotStarted = false;
        boolean gotLooped = false;
        boolean gotFinished = false;
        QueueDataEvent lastEvent;

        @Override
        public void started(QueueDataEvent event) {
            System.out.println("Callback started.");
            gotStarted = true;
            lastEvent = event;
        }

        @Override
        public void looped(QueueDataEvent event) {
            System.out.println("Callback looped.");
            gotLooped = true;
            lastEvent = event;
        }

        @Override
        public void finished(QueueDataEvent event) {
            System.out.println("Callback finished.");
            gotFinished = true;
            lastEvent = event;
        }
    }

    @Test
    public void testQueueCallback() {
        float[] data = {
                0.2f, -8.9f, 2.7f
        };
        FloatSample sample = new FloatSample(data.length, 1);
        sample.write(data);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        assertFalse(dataQueue.hasMore(), "start empty");

        // Create an object to be called when the queued data is done.
        TestQueueCallback callback = new TestQueueCallback();

        QueueDataCommand command = dataQueue.createQueueDataCommand(sample, 0, data.length);
        command.setCallback(callback);
        command.setNumLoops(2);
        dataQueue.addQueuedBlock(command);

        // Check to see if flags get set true by callback.
        dataQueue.firePendingCallbacks();
        assertFalse(callback.gotStarted, "not started yet");
        assertFalse(callback.gotLooped, "not looped yet");
        assertFalse(callback.gotFinished, "not finished yet");

        checkQueuedData(data, dataQueue, 0, 1);
        dataQueue.firePendingCallbacks();
        assertTrue(callback.gotStarted, "should be started now");
        assertFalse(callback.gotLooped, "not looped yet");
        assertFalse(callback.gotFinished, "not finished yet");
        assertEquals(dataQueue, callback.lastEvent.getSource(), "check source of event");
        assertEquals(sample, callback.lastEvent.getSequentialData(), "check sample");
        assertEquals(2, callback.lastEvent.getLoopsLeft(), "check loopCount");

        checkQueuedData(data, dataQueue, 1, data.length - 1);
        dataQueue.firePendingCallbacks();
        assertTrue(callback.gotLooped, "should be looped now");
        assertEquals(1, callback.lastEvent.getLoopsLeft(), "check loopCount");
        assertFalse(callback.gotFinished, "not finished yet");

        checkQueuedData(data, dataQueue, 0, data.length);
        dataQueue.firePendingCallbacks();
        assertEquals(0, callback.lastEvent.getLoopsLeft(), "check loopCount");

        checkQueuedData(data, dataQueue, 0, data.length);
        dataQueue.firePendingCallbacks();
        assertTrue(callback.gotFinished, "should be finished now");

        assertFalse(dataQueue.hasMore(), "end empty");
    }

    @Test
    public void testImmediate() {
        float[] data = {
                0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f
        };
        FloatSample sample = new FloatSample(data.length, 1);
        sample.write(data);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        dataQueue.queue(sample);

        // Only play some of the data then interrupt it with an immediate block.
        checkQueuedData(data, dataQueue, 0, 3);

        QueueDataCommand command = dataQueue.createQueueDataCommand(sample, 7, 3);
        command.setImmediate(true);
        command.run(); // execute "immediate" operation and add to block list

        // Should already be in new data.
        checkQueuedData(data, dataQueue, 7, 3);
    }

    @Test
    public void testCrossFade() {
        float[] data1 = {
                0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f
        };
        float[] data2 = {
                20.0f, 19.0f, 18.0f, 17.0f, 16.0f, 15.0f, 14.0f, 13.0f, 12.0f, 11.0f
        };
        FloatSample sample1 = new FloatSample(data1);
        FloatSample sample2 = new FloatSample(data2);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        dataQueue.queue(sample1, 0, 4);

        QueueDataCommand command = dataQueue.createQueueDataCommand(sample2, 1, 8);
        command.setCrossFadeIn(3);
        command.run(); // execute "immediate" operation and add to block list

        // Only play some of the data then crossfade to another sample.
        checkQueuedData(data1, dataQueue, 0, 4);

        for (int i = 0; i < 3; i++) {
            double factor = i / 3.0;
            double value = ((1.0 - factor) * data1[i + 4]) + (factor * data2[i + 1]);
            System.out.println("i = " + i + ", factor = " + factor + ", value = " + value);

            double actual = dataQueue.readNextMonoDouble(synth.getFramePeriod());
            assertEquals(value, actual, 0.00001, "crossfade " + i);
        }

        // Should already be in new data.
        checkQueuedData(data2, dataQueue, 4, 5);
    }

    @Test
    public void testImmediateCrossFade() {
        float[] data1 = {
                0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f
        };
        float[] data2 = {
                20.0f, 19.0f, 18.0f, 17.0f, 16.0f, 15.0f, 14.0f, 13.0f, 12.0f, 11.0f
        };
        FloatSample sample1 = new FloatSample(data1);
        FloatSample sample2 = new FloatSample(data2);

        UnitDataQueuePort dataQueue = new UnitDataQueuePort("test");
        dataQueue.queue(sample1, 0, 4);

        // Only play some of the data then crossfade to another sample.
        int beforeInterrupt = 2;
        checkQueuedData(data1, dataQueue, 0, beforeInterrupt);

        QueueDataCommand command = dataQueue.createQueueDataCommand(sample2, 1, 8);
        command.setImmediate(true);
        command.setCrossFadeIn(3);
        command.run(); // execute "immediate" operation and add to block list

        for (int i = 0; i < 3; i++) {
            double factor = i / 3.0;
            double value = ((1.0 - factor) * data1[i + beforeInterrupt]) + (factor * data2[i + 1]);
            System.out.println("i = " + i + ", factor = " + factor + ", value = " + value);

            double actual = dataQueue.readNextMonoDouble(synth.getFramePeriod());
            assertEquals(value, actual, 0.00001, "crossfade " + i);
        }

        // Should already be in new data.
        checkQueuedData(data2, dataQueue, 4, 5);
    }
}
