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

import java.util.LinkedList;

import com.jsyn.data.SequentialData;
import com.jsyn.exceptions.ChannelMismatchException;
import com.softsynth.shared.time.ScheduledCommand;
import com.softsynth.shared.time.TimeStamp;

/**
 * Queue for SequentialData, samples or envelopes
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class UnitDataQueuePort extends UnitPort {
    private final LinkedList<QueuedBlock> blocks = new LinkedList<QueuedBlock>();
    private QueueDataCommand currentBlock;
    private int frameIndex;
    private int numChannels = 1;
    private double normalizedRate;
    private long framesMoved;
    private boolean autoStopPending;
    private boolean targetValid;
    private QueueDataCommand finishingBlock;
    private QueueDataCommand loopingBlock;
    public static final int LOOP_IF_LAST = -1;

    public UnitDataQueuePort(String name) {
        super(name);
    }

    /** Hold a reference to part of a sample. */
    @SuppressWarnings("serial")
    private class QueuedBlock extends QueueDataCommand {

        public QueuedBlock(SequentialData queueableData, int startFrame, int numFrames) {
            super(UnitDataQueuePort.this, queueableData, startFrame, numFrames);
        }

        @Override
        public void run() {
            synchronized (blocks) {
                // Remove last block if it can be skipped.
                if (blocks.size() > 0) {
                    QueueDataEvent lastBlock = blocks.getLast();
                    if (lastBlock.isSkipIfOthers()) {
                        blocks.removeLast();
                    }
                }

                // If we are crossfading then figure out where to crossfade
                // from.
                if (getCrossFadeIn() > 0) {
                    if (isImmediate()) {
                        // Queue will be cleared so fade in from current.
                        if (currentBlock != null) {
                            setupCrossFade(currentBlock, frameIndex, this);
                        }
                        // else nothing is playing so don't crossfade.
                    } else {
                        QueueDataCommand endBlock = getEndBlock();
                        if (endBlock != null) {
                            setupCrossFade(endBlock,
                                    endBlock.getStartFrame() + endBlock.getNumFrames(), this);
                        }
                    }
                }

                if (isImmediate()) {
                    clearQueue();
                }

                blocks.add(this);
            }
        }
    }

    // FIXME - determine crossfade on any transition between blocks or when looping back.

    protected void setupCrossFade(QueueDataCommand sourceCommand, int sourceStartIndex,
            QueueDataCommand targetCommand) {
        int crossFrames = targetCommand.getCrossFadeIn();
        SequentialData sourceData = sourceCommand.getCurrentData();
        SequentialData targetData = targetCommand.getCurrentData();
        int remainingSource = sourceData.getNumFrames() - sourceStartIndex;
        // clip to end of source
        if (crossFrames > remainingSource)
            crossFrames = remainingSource;
        if (crossFrames > 0) {
            // The SequentialDataCrossfade should continue to the end of the target
            // so that we can crossfade from it to the target.
            int remainingTarget = targetData.getNumFrames() - targetCommand.getStartFrame();
            targetCommand.crossfadeData.setup(sourceData, sourceStartIndex, crossFrames,
                    targetData, targetCommand.getStartFrame(), remainingTarget);
            targetCommand.currentData = targetCommand.crossfadeData;
            targetCommand.startFrame = 0;
        }
    }

    public QueueDataCommand createQueueDataCommand(SequentialData queueableData) {
        return createQueueDataCommand(queueableData, 0, queueableData.getNumFrames());
    }

    public QueueDataCommand createQueueDataCommand(SequentialData queueableData, int startFrame,
            int numFrames) {
        if (queueableData.getChannelsPerFrame() != UnitDataQueuePort.this.numChannels) {
            throw new ChannelMismatchException("Tried to queue "
                    + queueableData.getChannelsPerFrame() + " channel data to a " + numChannels
                    + " channel port.");
        }
        return new QueuedBlock(queueableData, startFrame, numFrames);
    }

    public QueueDataCommand getEndBlock() {
        if (blocks.size() > 0) {
            return blocks.getLast();
        } else if (currentBlock != null) {
            return currentBlock;
        } else {
            return null;
        }
    }

    public void setCurrentBlock(QueueDataCommand currentBlock) {
        this.currentBlock = currentBlock;
    }

    public void firePendingCallbacks() {
        if (loopingBlock != null) {
            if (loopingBlock.getCallback() != null) {
                loopingBlock.getCallback().looped(currentBlock);
            }
            loopingBlock = null;
        }
        if (finishingBlock != null) {
            if (finishingBlock.getCallback() != null) {
                finishingBlock.getCallback().finished(currentBlock); // FIXME - Should this pass
                                                                     // finishingBlock?!
            }
            finishingBlock = null;
        }
    }

    public boolean hasMore() {
        return (currentBlock != null) || (blocks.size() > 0);
    }

    private void checkBlock() {
        if (currentBlock == null) {
            synchronized (blocks) {
                setCurrentBlock(blocks.remove());
                frameIndex = currentBlock.getStartFrame();
                currentBlock.loopsLeft = currentBlock.getNumLoops();
                if (currentBlock.getCallback() != null) {
                    currentBlock.getCallback().started(currentBlock);
                }
            }
        }
    }

    private void advanceFrameIndex() {
        frameIndex += 1;
        framesMoved += 1;
        // Are we done with this block?
        if (frameIndex >= (currentBlock.getStartFrame() + currentBlock.getNumFrames())) {
            // Should we loop on this block based on a counter?
            if (currentBlock.loopsLeft > 0) {
                currentBlock.loopsLeft -= 1;
                loopToStart();
            }
            // Should we loop forever on this block?
            else if ((blocks.size() == 0) && (currentBlock.loopsLeft < 0)) {
                loopToStart();
            }
            // We are done.
            else {
                if (currentBlock.isAutoStop()) {
                    autoStopPending = true;
                }
                finishingBlock = currentBlock;
                setCurrentBlock(null);
                // System.out.println("advanceFrameIndex: currentBlock set null");
            }
        }
    }

    private void loopToStart() {
        if (currentBlock.getCrossFadeIn() > 0) {
            setupCrossFade(currentBlock, frameIndex, currentBlock);
        }
        frameIndex = currentBlock.getStartFrame();
        loopingBlock = currentBlock;
    }

    public double getNormalizedRate() {
        return normalizedRate;
    }

    public double readCurrentChannelDouble(int channelIndex) {
        return currentBlock.currentData.readDouble((frameIndex * numChannels) + channelIndex);
    }

    public void writeCurrentChannelDouble(int channelIndex, double value) {
        currentBlock.currentData.writeDouble((frameIndex * numChannels) + channelIndex, value);
    }

    public void beginFrame(double synthesisPeriod) {
        checkBlock();
        normalizedRate = currentBlock.currentData.getRateScaler(frameIndex, synthesisPeriod);
    }

    public void endFrame() {
        advanceFrameIndex();
        targetValid = true;
    }

    public double readNextMonoDouble(double synthesisPeriod) {
        beginFrame(synthesisPeriod);
        double value = currentBlock.currentData.readDouble(frameIndex);
        endFrame();
        return value;
    }

    /** Write directly to the port queue. This is only called by unit tests! */
    protected void addQueuedBlock(QueueDataEvent block) {
        blocks.add((QueuedBlock) block);
    }

    /** Clear the queue. Internal use only. */
    protected void clearQueue() {
        synchronized (blocks) {
            blocks.clear();
            setCurrentBlock(null);
            targetValid = false;
            autoStopPending = false;
        }
    }

    class ClearQueueCommand implements ScheduledCommand {
        @Override
        public void run() {
            clearQueue();
        }
    }

    /** Queue the data to the port at a future time. */
    public void queue(SequentialData queueableData, int startFrame, int numFrames,
            TimeStamp timeStamp) {
        QueueDataCommand command = createQueueDataCommand(queueableData, startFrame, numFrames);
        scheduleCommand(timeStamp, command);
    }

    /**
     * Queue the data to the port at a future time. Command will clear the queue before executing.
     */
    public void queueImmediate(SequentialData queueableData, int startFrame, int numFrames,
            TimeStamp timeStamp) {
        QueueDataCommand command = createQueueDataCommand(queueableData, startFrame, numFrames);
        command.setImmediate(true);
        scheduleCommand(timeStamp, command);
    }

    /** Queue the data to the port at a future time. */
    public void queueLoop(SequentialData queueableData, int startFrame, int numFrames,
            TimeStamp timeStamp) {
        queueLoop(queueableData, startFrame, numFrames, LOOP_IF_LAST, timeStamp);
    }

    /**
     * Queue the data to the port at a future time with a specified number of loops.
     */
    public void queueLoop(SequentialData queueableData, int startFrame, int numFrames,
            int numLoops, TimeStamp timeStamp) {
        QueueDataCommand command = createQueueDataCommand(queueableData, startFrame, numFrames);
        command.setNumLoops(numLoops);
        scheduleCommand(timeStamp, command);
    }

    /** Queue the entire data object for looping. */
    public void queueLoop(SequentialData queueableData) {
        queueLoop(queueableData, 0, queueableData.getNumFrames());
    }

    /** Queue the data to the port for immediate use. */
    public void queueLoop(SequentialData queueableData, int startFrame, int numFrames) {
        queueLoop(queueableData, startFrame, numFrames, LOOP_IF_LAST);
    }

    /**
     * Queue the data to the port for immediate use with a specified number of loops.
     */
    public void queueLoop(SequentialData queueableData, int startFrame, int numFrames, int numLoops) {
        QueueDataCommand command = createQueueDataCommand(queueableData, startFrame, numFrames);
        command.setNumLoops(numLoops);
        queueCommand(command);
    }

    /**
     * Queue the data to the port at a future time. Request that the unit stop when this block is
     * finished.
     */
    public void queueStop(SequentialData queueableData, int startFrame, int numFrames,
            TimeStamp timeStamp) {
        QueueDataCommand command = createQueueDataCommand(queueableData, startFrame, numFrames);
        command.setAutoStop(true);
        scheduleCommand(timeStamp, command);
    }

    /** Queue the data to the port through the command queue ASAP. */
    public void queue(SequentialData queueableData, int startFrame, int numFrames) {
        QueueDataCommand command = createQueueDataCommand(queueableData, startFrame, numFrames);
        queueCommand(command);
    }

    /**
     * Queue entire amount of data with no options.
     *
     * @param queueableData
     */
    public void queue(SequentialData queueableData) {
        queue(queueableData, 0, queueableData.getNumFrames());
    }

    /** Schedule queueOn now! */
    public void queueOn(SequentialData queueableData) {
        queueOn(queueableData, getSynthesisEngine().createTimeStamp());
    }

    /** Schedule queueOff now! */
    public void queueOff(SequentialData queueableData) {
        queueOff(queueableData, false);
    }

    /** Schedule queueOff now! */
    public void queueOff(SequentialData queueableData, boolean ifStop) {
        queueOff(queueableData, ifStop, getSynthesisEngine().createTimeStamp());
    }

    /**
     * Convenience method that will queue the attack portion of a channelData and the sustain loop
     * if it exists. This could be used to implement a NoteOn method.
     */
    public void queueOn(SequentialData queueableData, TimeStamp timeStamp) {

        if (queueableData.getSustainBegin() < 0) {
            // no sustain loop, handle release
            if (queueableData.getReleaseBegin() < 0) {
                queueImmediate(queueableData, 0, queueableData.getNumFrames(), timeStamp); /*
                                                                                            * No
                                                                                            * loops.
                                                                                            */
            } else {
                queueImmediate(queueableData, 0, queueableData.getReleaseEnd(), timeStamp);
                int size = queueableData.getReleaseEnd() - queueableData.getReleaseBegin();
                queueLoop(queueableData, queueableData.getReleaseBegin(), size, timeStamp);
            }
        } else {
            // yes sustain loop
            if (queueableData.getSustainEnd() > 0) {
                int frontSize = queueableData.getSustainBegin();
                int loopSize = queueableData.getSustainEnd() - queueableData.getSustainBegin();
                // Is there an initial portion before the sustain loop?
                if (frontSize > 0) {
                    queueImmediate(queueableData, 0, frontSize, timeStamp);
                }
                loopSize = queueableData.getSustainEnd() - queueableData.getSustainBegin();
                if (loopSize > 0) {
                    queueLoop(queueableData, queueableData.getSustainBegin(), loopSize, timeStamp);
                }
            }

        }
    }

    /**
     * Convenience method that will queue the decay portion of a SequentialData object, or the gap
     * and release loop portions if they exist. This could be used to implement a NoteOff method.
     *
     * @param ifStop Will setAutostop(true) if release portion queued without a release loop. This will
     *         stop execution of the unit.
     */
    public void queueOff(SequentialData queueableData, boolean ifStop, TimeStamp timeStamp) {
        if (queueableData.getSustainBegin() >= 0) /* Sustain loop? */
        {
            int relSize = queueableData.getReleaseEnd() - queueableData.getReleaseBegin();
            if (queueableData.getReleaseBegin() < 0) { /* Sustain loop, no release loop. */
                int susEnd = queueableData.getSustainEnd();
                int size = queueableData.getNumFrames() - susEnd;
                // System.out.println("queueOff: size = " + size );
                if (size <= 0) {
                    // always queue something so that we can stop the loop
                    // 20001117
                    size = 1;
                    susEnd = queueableData.getNumFrames() - 1;
                }
                if (ifStop) {
                    queueStop(queueableData, susEnd, size, timeStamp);
                } else {
                    queue(queueableData, susEnd, size, timeStamp);
                }
            } else if (queueableData.getReleaseBegin() > queueableData.getSustainEnd()) {
                // Queue gap between sustain and release loop.
                queue(queueableData, queueableData.getSustainEnd(), queueableData.getReleaseEnd()
                        - queueableData.getSustainEnd(), timeStamp);
                if (relSize > 0)
                    queueLoop(queueableData, queueableData.getReleaseBegin(), relSize, timeStamp);
            } else if (relSize > 0) {
                // No gap between sustain and release.
                queueLoop(queueableData, queueableData.getReleaseBegin(), relSize, timeStamp);
            }
        }
        /* If no sustain loop, then nothing to do. */
    }

    public void clear(TimeStamp timeStamp) {
        ScheduledCommand command = new ClearQueueCommand();
        scheduleCommand(timeStamp, command);
    }

    public void clear() {
        ScheduledCommand command = new ClearQueueCommand();
        queueCommand(command);
    }

    public void writeNextDouble(double value) {
        checkBlock();
        currentBlock.currentData.writeDouble(frameIndex, value);
        advanceFrameIndex();
    }

    public long getFrameCount() {
        return framesMoved;
    }

    public boolean testAndClearAutoStop() {
        boolean temp = autoStopPending;
        autoStopPending = false;
        return temp;
    }

    public boolean isTargetValid() {
        return targetValid;
    }

    public void setNumChannels(int numChannels) {
        this.numChannels = numChannels;
    }

    public int getNumChannels() {
        return numChannels;
    }
}
