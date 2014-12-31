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

package com.jsyn.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.devices.AudioDeviceFactory;
import com.jsyn.devices.AudioDeviceInputStream;
import com.jsyn.devices.AudioDeviceManager;
import com.jsyn.devices.AudioDeviceOutputStream;
import com.jsyn.unitgen.UnitGenerator;
import com.softsynth.shared.time.ScheduledCommand;
import com.softsynth.shared.time.ScheduledQueue;
import com.softsynth.shared.time.TimeStamp;

//TODO Resolve problem with HearDAHDSR where "Rate" port.set is not reflected in knob. Engine not running.
//TODO new tutorial and docs on website
//TODO AutoStop on DAHDSR
//TODO Test/example SequentialData queueOn and queueOff

//TODO Abstract device interface. File device!
//TODO Measure thread switching sync, performance for multi-core synthesis. Use 4 core pro.
//TODO Optimize SineOscillatorPhaseModulated
//TODO More circuits.
//TODO DC blocker
//TODO Swing scope probe UIs, auto ranging

/**
 * Internal implementation of JSyn Synthesizer. The public API is in the Synthesizer interface. This
 * class might be used directly internally.
 * 
 * @author Phil Burk (C) 2009 Mobileer Inc
 * @see Synthesizer
 */
public class SynthesisEngine implements Runnable, Synthesizer {
    private final static int BLOCKS_PER_BUFFER = 8;
    private final static int FRAMES_PER_BUFFER = Synthesizer.FRAMES_PER_BLOCK * BLOCKS_PER_BUFFER;
    public static final int DEFAULT_FRAME_RATE = 44100;

    private AudioDeviceManager audioDeviceManager;
    private AudioDeviceOutputStream audioOutputStream;
    private AudioDeviceInputStream audioInputStream;
    private Thread audioThread;
    private ScheduledQueue<ScheduledCommand> commandQueue = new ScheduledQueue<ScheduledCommand>();
    private volatile boolean go;

    private InterleavingBuffer inputBuffer;
    private InterleavingBuffer outputBuffer;
    private double inverseNyquist;
    private long frameCount;
    private boolean pullDataEnabled = true;
    private boolean useRealTime = true;
    private boolean started;
    private int frameRate = DEFAULT_FRAME_RATE;
    private double framePeriod = 1.0 / frameRate;

    // List of all units added to the synth.
    private ArrayList<UnitGenerator> allUnitList = new ArrayList<UnitGenerator>();
    // List of running units.
    private ArrayList<UnitGenerator> runningUnitList = new ArrayList<UnitGenerator>();
    // List of units stopping because of autoStop.
    private ArrayList<UnitGenerator> stoppingUnitList = new ArrayList<UnitGenerator>();

    private LoadAnalyzer loadAnalyzer;
    // private int numOutputChannels;
    // private int numInputChannels;
    private CopyOnWriteArrayList<Runnable> audioTasks = new CopyOnWriteArrayList<Runnable>();
    /** A fraction corresponding to exactly -96 dB. */
    public static final double DB96 = (1.0 / 63095.73444801943);
    /** A fraction that is approximately -90.3 dB. Defined as 1 bit of an S16. */
    public static final double DB90 = (1.0 / (1 << 15));

    static Logger logger = Logger.getLogger(SynthesisEngine.class.getName());

    public SynthesisEngine(AudioDeviceManager audioDeviceManager) {
        this.audioDeviceManager = audioDeviceManager;
    }

    public SynthesisEngine() {
        this(AudioDeviceFactory.createAudioDeviceManager());
    }

    @Override
    public String getVersion() {
        return JSyn.VERSION;
    }

    @Override
    public int getVersionCode() {
        return JSyn.VERSION_CODE;
    }

    @Override
    public String toString() {
        return "JSyn " + JSyn.VERSION_TEXT;
    }

    public boolean isPullDataEnabled() {
        return pullDataEnabled;
    }

