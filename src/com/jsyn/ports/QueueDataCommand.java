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

import com.jsyn.data.SequentialData;
import com.softsynth.shared.time.ScheduledCommand;

/**
 * A command that can be used to queue SequentialData to a UnitDataQueuePort. Here is an example of
 * queuing data with a callback using this command.
 *
 * <pre>
 * <code>
 * 	// Queue an envelope with a completion callback.
 * 	QueueDataCommand command = envelopePlayer.dataQueue.createQueueDataCommand( envelope, 0,
 * 			envelope.getNumFrames() );
 * 	// Create an object to be called when the queued data is done.
 * 	TestQueueCallback callback = new TestQueueCallback();
 * 	command.setCallback( callback );
 * 	command.setNumLoops( 2 );
 * 	envelopePlayer.rate.set( 0.2 );
 * 	synth.queueCommand( command );
 *  </code>
 * </pre>
 *
 * The callback will be passed QueueDataEvents.
 *
 * <pre>
 * <code>
 * 	class TestQueueCallback implements UnitDataQueueCallback
 * 	{
 * 		public void started( QueueDataEvent event )
 * 		{
 * 			System.out.println("CALLBACK: Envelope started.");
 * 		}
 *
 * 		public void looped( QueueDataEvent event )
 * 		{
 * 			System.out.println("CALLBACK: Envelope looped.");
 * 		}
 *
 * 		public void finished( QueueDataEvent event )
 * 		{
 * 			System.out.println("CALLBACK: Envelope finished.");
 * 		}
 * 	}
 * </code>
 * </pre>
 *
 * @author Phil Burk 2009 Mobileer Inc
 */
public abstract class QueueDataCommand extends QueueDataEvent implements ScheduledCommand {

    protected SequentialDataCrossfade crossfadeData;
    protected SequentialData currentData;

    private static final long serialVersionUID = -1185274459972359536L;
    private UnitDataQueueCallback callback;

    public QueueDataCommand(UnitDataQueuePort port, SequentialData sequentialData, int startFrame,
            int numFrames) {
        super(port);

        if ((startFrame + numFrames) > sequentialData.getNumFrames()) {
            throw new IllegalArgumentException("tried to queue past end of data, " + (startFrame + numFrames));
        } else if (startFrame < 0) {
            throw new IllegalArgumentException("tried to queue before start of data, " + startFrame);
        }
        this.sequentialData = sequentialData;
        this.currentData = sequentialData;
        crossfadeData = new SequentialDataCrossfade();
        this.startFrame = startFrame;
        this.numFrames = numFrames;
    }

    @Override
    public abstract void run();

    /**
     * If true then this item will be skipped if other items are queued after it. This flag allows
     * you to queue lots of small pieces of sound without making the queue very long.
     *
     * @param skipIfOthers
     */
    public void setSkipIfOthers(boolean skipIfOthers) {
        this.skipIfOthers = skipIfOthers;
    }

    /**
     * If true then the queue will be cleared and this item will be started immediately. It is
     * better to use this flag than to clear the queue from the application because there could be a
     * gap before the next item is available. This is most useful when combined with
     * setCrossFadeIn().
     *
     * @param immediate
     */
    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public UnitDataQueueCallback getCallback() {
        return callback;
    }

    public void setCallback(UnitDataQueueCallback callback) {
        this.callback = callback;
    }

    public SequentialDataCrossfade getCrossfadeData() {
        return crossfadeData;
    }

    public void setCrossfadeData(SequentialDataCrossfade crossfadeData) {
        this.crossfadeData = crossfadeData;
    }

    public SequentialData getCurrentData() {
        return currentData;
    }

    public void setCurrentData(SequentialData currentData) {
        this.currentData = currentData;
    }

    /**
     * Stop the unit that contains this port after this command has finished.
     *
     * @param autoStop
     */
    public void setAutoStop(boolean autoStop) {
        this.autoStop = autoStop;
    }

    /**
     * Set how many time the block should be repeated after the first time. For example, if you set
     * numLoops to zero the block will only be played once. If you set numLoops to one the block
     * will be played twice.
     *
     * @param numLoops number of times to loop back
     */
    public void setNumLoops(int numLoops) {
        this.numLoops = numLoops;
    }

    /**
     * Number of frames to cross fade from the previous block to this block. This can be used to
     * avoid pops when making abrupt transitions. There must be frames available after the end of
     * the previous block to use for crossfading. The crossfade is linear.
     *
     * @param size
     */
    public void setCrossFadeIn(int size) {
        this.crossFadeIn = size;
    }

}
