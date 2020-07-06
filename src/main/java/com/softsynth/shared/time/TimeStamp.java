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

package com.softsynth.shared.time;

/**
 * @author Phil Burk, (C) 2009 Mobileer Inc
 */
public class TimeStamp implements Comparable<TimeStamp> {
    private final double time;

    public TimeStamp(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    /**
     * @return -1 if (this &lt; t2), 0 if equal, or +1
     */
    @Override
    public int compareTo(TimeStamp t2) {
        if (time < t2.time)
            return -1;
        else if (time == t2.time)
            return 0;
        else
            return 1;
    }

    /**
     * Create a new TimeStamp at a relative offset in seconds.
     *
     * @param delta
     * @return earlier or later TimeStamp
     */
    public TimeStamp makeRelative(double delta) {
        return new TimeStamp(time + delta);
    }

}
