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

package com.jsyn.util;

import java.util.concurrent.CopyOnWriteArrayList;

public class TransportModel {
    public static final int STATE_STOPPED = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_RUNNING = 2;

    private CopyOnWriteArrayList<TransportListener> listeners = new CopyOnWriteArrayList<TransportListener>();
    private int state = STATE_STOPPED;
    private long position;

    public void addTransportListener(TransportListener listener) {
        listeners.add(listener);
    }

    public void removeTransportListener(TransportListener listener) {
        listeners.remove(listener);
    }

    public void setState(int newState) {
        state = newState;
        fireStateChanged(newState);
    }

    public int getState() {
        return state;
    }

    public void setPosition(long newPosition) {
        position = newPosition;
        firePositionChanged(newPosition);
    }

    public long getPosition() {
        return position;
    }

    public void fireStateChanged(int newState) {
        for (TransportListener listener : listeners) {
            listener.stateChanged(this, newState);
        }
    }

    public void firePositionChanged(long newPosition) {
        for (TransportListener listener : listeners) {
            listener.positionChanged(this, newPosition);
        }
    }
}
