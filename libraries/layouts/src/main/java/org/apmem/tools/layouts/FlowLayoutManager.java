package org.apmem.tools.layouts;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayoutManager extends RecyclerView.LayoutManager {

    private final LayoutConfiguration config;
    private final ChildProvider childProvider = new ChildProvider();
    List<LineDefinition> lines = new ArrayList<>();


    public FlowLayoutManager(LayoutConfiguration config) {
        this.config = config;
    }

    public FlowLayoutManager() {
        this.config = new LayoutConfiguration();
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return super.checkLayoutParams(lp);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return null;
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }


    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
        }
        final int sizeWidth = this.getWidth();
        final int sizeHeight = this.getHeight();
        final int controlMaxLength = this.config.getOrientation() == Common.HORIZONTAL ? sizeWidth : sizeHeight;
        final int controlMaxThickness = this.config.getOrientation() == Common.HORIZONTAL ? sizeHeight : sizeWidth;

        childProvider.setRecycler(recycler);

        Common.fillLines(childProvider, lines, config, controlMaxLength, true);

        Common.calculateLinesAndChildPosition(lines);

        int contentLength = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            LineDefinition l = lines.get(i);
            contentLength = Math.max(contentLength, l.getLineLength());
        }

        LineDefinition currentLine = lines.get(lines.size() - 1);
        int contentThickness = currentLine.getLineStartThickness() + currentLine.getLineThickness();

        int realControlLength = Common.findSize(ViewGroup.LayoutParams.WRAP_CONTENT, controlMaxLength, contentLength);
        int realControlThickness = Common.findSize(ViewGroup.LayoutParams.WRAP_CONTENT, controlMaxThickness, contentThickness);

        Common.applyGravityToLines(lines, realControlLength, realControlThickness, config);

        for (int i = 0; i < linesCount; i++) {
            LineDefinition line = lines.get(i);
            Common.applyGravityToLine(line, config);
            applyPositionsToViews(line);
        }
    }

    private void applyPositionsToViews(LineDefinition line) {
        final List<View> childViews = line.getViews();
        final int childCount = childViews.size();
        for (int i = 0; i < childCount; i++) {
            final View child = childViews.get(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (this.config.getOrientation() == Common.HORIZONTAL) {
                lp.setPosition(
                        this.getPaddingLeft() + line.getLineStartLength() + lp.getInlineStartLength(),
                        this.getPaddingTop() + line.getLineStartThickness() + lp.getInlineStartThickness());

                measureChildWithMargins(child, lp.getLength(), lp.getThickness());
            } else {
                lp.setPosition(
                        this.getPaddingLeft() + line.getLineStartThickness() + lp.getInlineStartThickness(),
                        this.getPaddingTop() + line.getLineStartLength() + lp.getInlineStartLength());

                measureChildWithMargins(child, lp.getThickness(), lp.getLength());
            }

            layoutDecorated(child, lp.x + lp.leftMargin, lp.y + lp.topMargin,
                    lp.x + lp.leftMargin + child.getMeasuredWidth(), lp.y + lp.topMargin + child.getMeasuredHeight());
        }
    }

    public static class LayoutParams extends RecyclerView.LayoutParams implements Common.LayoutParams {
        @ViewDebug.ExportedProperty(mapping = {
                @ViewDebug.IntToString(from = Gravity.NO_GRAVITY, to = "NONE"),
                @ViewDebug.IntToString(from = Gravity.TOP, to = "TOP"),
                @ViewDebug.IntToString(from = Gravity.BOTTOM, to = "BOTTOM"),
                @ViewDebug.IntToString(from = Gravity.LEFT, to = "LEFT"),
                @ViewDebug.IntToString(from = Gravity.RIGHT, to = "RIGHT"),
                @ViewDebug.IntToString(from = Gravity.CENTER_VERTICAL, to = "CENTER_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_VERTICAL, to = "FILL_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER_HORIZONTAL, to = "CENTER_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_HORIZONTAL, to = "FILL_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER, to = "CENTER"),
                @ViewDebug.IntToString(from = Gravity.FILL, to = "FILL")
        })

        private boolean newLine = false;
        private int gravity = Gravity.NO_GRAVITY;
        private float weight = -1.0f;
        private int inlineStartLength;
        private int length;
        private int thickness;
        private int inlineStartThickness;
        private int x;
        private int y;
        private int orientation;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.readStyleParameters(context, attributeSet);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public boolean gravitySpecified() {
            return this.gravity != Gravity.NO_GRAVITY;
        }

        public boolean weightSpecified() {
            return this.weight >= 0;
        }

        private void readStyleParameters(Context context, AttributeSet attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout_LayoutParams);
            try {
                this.newLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_newLine, false);
                this.gravity = a.getInt(R.styleable.FlowLayout_LayoutParams_android_layout_gravity, Gravity.NO_GRAVITY);
                this.weight = a.getFloat(R.styleable.FlowLayout_LayoutParams_layout_weight, -1.0f);
            } finally {
                a.recycle();
            }
        }
        
        void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
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

        int getInlineStartThickness() {
            return inlineStartThickness;
        }

        public void setInlineStartThickness(int inlineStartThickness) {
            this.inlineStartThickness = inlineStartThickness;
        }

        public int getSpacingLength() {
            if (orientation == Common.HORIZONTAL) {
                return this.leftMargin + this.rightMargin;
            } else {
                return this.topMargin + this.bottomMargin;
            }
        }

        public int getSpacingThickness() {
            if (orientation == Common.HORIZONTAL) {
                return this.topMargin + this.bottomMargin;
            } else {
                return this.leftMargin + this.rightMargin;
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            this.weight = weight;
        }

        public boolean isNewLine() {
            return newLine;
        }

        public void setNewLine(boolean newLine) {
            this.newLine = newLine;
        }
    }

    class ChildProvider implements Common.ChildProvider {

        private RecyclerView.Recycler recycler;

        public void setRecycler(RecyclerView.Recycler recycler){

            this.recycler = recycler;
        }

        @Override
        public int provideChildCount() {
            return getItemCount();
        }

        @Override
        public View provideChildAt(int i) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            return child;
        }
    }
}