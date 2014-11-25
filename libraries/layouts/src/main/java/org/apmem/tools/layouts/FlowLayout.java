package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private static final int FILL_LINES_NONE = 0;
    private static final int FILL_LINES_EXCEPT_LAST = 1;
    private static final int FILL_LINES_ALL = 2;

    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private int orientation = 0;
    private boolean debugDraw = false;
    private float weightSum;
    private float weightDefault;
    private int gravity = Gravity.LEFT | Gravity.TOP;
    private FillLines fillLines = FillLines.NONE;

    public FlowLayout(Context context) {
        super(context);

        this.readStyleParameters(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.readStyleParameters(context, attributeSet);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);

        this.readStyleParameters(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom();

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int size;
        int mode;

        if (this.orientation == HORIZONTAL) {
            size = sizeWidth;
            mode = modeWidth;
        } else {
            size = sizeHeight;
            mode = modeHeight;
        }

        List<LineDefinition> lines = new ArrayList<LineDefinition>();
        LineDefinition line = new LineDefinition(0);
        lines.add(line);

        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), lp.height)
            );

            final int hSpacing = this.getHorizontalSpacing(lp);
            final int vSpacing = this.getVerticalSpacing(lp);

            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            int childLength;
            int childThickness;
            int spacingLength;
            int spacingThickness;

            if (this.orientation == HORIZONTAL) {
                childLength = childWidth;
                childThickness = childHeight;
                spacingLength = hSpacing;
                spacingThickness = vSpacing;
            } else {
                childLength = childHeight;
                childThickness = childWidth;
                spacingLength = vSpacing;
                spacingThickness = hSpacing;
            }

            boolean newLine = lp.newLine || (mode != MeasureSpec.UNSPECIFIED && line.lineLengthWithSpacing + childLength > size);
            if (newLine) {
                line = new LineDefinition(line.linePosition + line.lineThicknessWithSpacing);
                lines.add(line);
            }

            int posX;
            int posY;
            if (this.orientation == HORIZONTAL) {
                posX = this.getPaddingLeft() + line.lineLengthWithSpacing;
                posY = this.getPaddingTop() + line.linePosition;
            } else {
                posX = this.getPaddingLeft() + line.linePosition;
                posY = this.getPaddingTop() + line.lineLengthWithSpacing;
            }
            lp.setPosition(posX, posY);

            line.addView(child);
            line.addLength(childLength, spacingLength);
            line.addThickness(childThickness, spacingThickness);
        }

        if (this.fillLines != FillLines.NONE) {
            int linesSize = lines.size();
            for (int i = 0; i < linesSize; i++) {
                if (i == linesSize - 1 && this.fillLines == FillLines.EXCEPT_LAST) {
                    continue;
                }

                this.fillLine(size, lines.get(i));
            }
        }

        int controlMaxLength = 0;
        for (LineDefinition l : lines) {
            controlMaxLength = Math.max(controlMaxLength, l.lineLength);
        }
        int controlMaxThickness = line.linePosition + line.lineThickness;

        /* need to take padding into account */
        if (this.orientation == HORIZONTAL) {
            controlMaxLength += this.getPaddingLeft() + this.getPaddingRight();
            controlMaxThickness += this.getPaddingBottom() + this.getPaddingTop();
        } else {
            controlMaxLength += this.getPaddingBottom() + this.getPaddingTop();
            controlMaxThickness += this.getPaddingLeft() + this.getPaddingRight();
        }

        if (this.orientation == HORIZONTAL) {
            this.setMeasuredDimension(resolveSize(controlMaxLength, widthMeasureSpec), resolveSize(controlMaxThickness, heightMeasureSpec));
        } else {
            this.setMeasuredDimension(resolveSize(controlMaxThickness, widthMeasureSpec), resolveSize(controlMaxLength, heightMeasureSpec));
        }
    }

    private void fillLine(int size, LineDefinition line) {
        int lineCount = line.views.size();
        float totalWeight = 0;
        if (lineCount <= 0) {
            return;
        }

        if (this.weightSum > 0) {
            totalWeight = this.weightSum;
        } else {
            for (View prev : line.views) {
                LayoutParams plp = (LayoutParams) prev.getLayoutParams();
                float weight = this.getWeight(plp);
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0) {
            return;
        }

        int excessLength = size - line.lineLength;
        int excessOffset = 0;
        for (View child : line.views) {
            LayoutParams plp = (LayoutParams) child.getLayoutParams();

            float weight = this.getWeight(plp);
            int gravity = this.getGravity(plp);
            int extraLength = Math.round(excessLength * weight / totalWeight);

            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            Rect container = new Rect();
            if (this.orientation == HORIZONTAL) {
                container.left = excessOffset;
                container.right = childWidth + extraLength + excessOffset;
                container.bottom = line.lineThickness;
            } else {
                container.top = excessOffset;
                container.right = line.lineThickness;
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

    private int getVerticalSpacing(LayoutParams lp) {
        return lp.verticalSpacingSpecified() ? lp.verticalSpacing : this.verticalSpacing;
    }

    private int getHorizontalSpacing(LayoutParams lp) {
        return lp.horizontalSpacingSpecified() ? lp.horizontalSpacing : this.horizontalSpacing;
    }

    private int getGravity(LayoutParams lp) {
        return lp.gravitySpecified() ? lp.gravity : this.gravity;
    }

    private float getWeight(LayoutParams lp) {
        return lp.weightSpecified() ? lp.weight : this.weightDefault;
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

    private void readStyleParameters(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        try {
            this.horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, 0);
            this.verticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 0);
            this.orientation = a.getInteger(R.styleable.FlowLayout_orientation, HORIZONTAL);
            this.debugDraw = a.getBoolean(R.styleable.FlowLayout_debugDraw, false);
            this.weightSum = a.getFloat(R.styleable.FlowLayout_weightSum, 0.0f);
            this.weightDefault = a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f);
            int gravityIndex = a.getInt(R.styleable.FlowLayout_android_gravity, -1);
            if (gravityIndex >= 0) {
                this.setGravity(gravityIndex);
            }
            int fillLinesIndex = a.getInt(R.styleable.FlowLayout_fillLines, -1);
            if (fillLinesIndex >= 0) {
                this.setFillLines(FillLines.from(fillLinesIndex));
            }
        } finally {
            a.recycle();
        }
    }

    private void drawDebugInfo(Canvas canvas, View child) {
        if (!this.debugDraw) {
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
        } else if (this.horizontalSpacing > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x + this.horizontalSpacing, y, layoutPaint);
            canvas.drawLine(x + this.horizontalSpacing - 4.0f, y - 4.0f, x + this.horizontalSpacing, y, layoutPaint);
            canvas.drawLine(x + this.horizontalSpacing - 4.0f, y + 4.0f, x + this.horizontalSpacing, y, layoutPaint);
        }

        if (lp.verticalSpacing > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + lp.verticalSpacing, childPaint);
            canvas.drawLine(x - 4.0f, y + lp.verticalSpacing - 4.0f, x, y + lp.verticalSpacing, childPaint);
            canvas.drawLine(x + 4.0f, y + lp.verticalSpacing - 4.0f, x, y + lp.verticalSpacing, childPaint);
        } else if (this.verticalSpacing > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + this.verticalSpacing, layoutPaint);
            canvas.drawLine(x - 4.0f, y + this.verticalSpacing - 4.0f, x, y + this.verticalSpacing, layoutPaint);
            canvas.drawLine(x + 4.0f, y + this.verticalSpacing - 4.0f, x, y + this.verticalSpacing, layoutPaint);
        }

        if (lp.newLine) {
            if (this.orientation == HORIZONTAL) {
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
        return this.horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
        this.requestLayout();
    }

    public int getVerticalSpacing() {
        return this.verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
        this.requestLayout();
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        this.requestLayout();
    }

    public boolean isDebugDraw() {
        return this.debugDraw;
    }

    public void setDebugDraw(boolean debugDraw) {
        this.debugDraw = debugDraw;
        this.invalidate();
    }

    public float getWeightSum() {
        return this.weightSum;
    }

    public void setWeightSum(float weightSum) {
        this.weightSum = Math.max(0, weightSum);
        this.requestLayout();
    }

    public float getWeightDefault() {
        return this.weightDefault;
    }

    public void setWeightDefault(float weightDefault) {
        this.weightDefault = Math.max(0, weightDefault);
        this.requestLayout();
    }

    public int getGravity() {
        return this.gravity;
    }

    public void setGravity(int gravity) {
        if (this.gravity == gravity) {
            return;
        }

        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.LEFT;
        }

        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.TOP;
        }

        this.gravity = gravity;
        this.requestLayout();
    }

    public FillLines getFillLines() {
        return this.fillLines;
    }

    public void setFillLines(FillLines fillLines) {
        this.fillLines = fillLines != null ? fillLines : FillLines.NONE;
        this.requestLayout();
    }

    public enum FillLines {
        NONE(FILL_LINES_NONE),
        EXCEPT_LAST(FILL_LINES_EXCEPT_LAST),
        ALL(FILL_LINES_ALL);

        private final int intValue;

        FillLines(int intValue) {
            this.intValue = intValue;
        }

        public static FillLines from(int intValue) {
            if (intValue == EXCEPT_LAST.getIntValue()) {
                return EXCEPT_LAST;
            } else if (intValue == ALL.getIntValue()) {
                return ALL;
            } else {
                return NONE;
            }
        }

        public int getIntValue() {
            return this.intValue;
        }
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

    private class LineDefinition {
        private final List<View> views = new ArrayList<View>();
        private int lineLength;
        private int lineThickness;
        private int lineLengthWithSpacing;
        private int lineThicknessWithSpacing;
        private int linePosition;

        public LineDefinition(int linePosition) {
            this.linePosition = linePosition;
        }

        public void addLength(int childLength, int spacingLength) {
            this.lineLength = this.lineLengthWithSpacing + childLength;
            this.lineLengthWithSpacing = this.lineLength + spacingLength;
        }

        public void addThickness(int childThickness, int spacingThickness) {
            this.lineThicknessWithSpacing = Math.max(this.lineThicknessWithSpacing, childThickness + spacingThickness);
            this.lineThickness = Math.max(this.lineThickness, childThickness);
        }

        public void addView(View child) {
            this.views.add(child);
        }
    }
}
