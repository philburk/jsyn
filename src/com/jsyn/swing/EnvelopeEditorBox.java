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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.VariableRateDataReader;

/**
 * Edit a list of ordered duration,value pairs suitable for use with a SegmentedEnvelope.
 *
 * @author (C) 1997-2013 Phil Burk, SoftSynth.com
 * @see EnvelopePoints
 * @see SegmentedEnvelope
 * @see VariableRateDataReader
 */

/* ========================================================================== */
public class EnvelopeEditorBox extends XYController implements MouseListener, MouseMotionListener {
    EnvelopePoints points;
    ArrayList<EditListener> listeners = new ArrayList<EditListener>();
    int dragIndex = -1;
    double dragLowLimit;
    double dragHighLimit;
    double draggedPoint[];
    double xBefore; // WX value before point
    double xPicked; // WX value of picked point
    double dragWX;
    double dragWY;
    int maxPoints = Integer.MAX_VALUE;
    int radius = 4;
    double verticalBarSpacing = 1.0;
    boolean verticalBarsEnabled = false;
    double maximumXRange = Double.MAX_VALUE;
    double minimumXRange = 0.1;
    int rangeStart = -1; // gx coordinates
    int rangeEnd = -1;
    int mode = EDIT_POINTS;
    public final static int EDIT_POINTS = 0;
    public final static int SELECT_SUSTAIN = 1;
    public final static int SELECT_RELEASE = 2;

    Color rangeColor = Color.RED;
    Color sustainColor = Color.BLUE;
    Color releaseColor = Color.YELLOW;
    Color overlapColor = Color.GREEN;
    Color firstLineColor = Color.GRAY;

    public interface EditListener {
        public void objectEdited(Object editor, Object edited);
    }

    public EnvelopeEditorBox() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setMaximumXRange(double maxXRange) {
        maximumXRange = maxXRange;
    }

    public double getMaximumXRange() {
        return maximumXRange;
    }

    public void setMinimumXRange(double minXRange) {
        minimumXRange = minXRange;
    }

    public double getMinimumXRange() {
        return minimumXRange;
    }

    public void setSelection(int start, int end) {
        switch (mode) {
            case SELECT_SUSTAIN:
                points.setSustainLoop(start, end);
                break;
            case SELECT_RELEASE:
                points.setReleaseLoop(start, end);
                break;
        }
        // System.out.println("start = " + start + ", end = " + end );
    }

    /** Set mode to either EDIT_POINTS or SELECT_SUSTAIN, SELECT_RELEASE; */
    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    /**
     * Add a listener to receive edit events. Listener will be passed the editor object and the
     * edited object.
     */
    public void addEditListener(EditListener listener) {
        listeners.add(listener);
    }

    public void removeEditListener(EditListener listener) {
        listeners.remove(listener);
    }

    /** Send event to every subscribed listener. */
    public void fireObjectEdited() {
        for (EditListener listener : listeners) {
            listener.objectEdited(this, points);
        }
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public int getNumPoints() {
        return points.size();
    }

    public void setPoints(EnvelopePoints points) {
        this.points = points;
        setMaxWorldY(points.getMaximumValue());
    }

    public EnvelopePoints getPoints() {
        return points;
    }

    /**
     * Return index of point before this X position.
     */
    private int findPointBefore(double wx) {
        int pnt = -1;
        double px = 0.0;
        xBefore = 0.0;
        for (int i = 0; i < points.size(); i++) {
            px += points.getDuration(i);
            if (px > wx)
                break;
            pnt = i;
            xBefore = px;
        }
        return pnt;
    }

    private int pickPoint(double wx, double wxAperture, double wy, double wyAperture) {
        double px = 0.0;
        double wxLow = wx - wxAperture;
        double wxHigh = wx + wxAperture;
        // System.out.println("wxLow = " + wxLow + ", wxHigh = " + wxHigh );
        double wyLow = wy - wyAperture;
        double wyHigh = wy + wyAperture;
        // System.out.println("wyLow = " + wyLow + ", wyHigh = " + wyHigh );
        double wxScale = 1.0 / wxAperture; // only divide once, then multiply
        double wyScale = 1.0 / wyAperture;
        int bestPoint = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double dar[] = points.getPoint(i);
            px += dar[0];
            double py = dar[1];
            // System.out.println("px = " + px + ", py = " + py );
            if ((px > wxLow) && (px < wxHigh) && (py > wyLow) && (py < wyHigh)) {
                /* Inside pick range. Calculate distance squared. */
                double ndx = (px - wx) * wxScale;
                double ndy = (py - wy) * wyScale;
                double dist = (ndx * ndx) + (ndy * ndy);
                // System.out.println("dist = " + dist );
                if (dist < bestDistance) {
                    bestPoint = i;
                    bestDistance = dist;
                    xPicked = px;
                }
            }
        }
        return bestPoint;
    }

    private void clickDownRange(boolean shiftDown, int gx, int gy) {
        setSelection(-1, -1);
        rangeStart = rangeEnd = gx;
        repaint();
    }

    private void dragRange(int gx, int gy) {
        rangeEnd = gx;
        repaint();
    }

