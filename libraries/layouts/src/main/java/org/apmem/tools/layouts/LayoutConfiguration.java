package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;

class LayoutConfiguration {
    private int orientation = FlowLayout.HORIZONTAL;
    private boolean debugDraw = false;
    private float weightDefault = 0;
    private int gravity = Gravity.LEFT | Gravity.TOP;
    private int layoutDirection = FlowLayout.LAYOUT_DIRECTION_LTR;

    public LayoutConfiguration(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        try {
            this.setOrientation(a.getInteger(R.styleable.FlowLayout_android_orientation, FlowLayout.HORIZONTAL));
            this.setDebugDraw(a.getBoolean(R.styleable.FlowLayout_debugDraw, false));
            this.setWeightDefault(a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f));
            this.setGravity(a.getInteger(R.styleable.FlowLayout_android_gravity, Gravity.NO_GRAVITY));
            this.setLayoutDirection(a.getInteger(R.styleable.FlowLayout_layoutDirection, FlowLayout.LAYOUT_DIRECTION_LTR));
        } finally {
            a.recycle();
        }
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

    public int getLayoutDirection() {
        return layoutDirection;
    }

    public void setLayoutDirection(int layoutDirection) {
        if (layoutDirection == FlowLayout.LAYOUT_DIRECTION_RTL) {
            this.layoutDirection = layoutDirection;
        } else {
            this.layoutDirection = FlowLayout.LAYOUT_DIRECTION_LTR;
        }
    }
}
