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

package com.jsyn.unitgen;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.jsyn.Synthesizer;
import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.ConnectableInput;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.ports.UnitPort;
import com.softsynth.shared.time.TimeStamp;

/**
 * Base class for all unit generators.
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public abstract class UnitGenerator {
    protected static final double VERY_SMALL_FLOAT = 1.0e-26;

    // Some common port names.
    public static final String PORT_NAME_INPUT = "Input";
    public static final String PORT_NAME_OUTPUT = "Output";
    public static final String PORT_NAME_PHASE = "Phase";
    public static final String PORT_NAME_FREQUENCY = "Frequency";
    public static final String PORT_NAME_FREQUENCY_SCALER = "FreqScaler";
    public static final String PORT_NAME_AMPLITUDE = "Amplitude";
    public static final String PORT_NAME_PAN = "Pan";
    public static final String PORT_NAME_TIME = "Time";
    public static final String PORT_NAME_CUTOFF = "Cutoff";
    public static final String PORT_NAME_PRESSURE = "Pressure";
    public static final String PORT_NAME_TIMBRE = "Timbre";

    public static final double FALSE = 0.0;
    public static final double TRUE = 1.0;
    protected SynthesisEngine synthesisEngine;
    private final LinkedHashMap<String, UnitPort> ports = new LinkedHashMap<String, UnitPort>();
    private Circuit circuit;
    private long lastFrameCount;
    private boolean enabled = true;
    private static int nextId;
    private final int id = nextId++;

    static Logger logger = Logger.getLogger(UnitGenerator.class.getName());

    public int getId() {
        return id;
    }

    public int getFrameRate() {
        // return frameRate;
        return synthesisEngine.getFrameRate();
    }

    public double getFramePeriod() {
        // return framePeriod; // TODO - Why does OldJSynTestSuite fail if I use this!
        return synthesisEngine.getFramePeriod();
    }

    public void addPort(UnitPort port) {
        port.setUnitGenerator(this);
        // Store in a hash table by name.
        ports.put(port.getName().toLowerCase(), port);
    }

    public void addPort(UnitPort port, String name) {
        port.setName(name);
        addPort(port);
    }

    /**
     * Case-insensitive search for a port by name.
     * @param portName
     * @return matching port or null
     */
    public UnitPort getPortByName(String portName) {
        return ports.get(portName.toLowerCase());
    }

    public Collection<UnitPort> getPorts() {
        return ports.values();
    }

    /**
     * Perform essential synthesis function.
     *
     * @param start offset into port buffers
     * @param limit limit offset into port buffers for loop
     */
    public abstract void generate(int start, int limit);

    /**
     * Generate a full block.
     */
    public void generate() {
        generate(0, Synthesizer.FRAMES_PER_BLOCK);
    }

    /**
     * @return the synthesisEngine
     */
    public SynthesisEngine getSynthesisEngine() {
        return synthesisEngine;
    }

    /**
     * @return the Synthesizer
     */
    public Synthesizer getSynthesizer() {
        return synthesisEngine;
    }

    /**
     * @param synthesisEngine the synthesisEngine to set
     */
    public void setSynthesisEngine(SynthesisEngine synthesisEngine) {
        if ((this.synthesisEngine != null) && (this.synthesisEngine != synthesisEngine)) {
            throw new RuntimeException("Unit synthesisEngine already set.");
        }
        this.synthesisEngine = synthesisEngine;
    }

    public UnitGenerator getTopUnit() {
        UnitGenerator unit = this;
        // Climb to top of circuit hierarchy.
        while (unit.circuit != null) {
            unit = unit.circuit;
        }
        logger.fine("getTopUnit " + this + " => " + unit);
        return unit;
    }

    protected void autoStop() {
        synthesisEngine.autoStopUnit(getTopUnit());
    }

    /** Calculate signal based on halflife of an exponential decay. */
    public double convertHalfLifeToMultiplier(double halfLife) {
        if (halfLife < (2.0 * getFramePeriod())) {
            return 1.0;
        } else {
            // Oddly enough, this code is valid for both PeakFollower and AsymptoticRamp.
            return 1.0 - Math.pow(0.5, 1.0 / (halfLife * getSynthesisEngine().getFrameRate()));
        }
    }

    protected double incrementWrapPhase(double currentPhase, double phaseIncrement) {
        currentPhase += phaseIncrement;

        if (currentPhase >= 1.0) {
            currentPhase -= 2.0;
        } else if (currentPhase < -1.0) {
            currentPhase += 2.0;
        }
        return currentPhase;
    }

    /** Calculate rate based on phase going from 0.0 to 1.0 in time. */
    protected double convertTimeToRate(double time) {
        double period2X = synthesisEngine.getInverseNyquist();
        if (time < period2X) {
            return 1.0;
        } else {
            return getFramePeriod() / time;
        }
    }

    /** Flatten output ports so we don't output a changing signal when stopped. */
    public void flattenOutputs() {
        for (UnitPort port : ports.values()) {
            if (port instanceof UnitOutputPort) {
                ((UnitOutputPort) port).flatten();
            }
        }
    }

    public void setCircuit(Circuit circuit) {
        if ((this.circuit != null) && (circuit != null)) {
            throw new RuntimeException("Unit is already in a circuit.");
        }
        // logger.info( "setCircuit in  unit " + this + " with circuit " + circuit );
        this.circuit = circuit;
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public void pullData(long frameCount, int start, int limit) {
        // Don't generate twice in case the paths merge.
        if (enabled && (frameCount > lastFrameCount)) {
            // Do this first to block recursion when there is a feedback loop.
            lastFrameCount = frameCount;
            // Then pull from all the units that are upstream.
            for (UnitPort port : ports.values()) {
                if (port instanceof ConnectableInput) {
                    ((ConnectableInput) port).pullData(frameCount, start, limit);
                }
            }
            // Finally generate using outputs of the upstream units.
            generate(start, limit);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * If enabled, then a unit will execute if its output is connected to another unit that is
     * executed. If not enabled then it will not execute and will not pull data from units that are
     * connected to its inputs. Disabling a unit at the output of a tree of units can be used to
     * turn off the entire tree, thus saving CPU cycles.
     *
     * @param enabled
     * @see UnitGate#setupAutoDisable(UnitGenerator)
     * @see start
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            flattenOutputs();
        }
    }

    /**
     * Some units, for example LineOut and FixedRateMonoWriter, will only work if
     * started explicitly. Other units will run when downstream units are started.
     *
     * @return true if you should call start() for this unit
     */
    public boolean isStartRequired() {
        return false;
    }

    /**
     * Start executing this unit directly by adding it to a "run list" of units in the synthesis
     * engine. This method is normally only called for the final unit in a chain, for example a
     * LineOut. When that final unit executes it will "pull" data from any units connected to its
     * inputs. Those units will then pull data their inputs until the entire chain is executed. If
     * units are connected in a circle then this will be detected and the infinite recursion will be
     * blocked.
     *
     * @see setEnabled
     */
    public void start() {
        if (getSynthesisEngine() == null) {
            throw new RuntimeException("This " + this.getClass().getName()
                    + " was not add()ed to a Synthesizer.");
        }
        getSynthesisEngine().startUnit(this);
    }

    /**
     * Start a unit at the specified time.
     *
     * @param time
     * @see start
     */
    public void start(double time) {
        start(new TimeStamp(time));
    }

    /**
     * Start a unit at the specified time.
     *
     * @param timeStamp
     * @see start
     */
    public void start(TimeStamp timeStamp) {
        if (getSynthesisEngine() == null) {
            throw new RuntimeException("This " + this.getClass().getName()
                    + " was not add()ed to a Synthesizer.");
        }
        getSynthesisEngine().startUnit(this, timeStamp);
    }

    /**
     * Stop a unit at the specified time.
     *
     * @param time
     * @see start
     */
    public void stop(double time) {
        stop(new TimeStamp(time));
    }

    public void stop() {
        getSynthesisEngine().stopUnit(this);
    }

    public void stop(TimeStamp timeStamp) {
        getSynthesisEngine().stopUnit(this, timeStamp);
    }

    /**
     * @deprecated ignored, frameRate comes from the SynthesisEngine
     * @param rate
     */
    @Deprecated
    public void setFrameRate(int rate) {
    }

    /** Needed by UnitSink */
    public UnitGenerator getUnitGenerator() {
        return this;
    }

    /** Needed by UnitVoice */
    public void setPort(String portName, double value, TimeStamp timeStamp) {
        UnitInputPort port = (UnitInputPort) getPortByName(portName);
        // System.out.println("setPort " + port );
        if (port == null) {
            logger.warning("port was null for name " + portName + ", " + this.getClass().getName());
        } else {
            port.set(value, timeStamp);
        }
    }

    public void printConnections() {
        printConnections(System.out);
    }

    public void printConnections(PrintStream out) {
        printConnections(out, 0);
    }

    public void printConnections(PrintStream out, int level) {
        for (UnitPort port : getPorts()) {
            if (port instanceof UnitInputPort) {
                ((UnitInputPort) port).printConnections(out, level);
            }
        }
    }

}