    private void clickUpRange(int gx, int gy) {
        dragRange(gx, gy);
        if (rangeEnd < rangeStart) {
            int temp = rangeEnd;
            rangeEnd = rangeStart;
            rangeStart = temp;
        }
        // System.out.println("clickUpRange: gx = " + gx + ", rangeStart = " +
        // rangeStart );
        double wx = convertGXtoWX(rangeStart);
        int i0 = findPointBefore(wx);
        wx = convertGXtoWX(rangeEnd);
        int i1 = findPointBefore(wx);

        if (i1 == i0) {
            // set single point at zero so there is nothing played for queueOn()
            if (gx < 0) {
                setSelection(0, 0);
            }
            // else clear any existing loop
        } else if (i1 == (i0 + 1)) {
            setSelection(i1 + 1, i1 + 1); // set to a single point
        } else if (i1 > (i0 + 1)) {
            setSelection(i0 + 1, i1 + 1); // set to a range of two or more
        }

        rangeStart = -1;
        rangeEnd = -1;
        fireObjectEdited();
    }

    private void clickDownPoints(boolean shiftDown, int gx, int gy) {
        dragIndex = -1;
        double wx = convertGXtoWX(gx);
        double wy = convertGYtoWY(gy);
        // calculate world values for aperture
        double wxAp = convertGXtoWX(radius + 2) - convertGXtoWX(0);
        // System.out.println("wxAp = " + wxAp );
        double wyAp = convertGYtoWY(0) - convertGYtoWY(radius + 2);
        // System.out.println("wyAp = " + wyAp );
        int pnt = pickPoint(wx, wxAp, wy, wyAp);
        // System.out.println("pickPoint = " + pnt);
        if (shiftDown) {
            if (pnt >= 0) {
                points.removePoint(pnt);
                repaint();
            }
        } else {
            if (pnt < 0) // didn't hit one so look for point to left of click
            {
                if (points.size() < maxPoints) // add if room
                {
                    pnt = findPointBefore(wx);
                    // System.out.println("pointBefore = " + pnt);
                    dragIndex = pnt + 1;
                    if (pnt == (points.size() - 1)) {
                        points.add(wx - xBefore, wy);
                    } else {
                        points.insert(dragIndex, wx - xBefore, wy);
                    }
                    dragLowLimit = xBefore;
                    dragHighLimit = wx + (maximumXRange - points.getTotalDuration());
                    repaint();
                }
            } else
            // hit one so drag it
            {
                dragIndex = pnt;
                if (dragIndex <= 0)
                    dragLowLimit = 0.0; // FIXME envelope drag limit
                else
                    dragLowLimit = xPicked - points.getPoint(dragIndex)[0];
                dragHighLimit = xPicked + (maximumXRange - points.getTotalDuration());
                // System.out.println("dragLowLimit = " + dragLowLimit );
            }
        }
        // Set up drag point if we are dragging.
        if (dragIndex >= 0) {
            draggedPoint = points.getPoint(dragIndex);
        }

    }

    private void dragPoint(int gx, int gy) {
        if (dragIndex < 0)
            return;

        double wx = convertGXtoWX(gx);
        if (wx < dragLowLimit)
            wx = dragLowLimit;
        else if (wx > dragHighLimit)
            wx = dragHighLimit;
        draggedPoint[0] = wx - dragLowLimit; // duration

        double wy = convertGYtoWY(gy);
        wy = clipWorldY(wy);
        draggedPoint[1] = wy;
        dragWY = wy;
        dragWX = wx;
        points.setDirty(true);
        repaint();
    }

    private void clickUpPoints(int gx, int gy) {
        dragPoint(gx, gy);
        fireObjectEdited();
        dragIndex = -1;
    }

