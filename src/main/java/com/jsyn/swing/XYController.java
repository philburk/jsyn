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

package com.jsyn.swing;

import javax.swing.JPanel;

/**
 * Root class for 2 dimensional X,Y controller for wave editors, Theremins, etc. Maps pixel
 * coordinates into "world" coordinates.
 *
 * @author (C) 1997 Phil Burk, SoftSynth.com
 */

public class XYController extends JPanel {
    double minWorldX = 0.0;
    double maxWorldX = 1.0;
    double minWorldY = 0.0;
    double maxWorldY = 1.0;

    public XYController() {
    }

    public XYController(double minWX, double minWY, double maxWX, double maxWY) {
        setMinWorldX(minWX);
        setMaxWorldX(maxWX);
        setMinWorldY(minWY);
        setMaxWorldY(maxWY);
    }

    /**
     * Set minimum World coordinate value for the horizontal X dimension. The minimum value
     * corresponds to the left of the component.
     */
    public void setMinWorldX(double minWX) {
        minWorldX = minWX;
    }

    public double getMinWorldX() {
        return minWorldX;
    }

    /**
     * Set maximum World coordinate value for the horizontal X dimension. The minimum value
     * corresponds to the right of the component.
     */
    public void setMaxWorldX(double maxWX) {
        maxWorldX = maxWX;
    }

    public double getMaxWorldX() {
        return maxWorldX;
    }

    /**
     * Set minimum World coordinate value for the vertical Y dimension. The minimum value
     * corresponds to the bottom of the component.
     */
    public void setMinWorldY(double minWY) {
        minWorldY = minWY;
    }

    public double getMinWorldY() {
        return minWorldY;
    }

    /**
     * Set maximum World coordinate value for the vertical Y dimension. The maximum value
     * corresponds to the top of the component.
     */
    public void setMaxWorldY(double maxWY) {
        maxWorldY = maxWY;
    }

    public double getMaxWorldY() {
        return maxWorldY;
    }

    /** Convert from graphics coordinates (pixels) to world coordinates. */
    public double convertGXtoWX(int gx) {
        int width = getWidth();
        return minWorldX + ((maxWorldX - minWorldX) * gx) / width;
    }

    public double convertGYtoWY(int gy) {
        int height = getHeight();
        return minWorldY + ((maxWorldY - minWorldY) * (height - gy)) / height;
    }

    /** Convert from world coordinates to graphics coordinates (pixels). */
    public int convertWXtoGX(double wx) {
        int width = getWidth();
        return (int) (((wx - minWorldX) * width) / (maxWorldX - minWorldX));
    }

    public int convertWYtoGY(double wy) {
        int height = getHeight();
        return height - (int) (((wy - minWorldY) * height) / (maxWorldY - minWorldY));
    }

    /** Clip wx to the min and max World X values. */
    public double clipWorldX(double wx) {
        if (wx < minWorldX)
            wx = minWorldX;
        else if (wx > maxWorldX)
            wx = maxWorldX;
        return wx;
    }

    /** Clip wy to the min and max World Y values. */
    public double clipWorldY(double wy) {
        if (wy < minWorldY)
            wy = minWorldY;
        else if (wy > maxWorldY)
            wy = maxWorldY;
        return wy;
    }

}
