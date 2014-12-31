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

/** Measure CPU load. */
public class LoadAnalyzer {
    private long stopTime;
    private long previousStopTime;
    private long startTime;
    private double averageTotalTime;
    private double averageOnTime;

    protected LoadAnalyzer() {
        stopTime = System.nanoTime();
    }

    /**
     * Call this when you stop doing something. Ideally all of the time since start() was spent on
     * doing something without interruption.
     */
    public void stop() {
        previousStopTime = stopTime;
        stopTime = System.nanoTime();
        long onTime = stopTime - startTime;
        long totalTime = stopTime - previousStopTime;
        if (totalTime > 0) {
            // Recursive averaging filter.
            double rate = 0.01;
            averageOnTime = (averageOnTime * (1.0 - rate)) + (onTime * rate);
            averageTotalTime = (averageTotalTime * (1.0 - rate)) + (totalTime * rate);
        }
    }

    /** Call this when you start doing something. */
    public void start() {
        startTime = System.nanoTime();
    }

    /** Calculate, on average, how much of the time was spent doing something. */
    public double getAverageLoad() {
        if (averageTotalTime > 0.0) {
            return averageOnTime / averageTotalTime;
        } else {
            return 0.0;
        }
    }
}
