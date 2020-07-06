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

package com.jsyn.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Rotary controller looks like a knob on a synthesizer. You control this knob by clicking on it and
 * dragging <b>up</b> or <b>down</b>. If you move the mouse to the <b>left</b> of the knob then you
 * will have <b>coarse</b> control. If you move the mouse to the <b>right</b> of the knob then you
 * will have <b>fine</b> control.
 * <P>
 *
 * @author (C) 2010 Phil Burk, Mobileer Inc
 * @version 16.1
 */
public class RotaryController extends JPanel {
    private static final long serialVersionUID = 6681532871556659546L;
    private static final double SENSITIVITY = 0.01;
    private final BoundedRangeModel model;

    private final double minAngle = 1.4 * Math.PI;
    private final double maxAngle = -0.4 * Math.PI;
    private final double unitIncrement = 0.01;
    private int lastY;
    private int startX;
    private Color knobColor = Color.LIGHT_GRAY;
    private Color lineColor = Color.RED;
    private double baseValue;

    public enum Style {
        LINE, LINEDOT, ARROW, ARC
    };

    private Style style = Style.ARC;

    public RotaryController(BoundedRangeModel model) {
        this.model = model;
        setMinimumSize(new Dimension(50, 50));
        setPreferredSize(new Dimension(50, 50));
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                safeRepaint();
            }

        });
    }

    // This can be overridden in subclasses to workaround OpenJDK bugs.
    public void safeRepaint() {
        repaint();
    }

    public BoundedRangeModel getModel() {
        return model;
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            lastY = e.getY();
            startX = e.getX();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isEnabled()) {
                setKnobByXY(e.getX(), e.getY());
            }
        }
    }

    private class MouseMotionHandler extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (isEnabled()) {
                setKnobByXY(e.getX(), e.getY());
            }
        }
    }

    private int getModelRange() {
        return (((model.getMaximum() - model.getExtent()) - model.getMinimum()));
    }

    /**
     * A fractional value is useful for drawing.
     *
     * @return model value as a normalized fraction between 0.0 and 1.0
     */
    public double getFractionFromModel() {
        double value = model.getValue();
        return convertValueToFraction(value);
    }

    private double convertValueToFraction(double value) {
        return (value - model.getMinimum()) / getModelRange();
    }

    private void setKnobByXY(int x, int y) {
        // Scale increment by X position.
        int xdiff = startX - x; // More to left causes bigger increments.
        double power = xdiff * SENSITIVITY;
        double perPixel = unitIncrement * Math.pow(2.0, power);

        int ydiff = lastY - y;
        double fractionalDelta = ydiff * perPixel;
        // Only update the model if we actually change values.
        // This is needed in case the range is small.
        int valueDelta = (int) Math.round(fractionalDelta * getModelRange());
        if (valueDelta != 0) {
            model.setValue(model.getValue() + valueDelta);
            lastY = y;
        }
    }

    private double fractionToAngle(double fraction) {
        return (fraction * (maxAngle - minAngle)) + minAngle;
    }

    private void drawLineIndicator(Graphics g, int x, int y, int radius, double angle,
            boolean drawDot) {
        double arrowSize = radius * 0.95;
        int arrowX = (int) (arrowSize * Math.sin(angle));
        int arrowY = (int) (arrowSize * Math.cos(angle));
        g.setColor(lineColor);
        g.drawLine(x, y, x + arrowX, y - arrowY);
        if (drawDot) {
            // draw little dot at end
            double dotScale = 0.1;
            int dotRadius = (int) (dotScale * arrowSize);
            if (dotRadius > 1) {
                int dotX = x + (int) ((0.99 - dotScale) * arrowX) - dotRadius;
                int dotY = y - (int) ((0.99 - dotScale) * arrowY) - dotRadius;
                g.fillOval(dotX, dotY, dotRadius * 2, dotRadius * 2);
            }
        }
    }

    private void drawArrowIndicator(Graphics g, int x0, int y0, int radius, double angle) {
        int arrowSize = (int) (radius * 0.95);
        int arrowWidth = (int) (radius * 0.2);
        int xp[] = {
                0, arrowWidth, 0, -arrowWidth
        };
        int yp[] = {
                arrowSize, -arrowSize / 2, 0, -arrowSize / 2
        };
        double sa = Math.sin(angle);
        double ca = Math.cos(angle);
        for (int i = 0; i < xp.length; i++) {
            int x = xp[i];
            int y = yp[i];
            xp[i] = x0 - (int) ((x * ca) - (y * sa));
            yp[i] = y0 - (int) ((x * sa) + (y * ca));
        }
        g.fillPolygon(xp, yp, xp.length);
    }

    private void drawArcIndicator(Graphics g, int x, int y, int radius, double angle) {
        final double DEGREES_PER_RADIAN = 180.0 / Math.PI;
        final int minAngleDegrees = (int) (minAngle * DEGREES_PER_RADIAN);
        final int maxAngleDegrees = (int) (maxAngle * DEGREES_PER_RADIAN);

        int zeroAngleDegrees = (int) (fractionToAngle(baseValue) * DEGREES_PER_RADIAN);

        double arrowSize = radius * 0.95;
        int arcX = x - radius;
        int arcY = y - radius;
        int arcAngle = (int) (angle * DEGREES_PER_RADIAN);
        int arrowX = (int) (arrowSize * Math.cos(angle));
        int arrowY = (int) (arrowSize * Math.sin(angle));

        g.setColor(knobColor.darker().darker());
        g.fillArc(arcX, arcY, 2 * radius, 2 * radius, minAngleDegrees, maxAngleDegrees
                - minAngleDegrees);
        g.setColor(Color.ORANGE);
        g.fillArc(arcX, arcY, 2 * radius, 2 * radius, zeroAngleDegrees, arcAngle - zeroAngleDegrees);

        // fill in middle
        int arcWidth = radius / 4;
        int diameter = ((radius - arcWidth) * 2);
        g.setColor(knobColor);
        g.fillOval(arcWidth + x - radius, arcWidth + y - radius, diameter, diameter);

        g.setColor(lineColor);
        g.drawLine(x, y, x + arrowX, y - arrowY);

    }

    /**
     * Override this method if you want to draw your own line or dot on the knob.
     */
    public void drawIndicator(Graphics g, int x, int y, int radius, double angle) {
        g.setColor(isEnabled() ? lineColor : lineColor.darker());
        switch (style) {
            case LINE:
                drawLineIndicator(g, x, y, radius, angle, false);
                break;
            case LINEDOT:
                drawLineIndicator(g, x, y, radius, angle, true);
                break;
            case ARROW:
                drawArrowIndicator(g, x, y, radius, angle);
                break;
            case ARC:
                drawArcIndicator(g, x, y, radius, angle);
                break;
        }
    }

    /**
     * Override this method if you want to draw your own knob.
     *
     * @param g graphics context
     * @param x position of center of knob
     * @param y position of center of knob
     * @param radius of knob in pixels
     * @param angle in radians. Zero is straight up.
     */
    public void drawKnob(Graphics g, int x, int y, int radius, double angle) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int diameter = radius * 2;
        // Draw shaded side.
        g.setColor(knobColor.darker());
        g.fillOval(x - radius + 2, y - radius + 2, diameter, diameter);
        g.setColor(knobColor);
        g.fillOval(x - radius, y - radius, diameter, diameter);

        // Draw line or other indicator of knob position.
        drawIndicator(g, x, y, radius, angle);
    }

    // Draw the round knob based on the current size and model value.
    // This used to have a bug where the scope would draw in this components background.
    // Then I changed it from overriding paint() to overriding paintComponent() and it worked.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int x = width / 2;
        int y = height / 2;

        // Calculate radius from size of component.
        int diameter = (width < height) ? width : height;
        diameter -= 4;
        int radius = diameter / 2;

        double angle = fractionToAngle(getFractionFromModel());
        drawKnob(g, x, y, radius, angle);
    }

    public Color getKnobColor() {
        return knobColor;
    }

    /**
     * @param knobColor color of body of knob
     */
    public void setKnobColor(Color knobColor) {
        this.knobColor = knobColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    /**
     * @param lineColor color of indicator on knob like a line or arrow
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Style getStyle() {
        return style;
    }

    public double getBaseValue() {
        return baseValue;
    }

    /*
     * Specify where the orange arc originates. For example a pan knob with a centered arc would
     * have a baseValue of 0.5.
     * @param baseValue a fraction between 0.0 and 1.0.
     */
    public void setBaseValue(double baseValue) {
        if (baseValue < 0.0) {
            baseValue = 0.0;
        } else if (baseValue > 1.0) {
            baseValue = 1.0;
        }
        this.baseValue = baseValue;
    }

}