    /**
     * If set true then audio data will be pulled from the output ports of connected unit
     * generators. The final unit in a tree of units needs to be start()ed.
     * 
     * @param pullDataEnabled
     */
    public void setPullDataEnabled(boolean pullDataEnabled) {
        this.pullDataEnabled = pullDataEnabled;
    }

    private void setupAudioBuffers(int numInputChannels, int numOutputChannels) {
        inputBuffer = new InterleavingBuffer(FRAMES_PER_BUFFER, Synthesizer.FRAMES_PER_BLOCK,
                numInputChannels);
        outputBuffer = new InterleavingBuffer(FRAMES_PER_BUFFER, Synthesizer.FRAMES_PER_BLOCK,
                numOutputChannels);
    }

    public void terminate() {
    }

    class InterleavingBuffer {
        private double[] interleavedBuffer;
        ChannelBlockBuffer[] blockBuffers;

        InterleavingBuffer(int framesPerBuffer, int framesPerBlock, int samplesPerFrame) {
            interleavedBuffer = new double[framesPerBuffer * samplesPerFrame];
            // Allocate buffers for each channel of synthesis output.
            blockBuffers = new ChannelBlockBuffer[samplesPerFrame];
            for (int i = 0; i < blockBuffers.length; i++) {
                blockBuffers[i] = new ChannelBlockBuffer(framesPerBlock);
            }
        }

        int deinterleave(int inIndex) {
            for (int jf = 0; jf < Synthesizer.FRAMES_PER_BLOCK; jf++) {
                for (int iob = 0; iob < blockBuffers.length; iob++) {
                    ChannelBlockBuffer buffer = blockBuffers[iob];
                    buffer.values[jf] = interleavedBuffer[inIndex++];
                }
            }
            return inIndex;
        }

        int interleave(int outIndex) {
            for (int jf = 0; jf < Synthesizer.FRAMES_PER_BLOCK; jf++) {
                for (int iob = 0; iob < blockBuffers.length; iob++) {
                    ChannelBlockBuffer buffer = blockBuffers[iob];
                    interleavedBuffer[outIndex++] = buffer.values[jf];
                }
            }
            return outIndex;
        }

        public double[] getChannelBuffer(int i) {
            return blockBuffers[i].values;
        }

        public void clear() {
            for (int i = 0; i < blockBuffers.length; i++) {
                blockBuffers[i].clear();
            }
        }
    }

    class ChannelBlockBuffer {
        private double[] values;

        ChannelBlockBuffer(int framesPerBlock) {
            values = new double[framesPerBlock];
        }

        void clear() {
            for (int i = 0; i < values.length; i++) {
                values[i] = 0.0f;
            }
        }
    }

    @Override
    public void start() {
        // TODO Use constants.
        start(DEFAULT_FRAME_RATE, -1, 0, -1, 2);
    }

    @Override
    public void start(int frameRate) {
        // TODO Use constants.
        start(frameRate, -1, 0, -1, 2);
    }

    @Override
    public synchronized void start(int frameRate, int inputDeviceID, int numInputChannels,
            int outputDeviceID, int numOutputChannels) {
        if (started) {
            logger.info("JSyn already started.");
            return;
        }

        this.frameRate = frameRate;
        this.framePeriod = 1.0 / frameRate;

        // Set rate for any units that have already been added.
        for (UnitGenerator ugen : allUnitList) {
            ugen.setFrameRate(frameRate);
        }

        // this.numInputChannels = numInputChannels;
        // this.numOutputChannels = numOutputChannels;
        setupAudioBuffers(numInputChannels, numOutputChannels);

        logger.info("Pure Java JSyn from www.softsynth.com, rate = " + frameRate + ", "
                + (useRealTime ? "RT" : "NON-RealTime") + ", " + JSyn.VERSION_TEXT);

        inverseNyquist = 2.0 / frameRate;

        if (useRealTime) {
            if (numInputChannels > 0) {
                audioInputStream = audioDeviceManager.createInputStream(inputDeviceID, frameRate,
                        numInputChannels);
            }
            if (numOutputChannels > 0) {
                audioOutputStream = audioDeviceManager.createOutputStream(outputDeviceID,
                        frameRate, numOutputChannels);
            }
            audioThread = new Thread(this);
            logger.fine("Synth thread old priority = " + audioThread.getPriority());
            audioThread.setPriority(audioThread.getPriority() + 2);
            logger.fine("Synth thread new priority = " + audioThread.getPriority());
            go = true;
            audioThread.start();
        }

        started = true;
    }

