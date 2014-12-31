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

package com.jsyn.research;

import java.util.ArrayList;

import junit.framework.TestCase;

public class BenchMultiThreading extends TestCase {
    private static final int FRAMES_PER_BLOCK = 64;
    int numThreads = 4;
    int numLoops = 100000;
    private ArrayList<CustomThread> threadList;

    class CustomThread extends Thread {
        long frameCount = 0;
        long desiredFrame = 0;
        Object semaphore = new Object();
        Object goSemaphore = new Object();
        volatile boolean go = true;
        long startNano;
        long stopNano;
        long maxElapsed;

        @Override
        public void run() {
            try {
                startNano = System.nanoTime();
                while (go) {
                    // Watch for long delays.
                    stopNano = System.nanoTime();
                    long elapsed = stopNano - startNano;
                    startNano = System.nanoTime();
                    if (elapsed > maxElapsed) {
                        maxElapsed = elapsed;
                    }

                    synchronized (semaphore) {
                        // Audio synthesis would occur here.
                        frameCount += 1;
                        // System.out.println( this + " generating frame  " +
                        // frameCount );
                        semaphore.notify();
                    }
                    synchronized (goSemaphore) {
                        while (desiredFrame <= frameCount) {
                            goSemaphore.wait();
                        }
                    }
                    long stopNano = System.nanoTime();
                }
            } catch (InterruptedException e) {
                System.out.println("CustomThread interrupted. ");
            }
            System.out.println("Finishing " + this);
        }

        public void abort() {
            go = false;
            interrupt();
        }

        public void waitForFrame(long targetFrame) throws InterruptedException {
            synchronized (semaphore) {
                while (frameCount < targetFrame) {
                    semaphore.wait();
                }
            }
        }

        public void generateFrame(long desiredFrame) {
            synchronized (goSemaphore) {
                this.desiredFrame = desiredFrame;
                goSemaphore.notify();
            }
        }

    }

    public void testMultiThreads() {
        threadList = new ArrayList<CustomThread>();
        for (int i = 0; i < numThreads; i++) {
            CustomThread thread = new CustomThread();
            threadList.add(thread);
            thread.start();
        }

        long frameCount = 0;
        long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < numLoops; i++) {
                frameCount += 1;
                waitForThreads(frameCount);
                // System.out.println("got frame " + frameCount );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        double elapsedSeconds = 0.001 * elapsedTime;
        double blocksPerSecond = numLoops / elapsedSeconds;
        System.out.format("blocksPerSecond = %10.3f\n", blocksPerSecond);
        double framesPerSecond = blocksPerSecond * FRAMES_PER_BLOCK;
        System.out.format("audio framesPerSecond = %10.3f at %d frames per block\n",
                framesPerSecond, FRAMES_PER_BLOCK);

        for (CustomThread thread : threadList) {
            System.out.format("max elapsed time is %d nanos or %f msec\n", thread.maxElapsed,
                    (thread.maxElapsed / 1000000.0));
        }
        for (CustomThread thread : threadList) {
            assertEquals("BlockCount must match ", frameCount, thread.frameCount);
            thread.abort();
        }

    }

    private void waitForThreads(long frameCount) throws InterruptedException {
        for (CustomThread thread : threadList) {
            // Ask threads to wake up and generate up to this frame.
            thread.generateFrame(frameCount);
        }
        for (CustomThread thread : threadList) {
            // Wait for all the threads to catch up.
            thread.waitForFrame(frameCount);
        }
    }
}
