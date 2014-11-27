package org.apmem.tools.layouts;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

class LineDefinition {
    private final List<View> views = new ArrayList<View>();
    private final LayoutConfiguration config;
    private int lineLength;
    private int lineThickness;
    private int lineLengthWithSpacing;
    private int lineThicknessWithSpacing;
    private int linePosition;
    private final int maxLength;

    public LineDefinition(int linePosition, int maxLength, LayoutConfiguration config) {
        this.linePosition = linePosition;
        this.maxLength = maxLength;
        this.config = config;
    }

    public void addView(View child) {
        final FlowLayout.LayoutParams lp = (FlowLayout.LayoutParams) child.getLayoutParams();
        final int hSpacing = lp.horizontalSpacingSpecified() ? lp.horizontalSpacing : this.config.getHorizontalSpacing();
        final int vSpacing = lp.verticalSpacingSpecified() ? lp.verticalSpacing : this.config.getVerticalSpacing();

        final int childLength;
        final int childThickness;
        final int spacingLength;
        final int spacingThickness;
        if (this.config.getOrientation() == FlowLayout.HORIZONTAL) {
            childLength = child.getMeasuredWidth();
            childThickness = child.getMeasuredHeight();
            spacingLength = hSpacing;
            spacingThickness = vSpacing;
        } else {
            childLength = child.getMeasuredHeight();
            childThickness = child.getMeasuredWidth();
            spacingLength = vSpacing;
            spacingThickness = hSpacing;
        }

        this.views.add(child);

        this.lineLength = this.lineLengthWithSpacing + childLength;
        this.lineLengthWithSpacing = this.lineLength + spacingLength;
        this.lineThicknessWithSpacing = Math.max(this.lineThicknessWithSpacing, childThickness + spacingThickness);
        this.lineThickness = Math.max(this.lineThickness, childThickness);
    }

    public boolean canFit(View child) {
        final int childLength;
        if (this.config.getOrientation() == FlowLayout.HORIZONTAL) {
            childLength = child.getMeasuredWidth();
        } else {
            childLength = child.getMeasuredHeight();
        }
        return lineLengthWithSpacing + childLength <= maxLength;
    }

    public int getLinePosition() {
        return linePosition;
    }

    public int getLineThicknessWithSpacing() {
        return lineThicknessWithSpacing;
    }

    public int getLineLengthWithSpacing() {
        return lineLengthWithSpacing;
    }

    public int getLineLength() {
        return lineLength;
    }

    public int getLineThickness() {
        return lineThickness;
    }

    public List<View> getViews() {
        return views;
    }
}