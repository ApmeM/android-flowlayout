package org.apmem.tools.layouts;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

class LineDefinition {
    private final List<ViewContainer> views = new ArrayList<ViewContainer>();
    private final LayoutConfiguration config;
    private final int maxLength;
    private int lineLength;
    private int lineThickness;
    private int lineLengthWithSpacing;
    private int lineThicknessWithSpacing;
    private int lineStartThickness;
    private int lineStartLength;

    public LineDefinition(int lineStartPosition, int maxLength, LayoutConfiguration config) {
        this.lineStartThickness = lineStartPosition;
        this.lineStartLength = 0;
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

        final ViewContainer container = new ViewContainer(child, spacingLength, spacingThickness);
        container.setInlineStartLength(this.lineLengthWithSpacing);
        container.setLength(childLength);
        container.setThickness(childThickness);

        this.views.add(container);

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

    public int getLineStartThickness() {
        return lineStartThickness;
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

    public int getLineStartLength() {
        return lineStartLength;
    }

    public int getLineThickness() {
        return lineThickness;
    }

    public List<ViewContainer> getViews() {
        return views;
    }

    public void addThickness(int extraThickness) {
        this.lineThickness += extraThickness;
        this.lineThicknessWithSpacing += extraThickness;
    }

    public void addStartThickness(int extraLineStartThickness) {
        this.lineStartThickness += extraLineStartThickness;
    }

    public void addLength(int extraLength) {
        this.lineLength += extraLength;
        this.lineLengthWithSpacing += extraLength;
    }

    public void addStartLength(int extraLineStartLength) {
        this.lineStartLength += extraLineStartLength;
    }
}