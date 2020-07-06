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

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Store objects in time sorted order.
 */
public class ScheduledQueue<T> {
    private final SortedMap<TimeStamp, List<T>> timeNodes;

    public ScheduledQueue() {
        timeNodes = new TreeMap<TimeStamp, List<T>>();
    }

    public boolean isEmpty() {
        return timeNodes.isEmpty();
    }

    public synchronized void add(TimeStamp time, T obj) {
        List<T> timeList = timeNodes.get(time);
        if (timeList == null) {
            timeList = new LinkedList<T>();
            timeNodes.put(time, timeList);
        }
        timeList.add(obj);
    }

    public synchronized List<T> removeNextList(TimeStamp time) {
        List<T> timeList = null;
        if (!timeNodes.isEmpty()) {
            TimeStamp lowestTime = timeNodes.firstKey();
            // Is the lowest time before or equal to the specified time.
            if (lowestTime.compareTo(time) <= 0) {
                timeList = timeNodes.remove(lowestTime);
            }
        }
        return timeList;
    }

    public synchronized Object removeNext(TimeStamp time) {
        Object next = null;
        if (!timeNodes.isEmpty()) {
            TimeStamp lowestTime = timeNodes.firstKey();
            // Is the lowest time before or equal to the specified time.
            if (lowestTime.compareTo(time) <= 0) {
                List<T> timeList = timeNodes.get(lowestTime);
                if (timeList != null) {
                    next = timeList.remove(0);
                    if (timeList.isEmpty()) {
                        timeNodes.remove(lowestTime);
                    }
                }
            }
        }
        return next;
    }

    public synchronized void clear() {
        timeNodes.clear();
    }

    public TimeStamp getNextTime() {
        return timeNodes.firstKey();
    }

}
