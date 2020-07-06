/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

/**
 * Defines a class that can schedule the execution of Grains in a GrainFarm. This is mostly for
 * internal use.
 * 
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public interface GrainScheduler {

    /**
     * Calculate time in seconds for the next gap between grains.
     * 
     * @param duration
     * @param density
     * @return seconds before next grain
     */
    double nextGap(double duration, double density);

    /**
     * Calculate duration in seconds for the next grains.
     * 
     * @param suggestedDuration
     * @return duration of grain seconds
     */
    double nextDuration(double suggestedDuration);

}
