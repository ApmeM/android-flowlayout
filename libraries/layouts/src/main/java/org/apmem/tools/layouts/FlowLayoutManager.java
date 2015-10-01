package org.apmem.tools.layouts;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import org.apmem.tools.layouts.logic.CommonLogic;
import org.apmem.tools.layouts.logic.ConfigDefinition;
import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;

import java.util.ArrayList;
import java.util.List;

public class FlowLayoutManager extends RecyclerView.LayoutManager {

    private final ConfigDefinition config;
    List<LineDefinition> lines = new ArrayList<>();
    List<ViewDefinition> views = new ArrayList<>();

    public FlowLayoutManager(ConfigDefinition config) {
        this.config = config;
    }

    public FlowLayoutManager() {
        this.config = new ConfigDefinition();
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
        detachAndScrapAttachedViews(recycler);

        final int count = this.getItemCount();
        views.clear();
        lines.clear();
        for (int i = 0; i < count; i++) {
            View child = recycler.getViewForPosition(i);
            attachView(child);
            measureChildWithMargins(child, 0, 0);

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            ViewDefinition view = new ViewDefinition(this.config, child);
            view.setWidth(child.getMeasuredWidth());
            view.setHeight(child.getMeasuredHeight());
            view.setNewLine(lp.isNewLine());
            view.setGravity(lp.getGravity());
            view.setWeight(lp.getWeight());
            view.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            views.add(view);
        }

        this.config.setMaxWidth(this.getWidth() - this.getPaddingRight() - this.getPaddingLeft());
        this.config.setMaxHeight(this.getHeight() - this.getPaddingTop() - this.getPaddingBottom());
        this.config.setWidthMode(View.MeasureSpec.EXACTLY);
        this.config.setHeightMode(View.MeasureSpec.EXACTLY);
        this.config.setCheckCanFit(true);

        CommonLogic.fillLines(views, lines, config);
        CommonLogic.calculateLinesAndChildPosition(lines);

        int contentLength = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            LineDefinition l = lines.get(i);
            contentLength = Math.max(contentLength, l.getLineLength());
        }

        LineDefinition currentLine = lines.get(lines.size() - 1);
        int contentThickness = currentLine.getLineStartThickness() + currentLine.getLineThickness();
        int realControlLength = CommonLogic.findSize(this.config.getLengthMode(), this.config.getMaxLength(), contentLength);
        int realControlThickness = CommonLogic.findSize(this.config.getThicknessMode(), this.config.getMaxThickness(), contentThickness);

        CommonLogic.applyGravityToLines(lines, realControlLength, realControlThickness, config);

        for (int i = 0; i < linesCount; i++) {
            LineDefinition line = lines.get(i);
            applyPositionsToViews(line);
        }
    }

    private void applyPositionsToViews(LineDefinition line) {
        final List<ViewDefinition> childViews = line.getViews();
        final int childCount = childViews.size();
        for (int i = 0; i < childCount; i++) {
            final ViewDefinition child = childViews.get(i);
            final View view = child.getView();
            measureChildWithMargins(view, child.getWidth(), child.getHeight());

            layoutDecorated(view,
                    this.getPaddingLeft() + line.getLineStartLength() + child.getInlineStartLength(),
                    this.getPaddingTop() + line.getLineStartThickness() + child.getInlineStartThickness(),
                    this.getPaddingLeft() + line.getLineStartLength() + child.getInlineStartLength() + child.getWidth(),
                    this.getPaddingTop() + line.getLineStartThickness() + child.getInlineStartThickness() + child.getHeight()
            );
        }
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {
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

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.readStyleParameters(context, attributeSet);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
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
}