    @Override
    public boolean isRunning() {
        return go;
    }

    @Override
    public synchronized void stop() {
        if (!started) {
            logger.info("JSyn already stopped.");
            return;
        }

        if (useRealTime) {
            // Stop audio synthesis and all units.
            go = false;
            if (audioThread != null) {
                try {
                    // Interrupt now, otherwise audio thread will wait for audio I/O.
                    audioThread.interrupt();

                    audioThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (runningUnitList) {
            runningUnitList.clear();
        }
        started = false;
    }

    @Override
    public void run() {
        logger.fine("JSyn synthesis thread starting.");
        try {
            if (audioInputStream != null) {
                logger.finer("JSyn synthesis thread trying to start audio INPUT!");
                audioInputStream.start();
                String msg = String.format("Input Latency in = %5.1f msec",
                        1000 * audioInputStream.getLatency());
                logger.fine(msg);
            }
            if (audioOutputStream != null) {
                logger.finer("JSyn synthesis thread trying to start audio OUTPUT!");
                audioOutputStream.start();
                String msg = String.format("Output Latency = %5.1f msec",
                        1000 * audioOutputStream.getLatency());
                logger.fine(msg);
                // Buy some time while we fill the buffer.
                audioOutputStream.write(outputBuffer.interleavedBuffer);
            }
            loadAnalyzer = new LoadAnalyzer();
            while (go) {
                if (audioInputStream != null) {
                    audioInputStream.read(inputBuffer.interleavedBuffer);
                }

                loadAnalyzer.start();
                runAudioTasks();
                generateNextBuffer();
                loadAnalyzer.stop();

                if (audioOutputStream != null) {
                    // This call will block when the output is full.
                    audioOutputStream.write(outputBuffer.interleavedBuffer);
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            go = false;

        } finally {
            logger.info("JSyn synthesis thread in finally code.");
            // Stop audio system.
            if (audioInputStream != null) {
                audioInputStream.stop();
            }
            if (audioOutputStream != null) {
                audioOutputStream.stop();
            }
        }
        logger.fine("JSyn synthesis thread exiting.");
    }

    private void runAudioTasks() {
        for (Runnable task : audioTasks) {
            task.run();
        }
    }

    // TODO We need to implement a sharedSleeper like we use in JSyn1.
    public void generateNextBuffer() {
        int outIndex = 0;
        int inIndex = 0;
        for (int i = 0; i < BLOCKS_PER_BUFFER; i++) {
            if (inputBuffer != null) {
                inIndex = inputBuffer.deinterleave(inIndex);
            }

            TimeStamp timeStamp = createTimeStamp();
            // Try putting this up here so incoming time-stamped events will get
            // scheduled later.
            processScheduledCommands(timeStamp);
            clearBlockBuffers();
            synthesizeBuffer();

            if (outputBuffer != null) {
                outIndex = outputBuffer.interleave(outIndex);
            }
            frameCount += Synthesizer.FRAMES_PER_BLOCK;
        }
    }

    @Override
    public double getCurrentTime() {
        return frameCount * framePeriod;
    }

    @Override
    public TimeStamp createTimeStamp() {
        return new TimeStamp(getCurrentTime());
    }

    private void processScheduledCommands(TimeStamp timeStamp) {
        List<ScheduledCommand> timeList = commandQueue.removeNextList(timeStamp);

        while (timeList != null) {
            while (!timeList.isEmpty()) {
                ScheduledCommand command = timeList.remove(0);
                logger.fine("processing " + command + ", at time " + timeStamp.getTime());
                command.run();
            }
            // Get next list of commands at the given time.
            timeList = commandQueue.removeNextList(timeStamp);
        }
    }

    @Override
    public void scheduleCommand(TimeStamp timeStamp, ScheduledCommand command) {
        if ((Thread.currentThread() == audioThread) && (timeStamp.getTime() <= getCurrentTime())) {
            command.run();
        } else {
            logger.fine("scheduling " + command + ", at time " + timeStamp.getTime());
            commandQueue.add(timeStamp, command);
        }
    }

    @Override
    public void scheduleCommand(double time, ScheduledCommand command) {
        TimeStamp timeStamp = new TimeStamp(time);
        scheduleCommand(timeStamp, command);
    }

    @Override
    public void queueCommand(ScheduledCommand command) {
        TimeStamp timeStamp = createTimeStamp();
        scheduleCommand(timeStamp, command);
    }

    private void clearBlockBuffers() {
        outputBuffer.clear();
    }

    private void synthesizeBuffer() {
        synchronized (runningUnitList) {
            ListIterator<UnitGenerator> iterator = runningUnitList.listIterator();
            while (iterator.hasNext()) {
                UnitGenerator unit = iterator.next();
                if (pullDataEnabled) {
                    unit.pullData(getFrameCount(), 0, Synthesizer.FRAMES_PER_BLOCK);
                } else {
                    unit.generate(0, Synthesizer.FRAMES_PER_BLOCK);
                }
            }
            // Remove any units that got auto stopped.
            for (UnitGenerator ugen : stoppingUnitList) {
                runningUnitList.remove(ugen);
                ugen.flattenOutputs();
            }
        }
        stoppingUnitList.clear();
    }

    public double[] getInputBuffer(int i) {
        try {
            return inputBuffer.getChannelBuffer(i);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Audio Input not configured in start() method.");
        }
    }

    public double[] getOutputBuffer(int i) {
        try {
            return outputBuffer.getChannelBuffer(i);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Audio Output not configured in start() method.");
        }
    }

    private void internalStopUnit(UnitGenerator unit) {
        synchronized (runningUnitList) {
            runningUnitList.remove(unit);
        }
        unit.flattenOutputs();
    }

    public void autoStopUnit(UnitGenerator unitGenerator) {
        synchronized (stoppingUnitList) {
            stoppingUnitList.add(unitGenerator);
        }
    }

    @Override
    public void startUnit(UnitGenerator unit, double time) {
        startUnit(unit, new TimeStamp(time));
    }

    @Override
    public void stopUnit(UnitGenerator unit, double time) {
        stopUnit(unit, new TimeStamp(time));
    }

    @Override
    public void startUnit(final UnitGenerator unit, TimeStamp timeStamp) {
        // Don't start if it is a component in a circuit because it will be
        // executed by the circuit.
        if (unit.getCircuit() == null) {
            scheduleCommand(timeStamp, new ScheduledCommand() {
                @Override
                public void run() {
                    internalStartUnit(unit);
                }
            });
        }
    }

    @Override
    public void stopUnit(final UnitGenerator unit, TimeStamp timeStamp) {
        scheduleCommand(timeStamp, new ScheduledCommand() {
            @Override
            public void run() {
                internalStopUnit(unit);
            }
        });
    }

    @Override
    public void startUnit(UnitGenerator unit) {
        startUnit(unit, createTimeStamp());
    }

    @Override
    public void stopUnit(UnitGenerator unit) {
        stopUnit(unit, createTimeStamp());
    }

    private void internalStartUnit(UnitGenerator unit) {
        // logger.info( "internalStartUnit " + unit + " with circuit " +
        // unit.getCircuit() );
        if (unit.getCircuit() == null) {
            synchronized (runningUnitList) {
                if (!runningUnitList.contains(unit)) {
                    runningUnitList.add(unit);
                }
            }
        }
        // else
        // {
        // logger.info(
        // "internalStartUnit detected race condition !!!! from old JSyn" + unit
        // + " with circuit " + unit.getCircuit() );
        // }
    }

    public double getInverseNyquist() {
        return inverseNyquist;
    }

    public double convertTimeToExponentialScaler(double duration) {
        // Calculate scaler so that scaler^frames = target/source
        double numFrames = duration * getFrameRate();
        return Math.pow(DB90, (1.0 / numFrames));
    }

    @Override
    public long getFrameCount() {
        return frameCount;
    }

    /**
     * @return the frameRate
     */
    @Override
    public int getFrameRate() {
        return frameRate;
    }

    /**
     * @return the inverse of the frameRate for efficiency
     */
    @Override
    public double getFramePeriod() {
        return framePeriod;
    }

    /** Convert a short value to a double in the range -1.0 to almost 1.0. */
    public static double convertShortToDouble(short sdata) {
        return (sdata * (1.0 / Short.MAX_VALUE));
    }

    /**
     * Convert a double value in the range -1.0 to almost 1.0 to a short. Double value is clipped
     * before converting.
     */
    public static short convertDoubleToShort(double d) {
        final double maxValue = ((double) (Short.MAX_VALUE - 1)) / Short.MAX_VALUE;
        if (d > maxValue) {
            d = maxValue;
        } else if (d < -1.0) {
            d = -1.0;
        }
        return (short) (d * Short.MAX_VALUE);
    }

    @Override
    public void addAudioTask(Runnable blockTask) {
        audioTasks.add(blockTask);
    }

    @Override
    public void removeAudioTask(Runnable blockTask) {
        audioTasks.remove(blockTask);
    }

    @Override
    public double getUsage() {
        // use temp so we don't have to synchronize
        LoadAnalyzer temp = loadAnalyzer;
        if (temp != null) {
            return temp.getAverageLoad();
        } else {
            return 0.0;
        }
    }

    @Override
    public AudioDeviceManager getAudioDeviceManager() {
        return audioDeviceManager;
    }

    @Override
    public void setRealTime(boolean realTime) {
        useRealTime = realTime;
    }

    @Override
    public boolean isRealTime() {
        return useRealTime;
    }

    public double getOutputLatency() {
        if (audioOutputStream != null) {
            return audioOutputStream.getLatency();
        } else {
            return 0;
        }
    }

    public double getInputLatency() {
        if (audioInputStream != null) {
            return audioInputStream.getLatency();
        } else {
            return 0;
        }
    }

    @Override
    public void add(UnitGenerator ugen) {
        ugen.setSynthesisEngine(this);
        allUnitList.add(ugen);
        if (frameRate > 0) {
            ugen.setFrameRate(frameRate);
        }
    }

    @Override
    public void remove(UnitGenerator ugen) {
        allUnitList.remove(ugen);
    }

    @Override
    public void sleepUntil(double time) throws InterruptedException {
        double timeToSleep = time - getCurrentTime();
        while (timeToSleep > 0.0) {
            if (useRealTime) {
                long msecToSleep = (long) (1000 * timeToSleep);
                if (msecToSleep <= 0) {
                    msecToSleep = 1;
                }
                Thread.sleep(msecToSleep);
            } else {

                generateNextBuffer();
            }
            timeToSleep = time - getCurrentTime();
        }
    }

    @Override
    public void sleepFor(double duration) throws InterruptedException {
        sleepUntil(getCurrentTime() + duration);
    }

    public void printConnections() {
        if (pullDataEnabled) {
            ListIterator<UnitGenerator> iterator = runningUnitList.listIterator();
            while (iterator.hasNext()) {
                UnitGenerator unit = iterator.next();
                unit.printConnections();
            }
        }

    }

}
