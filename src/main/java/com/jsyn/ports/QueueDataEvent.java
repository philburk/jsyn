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

package com.jsyn.ports;

import java.util.EventObject;

import com.jsyn.data.SequentialData;

/**
 * An event that is passed to a UnitDataQueueCallback when the element in the queue is played..
 * 
 * @author Phil Burk 2009 Mobileer Inc
 */
public class QueueDataEvent extends EventObject {
    private static final long serialVersionUID = 176846633064538053L;
    protected SequentialData sequentialData;
    protected int startFrame;
    protected int numFrames;
    protected int numLoops;
    protected int loopsLeft;
    protected int crossFadeIn;
    protected boolean skipIfOthers;
    protected boolean autoStop;
    protected boolean immediate;

    public QueueDataEvent(Object arg0) {
        super(arg0);
    }

    public boolean isSkipIfOthers() {
        return skipIfOthers;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public SequentialData getSequentialData() {
        return sequentialData;
    }

    public int getCrossFadeIn() {
        return crossFadeIn;
    }

    public int getStartFrame() {
        return startFrame;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public int getNumLoops() {
        return numLoops;
    }

    public int getLoopsLeft() {
        return loopsLeft;
    }

    public boolean isAutoStop() {
        return autoStop;
    }

}
