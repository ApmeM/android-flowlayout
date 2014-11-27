package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;

class LayoutConfiguration {
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private int orientation = FlowLayout.HORIZONTAL;
    private boolean debugDraw = false;
    private float weightSum = 0;
    private float weightDefault = 0;
    private int gravity = Gravity.LEFT | Gravity.TOP;
    private int fillLines = FlowLayout.FILL_LINES_NONE;

    public LayoutConfiguration(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        try {
            this.setHorizontalSpacing(a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, 0));
            this.setVerticalSpacing(a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 0));
            this.setOrientation(a.getInteger(R.styleable.FlowLayout_orientation, FlowLayout.HORIZONTAL));
            this.setDebugDraw(a.getBoolean(R.styleable.FlowLayout_debugDraw, false));
            this.setWeightSum(a.getFloat(R.styleable.FlowLayout_weightSum, 0.0f));
            this.setWeightDefault(a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f));
            this.setGravity(a.getInteger(R.styleable.FlowLayout_android_gravity, Gravity.NO_GRAVITY));
            this.setFillLines(a.getInteger(R.styleable.FlowLayout_fillLines, FlowLayout.FILL_LINES_NONE));
        } finally {
            a.recycle();
        }
    }

    public int getHorizontalSpacing() {
        return this.horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = Math.max(0, horizontalSpacing);
    }

    public int getVerticalSpacing() {
        return this.verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = Math.max(0, verticalSpacing);
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == FlowLayout.VERTICAL) {
            this.orientation = orientation;
        } else {
            this.orientation = FlowLayout.HORIZONTAL;
        }
    }

    public boolean isDebugDraw() {
        return this.debugDraw;
    }

    public void setDebugDraw(boolean debugDraw) {
        this.debugDraw = debugDraw;
    }

    public float getWeightSum() {
        return this.weightSum;
    }

    public void setWeightSum(float weightSum) {
        this.weightSum = Math.max(0, weightSum);
    }

    public float getWeightDefault() {
        return this.weightDefault;
    }

    public void setWeightDefault(float weightDefault) {
        this.weightDefault = Math.max(0, weightDefault);
    }

    public int getGravity() {
        return this.gravity;
    }

    public void setGravity(int gravity) {
        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.LEFT;
        }

        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.TOP;
        }

        this.gravity = gravity;
    }

    public int getFillLines() {
        return this.fillLines;
    }

    public void setFillLines(int fillLines) {
        if (fillLines != FlowLayout.FILL_LINES_EXCEPT_LAST && fillLines != FlowLayout.FILL_LINES_ALL)
            this.fillLines = FlowLayout.FILL_LINES_NONE;
        else
            this.fillLines = fillLines;
    }
}
