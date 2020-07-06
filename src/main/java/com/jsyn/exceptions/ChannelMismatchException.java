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

package com.jsyn.exceptions;

/**
 * This will get thrown if, for example, stereo data is queued to a mono player.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class ChannelMismatchException extends RuntimeException {

    public ChannelMismatchException(String message) {
        super(message);
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = -5345224363387498119L;

}
