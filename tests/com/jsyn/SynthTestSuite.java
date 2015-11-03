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

package com.jsyn;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.jsyn.data.TestShortSample;
import com.jsyn.engine.TestDevices;
import com.jsyn.engine.TestEngine;
import com.jsyn.engine.TestFifo;
import com.jsyn.engine.TestWaveFileReadWrite;
import com.jsyn.ports.TestQueuedDataPort;
import com.jsyn.ports.TestSet;
import com.jsyn.unitgen.TestEnable;
import com.jsyn.unitgen.TestEnvelopeAttackDecay;
import com.jsyn.unitgen.TestEnvelopeDAHDSR;
import com.jsyn.unitgen.TestFunction;
import com.jsyn.unitgen.TestRamps;
import com.jsyn.util.TestFFT;
import com.jsyn.util.TestVoiceAllocator;

/**
 * Test new pure Java JSyn API.
 *
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class SynthTestSuite {

    public static Test suite() {

        TestSuite suite = new TestSuite();

        suite.addTestSuite(TestEngine.class);
        suite.addTestSuite(TestSet.class);
        // suite.addTestSuite( TestMath.class );
        suite.addTestSuite(TestRamps.class);
        suite.addTestSuite(TestEnvelopeAttackDecay.class);
        suite.addTestSuite(TestEnvelopeDAHDSR.class);
        suite.addTestSuite(TestShortSample.class);
        suite.addTestSuite(TestDevices.class);
        suite.addTestSuite(TestQueuedDataPort.class);
        suite.addTestSuite(TestFifo.class);
        suite.addTestSuite(TestEnable.class);
        suite.addTestSuite(TestFunction.class);
        suite.addTestSuite(TestFFT.class);
        // suite.addTestSuite( TestSampleLoader.class );
        suite.addTestSuite(TestWaveFileReadWrite.class);
        suite.addTestSuite(TestVoiceAllocator.class);
        // suite.addTestSuite(TestWrapAroundBug.class);

        return suite;
    }

    /**
     * Runs the test suite using the textual runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }
}
