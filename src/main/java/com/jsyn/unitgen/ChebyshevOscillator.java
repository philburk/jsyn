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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jsyn.data.DoubleTable;
import com.jsyn.engine.SynthesisEngine;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.softsynth.math.ChebyshevPolynomial;
import com.softsynth.math.PolynomialTableData;

/**
 * Waveshaping oscillator: a sine wave passed through a Chebyshev polynomial.
 * <p>
 * Feeding cos(θ) into the Chebyshev polynomial T(n) produces exactly cos(nθ), the n-th harmonic.
 * Lowering the drive sweeps less of the polynomial and blends in the lower harmonics, so it
 * behaves like the drive control on a distortion unit: more drive, brighter tone.
 * <p>
 * Only odd orders are offered. An even order is built from even powers of its input, which
 * leaves a DC offset whenever the drive is below 1.0.
 * <p>
 * The polynomial tables are built once and shared by every oscillator of the same order.
 * <p>
 * This is the oscillator from {@link com.jsyn.instruments.WaveShapingVoice} without its
 * envelopes.
 *
 * @author Phil Burk (C) 2026 Mobileer Inc
 */
public class ChebyshevOscillator extends Circuit implements UnitSource {
    public static final String PORT_NAME_DRIVE = "Drive";

    /** The odd orders that can be selected. */
    public static final List<Integer> ORDERS = Collections.unmodifiableList(
            Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15));

    /** Matches the order used by the WaveShapingVoice. */
    public static final int DEFAULT_ORDER = 11;

    /**
     * Points per table. T(15) crosses zero 15 times across the table, and linear interpolation
     * error shrinks with the square of the spacing, so a generous size keeps the high orders
     * clean. The tables are shared, so the memory is small.
     */
    private static final int TABLE_SIZE = 4096;

    private final SineOscillator osc;
    private final FunctionEvaluator waveShaper;
    private int order = DEFAULT_ORDER;

    /** Frequency in Hertz. */
    public final UnitInputPort frequency;

    /** How much of the polynomial the sine sweeps, 0.1 to 1.0. Higher is brighter. */
    public final UnitInputPort drive;

    public final UnitInputPort amplitude;
    public final UnitOutputPort output;

    /** The tables are built together on first use. The class loader makes this thread safe. */
    private static class Tables {
        static final DoubleTable[] TABLES = new DoubleTable[ORDERS.size()];
        static {
            for (int i = 0; i < ORDERS.size(); i++) {
                PolynomialTableData data = new PolynomialTableData(
                        ChebyshevPolynomial.T(ORDERS.get(i)), TABLE_SIZE);
                TABLES[i] = new DoubleTable(data.getData());
            }
        }
    }

    public ChebyshevOscillator() {
        add(osc = new SineOscillator());
        add(waveShaper = new FunctionEvaluator());

        addPort(frequency = osc.frequency, PORT_NAME_FREQUENCY);
        addPort(drive = osc.amplitude, PORT_NAME_DRIVE);
        addPort(amplitude = waveShaper.amplitude, PORT_NAME_AMPLITUDE);
        addPort(output = waveShaper.output, PORT_NAME_OUTPUT);

        osc.output.connect(waveShaper.input);

        drive.setup(0.1, 0.8, 1.0);
        amplitude.setup(0.0, 0.5, 1.0);

        waveShaper.function.set(tableForOrder(DEFAULT_ORDER));
    }

    @Override
    public UnitOutputPort getOutput() {
        return output;
    }

    public int getOrder() {
        return order;
    }

    /**
     * Set the order of the Chebyshev polynomial. The new table takes effect at the start of the
     * next block when the engine is running, so it never changes in the middle of a block.
     *
     * @param order must be one of {@link #ORDERS}
     * @throws IllegalArgumentException if the order is not one of {@link #ORDERS}
     */
    public void setOrder(int order) {
        if (!isValidOrder(order)) {
            throw new IllegalArgumentException("Chebyshev order must be one of " + ORDERS
                    + ", was " + order);
        }
        this.order = order;
        final DoubleTable table = tableForOrder(order);
        SynthesisEngine engine = getSynthesisEngine();
        if (engine != null && engine.isRunning()) {
            engine.queueCommand(() -> waveShaper.function.set(table));
        } else {
            waveShaper.function.set(table);
        }
    }

    public static boolean isValidOrder(int order) {
        return ORDERS.contains(order);
    }

    static DoubleTable tableForOrder(int order) {
        return Tables.TABLES[ORDERS.indexOf(order)];
    }
}