    // Implement the MouseMotionListener interface for AWT 1.1
    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (points == null)
            return;
        if (mode == EDIT_POINTS) {
            dragPoint(x, y);
        } else {
            dragRange(x, y);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    // Implement the MouseListener interface for AWT 1.1
    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (points == null)
            return;
        if (mode == EDIT_POINTS) {
            clickDownPoints(e.isShiftDown(), x, y);
        } else {
            clickDownRange(e.isShiftDown(), x, y);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (points == null)
            return;
        if (mode == EDIT_POINTS) {
            clickUpPoints(x, y);
        } else {
            clickUpRange(x, y);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Draw selected range.
     */
    private void drawRange(Graphics g) {
        if (rangeStart >= 0) {
            int height = getHeight();
            int gx0 = 0, gx1 = 0;

            if (rangeEnd < rangeStart) {
                gx0 = rangeEnd;
                gx1 = rangeStart;
            } else {
                gx0 = rangeStart;
                gx1 = rangeEnd;
            }
            g.setColor(rangeColor);
            g.fillRect(gx0, 0, gx1 - gx0, height);
        }
    }

    private void drawUnderSelection(Graphics g, int start, int end) {
        if (start >= 0) {
            int height = getHeight();
            int gx0 = 0, gx1 = radius;
            double wx = 0.0;
            for (int i = 0; i <= (end - 1); i++) {
                double dar[] = (double[]) points.elementAt(i);
                wx += dar[0];
                if (start == (i + 1)) {
                    gx0 = convertWXtoGX(wx) + radius;
                }
                if (end == (i + 1)) {
                    gx1 = convertWXtoGX(wx) + radius;
                }
            }
            if (gx0 == gx1)
                gx0 = gx0 - radius;
            g.fillRect(gx0, 0, gx1 - gx0, height);
        }
    }

    private void drawSelections(Graphics g) {
        int sus0 = points.getSustainBegin();
        int sus1 = points.getSustainEnd();
        int rel0 = points.getReleaseBegin();
        int rel1 = points.getReleaseEnd();

        g.setColor(sustainColor);
        drawUnderSelection(g, sus0, sus1);
        g.setColor(releaseColor);
        drawUnderSelection(g, rel0, rel1);
        // draw overlapping sustain and release region
        if (sus1 >= rel0) {
            int sel1 = (rel1 < sus1) ? rel1 : sus1;
            g.setColor(overlapColor);
            drawUnderSelection(g, rel0, sel1);
        }
    }

    /**
     * Override this to draw a grid or other stuff under the envelope.
     */
    public void drawUnderlay(Graphics g) {
        if (dragIndex < 0) {
            drawSelections(g);
            drawRange(g);
        }
        if (verticalBarsEnabled)
            drawVerticalBars(g);
    }

    public void setVerticalBarsEnabled(boolean flag) {
        verticalBarsEnabled = flag;
    }

    public boolean areVerticalBarsEnabled() {
        return verticalBarsEnabled;
    }

    /**
     * Set spacing in world coordinates.
     */
    public void setVerticalBarSpacing(double spacing) {
        verticalBarSpacing = spacing;
    }

    public double getVerticalBarSpacing() {
        return verticalBarSpacing;
    }

    /**
     * Draw vertical lines.
     */
    private void drawVerticalBars(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        double wx = verticalBarSpacing;
        int gx;

        // g.setColor( getBackground().darker() );
        g.setColor(Color.lightGray);
        while (true) {
            gx = convertWXtoGX(wx);
            if (gx > width)
                break;
            g.drawLine(gx, 0, gx, height);
            wx += verticalBarSpacing;
        }
    }

    public void drawPoints(Graphics g, Color lineColor) {
        double wx = 0.0;
        int gx1 = 0;
        int gy1 = getHeight();
        for (int i = 0; i < points.size(); i++) {
            double dar[] = (double[]) points.elementAt(i);
            wx += dar[0];
            double wy = dar[1];
            int gx2 = convertWXtoGX(wx);
            int gy2 = convertWYtoGY(wy);
            if (i == 0) {
                g.setColor(isEnabled() ? firstLineColor : firstLineColor.darker());
                g.drawLine(gx1, gy1, gx2, gy2);
                g.setColor(isEnabled() ? lineColor : lineColor.darker());
            } else if (i > 0) {
                g.drawLine(gx1, gy1, gx2, gy2);
            }
            int diameter = (2 * radius) + 1;
            g.fillOval(gx2 - radius, gy2 - radius, diameter, diameter);
            gx1 = gx2;
            gy1 = gy2;
        }
    }

    public void drawAllPoints(Graphics g) {
        drawPoints(g, getForeground());
    }

    /* Override default paint action. */
    @Override
    public void paint(Graphics g) {
        double wx = 0.0;
        int width = getWidth();
        int height = getHeight();

        // draw background and erase all values
        g.setColor(isEnabled() ? getBackground() : getBackground().darker());
        g.fillRect(0, 0, width, height);

        if (points == null) {
            g.setColor(getForeground());
            g.drawString("No EnvelopePoints", 10, 30);
            return;
        }

        // Determine total duration.
        if (points.size() > 0) {
            wx = points.getTotalDuration();
            // Adjust max X so that we see entire circle of last point.
            double radiusWX = this.convertGXtoWX(radius) - this.getMinWorldX();
            double wxFar = wx + radiusWX;
            if (wxFar > getMaxWorldX()) {
                if (wx > maximumXRange)
                    wxFar = maximumXRange;
                setMaxWorldX(wxFar);
            } else if (wx < (getMaxWorldX() * 0.7)) {
                double newMax = wx / 0.7001; // make slightly larger to prevent
                                             // endless jitter, FIXME - still
                                             // needed after repaint()
                                             // removed from setMaxWorldX?
                // System.out.println("newMax = " + newMax );
                if (newMax < minimumXRange)
                    newMax = minimumXRange;
                setMaxWorldX(newMax);
            }
        }
        // System.out.println("total X = " + wx );

        drawUnderlay(g);

        drawAllPoints(g);

        /* Show X,Y,TotalX as text. */
        g.drawString(points.getName() + ", len=" + String.format("%7.3f", wx), 5, 15);
        if ((draggedPoint != null) && (dragIndex >= 0)) {
            String s = "i=" + dragIndex + ", dur="
                    + String.format("%7.3f", draggedPoint[0]) + ", y = "
                    + String.format("%8.4f", draggedPoint[1]);
            g.drawString(s, 5, 30);
        }
    }
}
