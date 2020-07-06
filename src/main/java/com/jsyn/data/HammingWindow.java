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

package com.jsyn.data;

public class HammingWindow implements SpectralWindow {
    private double[] data;

    /** Construct a generalized Hamming Window */
    public HammingWindow(int length, double alpha, double beta) {
        data = new double[length];
        double scaler = 2.0 * Math.PI / (length - 1);
        for (int i = 0; i < length; i++) {
            data[i] = alpha - (beta * (Math.cos(i * scaler)));
        }
    }

    /** Traditional Hamming Window with alpha = 25/46 and beta = 21/46 */
    public HammingWindow(int length) {
        this(length, 25.0 / 46.0, 21.0 / 46.0);
    }

    @Override
    public double get(int index) {
        return data[index];
    }

}
