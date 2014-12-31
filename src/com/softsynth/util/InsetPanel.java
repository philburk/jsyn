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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Panel;

/**
 * Panel with insets that can be used to border a centered component.
 * 
 * @author (C) 1997 Phil Burk, SoftSynth.com
 */
public class InsetPanel extends Panel {
    int border; /* Pixels in border. */
    static final int default_border = 6;

    public InsetPanel(Component borderMe) {
        this(borderMe, Color.blue, default_border);
    }

    public InsetPanel(Component borderMe, Color borderColor) {
        this(borderMe, borderColor, default_border);
    }

    public InsetPanel(Component borderMe, int border) {
        this(borderMe, Color.blue, border);
    }

    /**
     * @param borderMe component to be centerred in this panel. Typically another Panel.
     * @param borderColor color to paint the border.
     * @param border width in pixels of border.
     */
    public InsetPanel(Component borderMe, Color borderColor, int border) {
        this.border = border;
        setLayout(new BorderLayout());
        /* Force a background for the component so the border shows up. */
        if (borderMe != null) {
            add("Center", borderMe);
            if (borderMe.getBackground() == null)
                borderMe.setBackground(Color.white);
        }
        if (borderColor != null)
            setBackground(borderColor);
    }

    @Override
    public Insets insets() {
        return new Insets(border, border, border, border);
    }

    /** @param border width in pixels of border. */
    public void setBorder(int borderWidth) {
        border = borderWidth;
        repaint();
    }

    public int getBorder() {
        return border;
    }
}
