package org.apmem.tools.layouts;

import android.view.View;

public class ViewContainer {
    private final View view;
    private final int spacingLength;
    private final int spacingThickness;
    private int inlineStartLength;
    private int length;
    private int thickness;
    private int inlineStartThickness;

    public ViewContainer(View view, int spacingLength, int spacingThickness) {
        this.view = view;
        this.spacingLength = spacingLength;
        this.spacingThickness = spacingThickness;
        this.inlineStartThickness = 0;
    }

    public View getView() {
        return view;
    }

    public int getInlineStartLength() {
        return inlineStartLength;
    }

    public void setInlineStartLength(int inlineStartLength) {
        this.inlineStartLength = inlineStartLength;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public int getInlineStartThickness() {
        return inlineStartThickness;
    }

    public int getSpacingLength() {
        return spacingLength;
    }

    public int getSpacingThickness() {
        return spacingThickness;
    }

    public void addInlinePosition(int extraInlinePosition) {
        this.inlineStartLength += extraInlinePosition;
    }

    public void addInlineStartThickness(int extraInlineStartThickness) {
        this.inlineStartThickness += extraInlineStartThickness;
    }
}
