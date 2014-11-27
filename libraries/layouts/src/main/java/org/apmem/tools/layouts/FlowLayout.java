package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int FILL_LINES_NONE = 0;
    public static final int FILL_LINES_EXCEPT_LAST = 1;
    public static final int FILL_LINES_ALL = 2;

    private final LayoutConfiguration config;
    List<LineDefinition> lines = new ArrayList<LineDefinition>();

    public FlowLayout(Context context) {
        super(context);
        this.config = new LayoutConfiguration(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.config = new LayoutConfiguration(context, attributeSet);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.config = new LayoutConfiguration(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
        final int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom();
        final int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        final int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        final int size = this.config.getOrientation() == HORIZONTAL ? sizeWidth : sizeHeight;
        final int mode = this.config.getOrientation() == HORIZONTAL ? modeWidth : modeHeight;

        lines.clear();
        LineDefinition line = new LineDefinition(0, size, config);
        lines.add(line);

        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), lp.height)
            );

            boolean newLine = lp.newLine || (mode != MeasureSpec.UNSPECIFIED && !line.canFit(child));
            if (newLine) {
                line = new LineDefinition(line.getLinePosition() + line.getLineThicknessWithSpacing(), size, config);
                lines.add(line);
            }

            int posX = this.config.getOrientation() == HORIZONTAL ? this.getPaddingLeft() + line.getLineLengthWithSpacing() : this.getPaddingLeft() + line.getLinePosition();
            int posY = this.config.getOrientation() == HORIZONTAL ? this.getPaddingTop() + line.getLinePosition() : this.getPaddingTop() + line.getLineLengthWithSpacing();
            lp.setPosition(posX, posY);

            line.addView(child);
        }

        if (this.config.getFillLines() != FILL_LINES_NONE) {
            int linesSize = lines.size();
            for (int i = 0; i < linesSize; i++) {
                if (i == linesSize - 1 && this.config.getFillLines() == FILL_LINES_EXCEPT_LAST) {
                    continue;
                }

                this.fillLine(size, lines.get(i));
            }
        }

        int controlMaxLength = 0;
        for (LineDefinition l : lines) {
            controlMaxLength = Math.max(controlMaxLength, l.getLineLength());
        }
        int controlMaxThickness = line.getLinePosition() + line.getLineThickness();

        /* need to take padding into account */
        if (this.config.getOrientation() == HORIZONTAL) {
            controlMaxLength += this.getPaddingLeft() + this.getPaddingRight();
            controlMaxThickness += this.getPaddingBottom() + this.getPaddingTop();
        } else {
            controlMaxLength += this.getPaddingBottom() + this.getPaddingTop();
            controlMaxThickness += this.getPaddingLeft() + this.getPaddingRight();
        }

        if (this.config.getOrientation() == HORIZONTAL) {
            this.setMeasuredDimension(resolveSize(controlMaxLength, widthMeasureSpec), resolveSize(controlMaxThickness, heightMeasureSpec));
        } else {
            this.setMeasuredDimension(resolveSize(controlMaxThickness, widthMeasureSpec), resolveSize(controlMaxLength, heightMeasureSpec));
        }
    }

    private void fillLine(int size, LineDefinition line) {
        int lineCount = line.getViews().size();
        float totalWeight = 0;
        if (lineCount <= 0) {
            return;
        }

        if (this.config.getWeightSum() > 0) {
            totalWeight = this.config.getWeightSum();
        } else {
            for (View prev : line.getViews()) {
                LayoutParams plp = (LayoutParams) prev.getLayoutParams();
                float weight = this.getWeight(plp);
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0) {
            return;
        }

        int excessLength = size - line.getLineLength();
        int excessOffset = 0;
        for (View child : line.getViews()) {
            LayoutParams plp = (LayoutParams) child.getLayoutParams();

            float weight = this.getWeight(plp);
            int gravity = this.getGravity(plp);
            int extraLength = Math.round(excessLength * weight / totalWeight);

            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            Rect container = new Rect();
            if (this.config.getOrientation() == HORIZONTAL) {
                container.left = excessOffset;
                container.right = childWidth + extraLength + excessOffset;
                container.bottom = line.getLineThickness();
            } else {
                container.top = excessOffset;
                container.right = line.getLineThickness();
                container.bottom = childHeight + extraLength + excessOffset;
            }

            Rect result = new Rect();
            Gravity.apply(gravity, childWidth, childHeight, container, result);

            plp.setPosition(plp.x + result.left, plp.y + result.top);
            excessOffset += extraLength;
            child.measure(
                    MeasureSpec.makeMeasureSpec(result.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(result.height(), MeasureSpec.EXACTLY)
            );
        }
    }

    private int getGravity(LayoutParams lp) {
        return lp.gravitySpecified() ? lp.gravity : this.config.getGravity();
    }

    private float getWeight(LayoutParams lp) {
        return lp.weightSpecified() ? lp.weight : this.config.getWeightDefault();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        this.drawDebugInfo(canvas, child);
        return more;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(this.getContext(), attributeSet);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    private void drawDebugInfo(Canvas canvas, View child) {
        if (!this.config.isDebugDraw()) {
            return;
        }

        Paint childPaint = this.createPaint(0xffffff00);
        Paint layoutPaint = this.createPaint(0xff00ff00);
        Paint newLinePaint = this.createPaint(0xffff0000);

        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        if (lp.horizontalSpacing > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x + lp.horizontalSpacing, y, childPaint);
            canvas.drawLine(x + lp.horizontalSpacing - 4.0f, y - 4.0f, x + lp.horizontalSpacing, y, childPaint);
            canvas.drawLine(x + lp.horizontalSpacing - 4.0f, y + 4.0f, x + lp.horizontalSpacing, y, childPaint);
        } else if (this.config.getHorizontalSpacing() > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x + this.config.getHorizontalSpacing(), y, layoutPaint);
            canvas.drawLine(x + this.config.getHorizontalSpacing() - 4.0f, y - 4.0f, x + this.config.getHorizontalSpacing(), y, layoutPaint);
            canvas.drawLine(x + this.config.getHorizontalSpacing() - 4.0f, y + 4.0f, x + this.config.getHorizontalSpacing(), y, layoutPaint);
        }

        if (lp.verticalSpacing > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + lp.verticalSpacing, childPaint);
            canvas.drawLine(x - 4.0f, y + lp.verticalSpacing - 4.0f, x, y + lp.verticalSpacing, childPaint);
            canvas.drawLine(x + 4.0f, y + lp.verticalSpacing - 4.0f, x, y + lp.verticalSpacing, childPaint);
        } else if (this.config.getVerticalSpacing() > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + this.config.getVerticalSpacing(), layoutPaint);
            canvas.drawLine(x - 4.0f, y + this.config.getVerticalSpacing() - 4.0f, x, y + this.config.getVerticalSpacing(), layoutPaint);
            canvas.drawLine(x + 4.0f, y + this.config.getVerticalSpacing() - 4.0f, x, y + this.config.getVerticalSpacing(), layoutPaint);
        }

        if (lp.newLine) {
            if (this.config.getOrientation() == HORIZONTAL) {
                float x = child.getLeft();
                float y = child.getTop() + child.getHeight() / 2.0f;
                canvas.drawLine(x, y - 6.0f, x, y + 6.0f, newLinePaint);
            } else {
                float x = child.getLeft() + child.getWidth() / 2.0f;
                float y = child.getTop();
                canvas.drawLine(x - 6.0f, y, x + 6.0f, y, newLinePaint);
            }
        }
    }

    private Paint createPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(2.0f);
        return paint;
    }

    public int getHorizontalSpacing() {
        return this.config.getHorizontalSpacing();
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.config.setHorizontalSpacing(horizontalSpacing);
        this.requestLayout();
    }

    public int getVerticalSpacing() {
        return this.config.getVerticalSpacing();
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.config.setVerticalSpacing(verticalSpacing);
        this.requestLayout();
    }

    public int getOrientation() {
        return this.config.getOrientation();
    }

    public void setOrientation(int orientation) {
        this.config.setOrientation(orientation);
        this.requestLayout();
    }

    public boolean isDebugDraw() {
        return this.config.isDebugDraw();
    }

    public void setDebugDraw(boolean debugDraw) {
        this.config.setDebugDraw(debugDraw);
        this.invalidate();
    }

    public float getWeightSum() {
        return this.config.getWeightSum();
    }

    public void setWeightSum(float weightSum) {
        this.config.setWeightSum(weightSum);
        this.requestLayout();
    }

    public float getWeightDefault() {
        return this.config.getWeightDefault();
    }

    public void setWeightDefault(float weightDefault) {
        this.config.setWeightDefault(weightDefault);
        this.requestLayout();
    }

    public int getGravity() {
        return this.config.getGravity();
    }

    public void setGravity(int gravity) {
        this.config.setGravity(gravity);
        this.requestLayout();
    }

    public int getFillLines() {
        return this.config.getFillLines();
    }

    public void setFillLines(int fillLines) {
        this.config.setFillLines(fillLines);
        this.requestLayout();
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        private static final int NO_SPACING = -1;
        @android.view.ViewDebug.ExportedProperty(category = "layout")
        public int x;
        @android.view.ViewDebug.ExportedProperty(category = "layout")
        public int y;
        @android.view.ViewDebug.ExportedProperty(category = "layout", mapping = {@android.view.ViewDebug.IntToString(from = NO_SPACING, to = "NO_SPACING")})
        public int horizontalSpacing = NO_SPACING;
        @android.view.ViewDebug.ExportedProperty(category = "layout", mapping = {@android.view.ViewDebug.IntToString(from = NO_SPACING, to = "NO_SPACING")})
        public int verticalSpacing = NO_SPACING;
        public boolean newLine = false;
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
        public int gravity = Gravity.NO_GRAVITY;
        public float weight = -1.0f;

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

        public boolean horizontalSpacingSpecified() {
            return this.horizontalSpacing != NO_SPACING;
        }

        public boolean verticalSpacingSpecified() {
            return this.verticalSpacing != NO_SPACING;
        }

        public boolean gravitySpecified() {
            return this.gravity != Gravity.NO_GRAVITY;
        }

        public boolean weightSpecified() {
            return this.weight >= 0;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private void readStyleParameters(Context context, AttributeSet attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout_LayoutParams);
            try {
                this.horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_horizontalSpacing, NO_SPACING);
                this.verticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_verticalSpacing, NO_SPACING);
                this.newLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_newLine, false);
                this.gravity = a.getInt(R.styleable.FlowLayout_LayoutParams_android_layout_gravity, Gravity.NO_GRAVITY);
                this.weight = a.getFloat(R.styleable.FlowLayout_LayoutParams_layout_weight, -1.0f);
            } finally {
                a.recycle();
            }
        }
    }
}
