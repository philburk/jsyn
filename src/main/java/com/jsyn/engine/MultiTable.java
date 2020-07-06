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

/*
 * Multiple tables of sawtooth data.
 * organized by octaves below the Nyquist Rate.
 * used to generate band-limited Sawtooth, Impulse, Pulse, Square and Triangle BL waveforms
 *
 <pre>
 Analysis of octave requirements for tables.

 OctavesIndex    Frequency     Partials
 0               N/2  11025      1
 1               N/4   5512      2
 2               N/8   2756      4
 3               N/16  1378      8
 4               N/32   689      16
 5               N/64   344      32
 6               N/128  172      64
 7               N/256   86      128
 </pre>
 *
 * @author Phil Burk (C) 2009 Mobileer Inc
 */
public class MultiTable {

    public final static int NUM_TABLES = 8;
    public final static int CYCLE_SIZE = (1 << 10);

    private static MultiTable instance = new MultiTable(NUM_TABLES, CYCLE_SIZE);
    private double phaseScalar;
    private float[][] tables; // array of array of tables

    /**************************************************************************
     * Initialize sawtooth wavetables. Table[0] should contain a pure sine wave. Succeeding tables
     * should have increasing numbers of partials.
     */
    public MultiTable(int numTables, int cycleSize) {
        int tableSize = cycleSize + 1;

        // Allocate array of arrays.
        tables = new float[numTables][tableSize];

        float[] sineTable = tables[0];

        phaseScalar = (float) (cycleSize * 0.5);

        /* Fill initial sine table with values for -PI to PI. */
        for (int j = 0; j < tableSize; j++) {
            sineTable[j] = (float) Math.sin(((((double) j) / (double) cycleSize) * Math.PI * 2.0)
                    - Math.PI);
        }

        /*
         * Build each table from scratch and scale partials by raised cosine* to eliminate Gibbs
         * effect.
         */
        for (int i = 1; i < numTables; i++) {
            int numPartials;
            double kGibbs;
            float[] table = tables[i];

            /* Add together partials for this table. */
            numPartials = 1 << i;
            kGibbs = Math.PI / (2 * numPartials);
            for (int k = 0; k < numPartials; k++) {
                double ampl, cGibbs;
                int sineIndex = 0;
                int partial = k + 1;
                cGibbs = Math.cos(k * kGibbs);
                /* Calculate amplitude for Nth partial */
                ampl = cGibbs * cGibbs / partial;

                for (int j = 0; j < tableSize; j++) {
                    table[j] += (float) ampl * sineTable[sineIndex];
                    sineIndex += partial;
                    /* Wrap index at end of table.. */
                    if (sineIndex >= cycleSize) {
                        sineIndex -= cycleSize;
                    }
                }
            }
        }

        /* Normalize after */
        for (int i = 1; i < numTables; i++) {
            normalizeArray(tables[i]);
        }
    }

    /**************************************************************************/
    public static float normalizeArray(float[] fdata) {
        float max, val, gain;
        int i;

        // determine maximum value.
        max = 0.0f;
        for (i = 0; i < fdata.length; i++) {
            val = Math.abs(fdata[i]);
            if (val > max)
                max = val;
        }
        if (max < 0.0000001f)
            max = 0.0000001f;
        // scale array
        gain = 1.0f / max;
        for (i = 0; i < fdata.length; i++)
            fdata[i] *= gain;
        return gain;
    }

    /*****************************************************************************
     * When the phaseInc maps to the highest level table, then we start interpolating between the
     * highest table and the raw sawtooth value (phase). When phaseInc points to highest table:
     * flevel = NUM_TABLES - 1 = -1 - log2(pInc); log2(pInc) = - NUM_TABLES pInc = 2**(-NUM_TABLES)
     */
    private final static double LOWEST_PHASE_INC_INV = (1 << NUM_TABLES);

    /**************************************************************************/
    /* Phase ranges from -1.0 to +1.0 */
    public double calculateSawtooth(double currentPhase, double positivePhaseIncrement,
            double flevel) {
        float[] tableBase;
        double val;
        double hiSam; /* Use when verticalFraction is 1.0 */
        double loSam; /* Use when verticalFraction is 0.0 */
        double sam1, sam2;

        /* Use Phase to determine sampleIndex into table. */
        double findex = ((phaseScalar * currentPhase) + phaseScalar);
        // findex is > 0 so we do not need to call floor().
        int sampleIndex = (int) findex;
        double horizontalFraction = findex - sampleIndex;
        int tableIndex = (int) flevel;

        if (tableIndex > (NUM_TABLES - 2)) {
            /*
             * Just use top table and mix with arithmetic sawtooth if below lowest frequency.
             * Generate new fraction for interpolating between 0.0 and lowest table frequency.
             */
            double fraction = positivePhaseIncrement * LOWEST_PHASE_INC_INV;
            tableBase = tables[(NUM_TABLES - 1)];

            /* Get adjacent samples. Assume guard point present. */
            sam1 = tableBase[sampleIndex];
            sam2 = tableBase[sampleIndex + 1];
            /* Interpolate between adjacent samples. */
            loSam = sam1 + (horizontalFraction * (sam2 - sam1));

            /* Use arithmetic version for low frequencies. */
            /* fraction is 0.0 at 0 Hz */
            val = currentPhase + (fraction * (loSam - currentPhase));
        } else {

            double verticalFraction = flevel - tableIndex;

            if (tableIndex < 0) {
                if (tableIndex < -1) // above Nyquist!
                {
                    val = 0.0;
                } else {
                    /*
                     * At top of supported range, interpolate between 0.0 and first partial.
                     */
                    tableBase = tables[0]; /* Sine wave table. */

                    /* Get adjacent samples. Assume guard point present. */
                    sam1 = tableBase[sampleIndex];
                    sam2 = tableBase[sampleIndex + 1];

                    /* Interpolate between adjacent samples. */
                    hiSam = sam1 + (horizontalFraction * (sam2 - sam1));
                    /* loSam = 0.0 */
                    // verticalFraction is 0.0 at Nyquist
                    val = verticalFraction * hiSam;
                }
            } else {
                /*
                 * Interpolate between adjacent levels to prevent harmonics from popping.
                 */
                tableBase = tables[tableIndex + 1];

                /* Get adjacent samples. Assume guard point present. */
                sam1 = tableBase[sampleIndex];
                sam2 = tableBase[sampleIndex + 1];

                /* Interpolate between adjacent samples. */
                hiSam = sam1 + (horizontalFraction * (sam2 - sam1));

                /* Get adjacent samples. Assume guard point present. */
                tableBase = tables[tableIndex];
                sam1 = tableBase[sampleIndex];
                sam2 = tableBase[sampleIndex + 1];

                /* Interpolate between adjacent samples. */
                loSam = sam1 + (horizontalFraction * (sam2 - sam1));

                val = loSam + (verticalFraction * (hiSam - loSam));
            }
        }
        return val;
    }

    public double convertPhaseIncrementToLevel(double positivePhaseIncrement) {
        if (positivePhaseIncrement < 1.0e-30) {
            positivePhaseIncrement = 1.0e-30;
        }
        return -1.0 - (Math.log(positivePhaseIncrement) / Math.log(2.0));
    }

    public static MultiTable getInstance() {
        return instance;
    }

}
