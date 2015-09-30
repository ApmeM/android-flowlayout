package org.apmem.tools.layouts.logic;

import android.view.Gravity;
import android.view.View;

public class ConfigDefinition {
    private int orientation = CommonLogic.HORIZONTAL;
    private boolean debugDraw = false;
    private float weightDefault = 0;
    private int gravity = Gravity.LEFT | Gravity.TOP;
    private int layoutDirection = View.LAYOUT_DIRECTION_LTR;
    private int maxWidth;
    private int maxHeight;
    private boolean checkCanFit;
    private int widthMode;
    private int heightMode;

    public ConfigDefinition() {
        this.setOrientation(CommonLogic.HORIZONTAL);
        this.setDebugDraw(false);
        this.setWeightDefault(0.0f);
        this.setGravity(Gravity.NO_GRAVITY);
        this.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        this.setCheckCanFit(true);
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == CommonLogic.VERTICAL) {
            this.orientation = orientation;
        } else {
            this.orientation = CommonLogic.HORIZONTAL;
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
        this.gravity = gravity;
    }

    public int getLayoutDirection() {
        return layoutDirection;
    }

    public void setLayoutDirection(int layoutDirection) {
        if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            this.layoutDirection = layoutDirection;
        } else {
            this.layoutDirection = View.LAYOUT_DIRECTION_LTR;
        }
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxLength() {
        return this.orientation == CommonLogic.HORIZONTAL ? this.maxWidth : this.maxHeight;
    }

    public int getMaxThickness() {
        return this.orientation == CommonLogic.HORIZONTAL ? this.maxHeight : this.maxWidth;
    }

    public void setCheckCanFit(boolean checkCanFit) {
        this.checkCanFit = checkCanFit;
    }

    public boolean isCheckCanFit() {
        return checkCanFit;
    }

    public void setWidthMode(int widthMode) {
        this.widthMode = widthMode;
    }

    public void setHeightMode(int heightMode) {
        this.heightMode = heightMode;
    }

    public int getLengthMode() {
        return this.orientation == CommonLogic.HORIZONTAL ? this.widthMode : this.heightMode;
    }

    public int getThicknessMode() {
        return this.orientation == CommonLogic.HORIZONTAL ? this.heightMode : this.widthMode;
    }
}
