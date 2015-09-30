package org.apmem.tools.layouts.logic;

import android.view.Gravity;
import android.view.View;

public class ViewDefinition {
    private final ConfigDefinition config;
    private final View view;
    private int inlineStartLength;
    private float weight;
    private int gravity;
    private boolean newLine;
    private int inlineStartThickness;
    private int width;
    private int height;
    private int leftMargin;
    private int topMargin;
    private int rightMargin;
    private int bottomMargin;

    public ViewDefinition(ConfigDefinition config, View child) {
        this.config = config;
        this.view = child;
    }

    public int getLength() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? width : height;
    }

    public void setLength(int length) {
        if (this.config.getOrientation() == CommonLogic.HORIZONTAL) {
            width = length;
        } else {
            height = length;
        }
    }

    public int getSpacingLength() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? this.leftMargin + this.rightMargin : this.topMargin + this.bottomMargin;
    }

    public int getThickness() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? height : width;
    }

    public void setThickness(int thickness) {
        if (this.config.getOrientation() == CommonLogic.HORIZONTAL) {
            height = thickness;
        } else {
            width = thickness;
        }
    }

    public int getSpacingThickness() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? this.topMargin + this.bottomMargin : this.leftMargin + this.rightMargin;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean weightSpecified() {
        return this.weight >= 0;
    }

    public int getInlineStartLength() {
        return inlineStartLength;
    }

    public void setInlineStartLength(int inlineStartLength) {
        this.inlineStartLength = inlineStartLength;
    }

    public boolean gravitySpecified() {
        return gravity != Gravity.NO_GRAVITY;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public boolean isNewLine() {
        return newLine;
    }

    public void setNewLine(boolean newLine) {
        this.newLine = newLine;
    }

    public View getView() {
        return view;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getInlineStartThickness() {
        return inlineStartThickness;
    }

    public void setInlineStartThickness(int inlineStartThickness) {
        this.inlineStartThickness = inlineStartThickness;
    }

    public void setMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
        this.rightMargin = rightMargin;
        this.bottomMargin = bottomMargin;
    }

    public int getInlineX() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? this.inlineStartLength : this.inlineStartThickness;
    }
    public int getInlineY() {
        return this.config.getOrientation() == CommonLogic.HORIZONTAL ? this.inlineStartThickness : this.inlineStartLength;
    }
}
