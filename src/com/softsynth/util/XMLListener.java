/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

package com.softsynth.util;

/**
 * Listener for parsing an XML stream.
 * 
 * @author (C) 1997 Phil Burk
 * @see XMLReader
 * @see XMLPrinter
 */

public interface XMLListener {
    /** Handles the start of an element. The flag ifEmpty if there is no content or endTag. */
    void beginElement(String tag, java.util.Hashtable attributes, boolean ifEmpty);

    /** Handles the content of an element. */
    void foundContent(String content);

    /** Handles the end of an element. */
    void endElement(String tag);
}
