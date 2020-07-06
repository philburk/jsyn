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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.jsyn.data.FloatSample;

public interface AudioSampleLoader {
    /**
     * Load a FloatSample from a File object.
     */
    public FloatSample loadFloatSample(File fileIn) throws IOException;

    /**
     * Load a FloatSample from an InputStream. This is handy when loading Resources from a JAR file.
     */
    public FloatSample loadFloatSample(InputStream inputStream) throws IOException;

    /**
     * Load a FloatSample from a URL.. This is handy when loading Resources from a website.
     */
    public FloatSample loadFloatSample(URL url) throws IOException;

}
