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
    public static final int LAYOUT_DIRECTION_LTR = 0;
    public static final int LAYOUT_DIRECTION_RTL = 1;

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
        final int controlMaxLength = this.config.getOrientation() == HORIZONTAL ? sizeWidth : sizeHeight;
        final int controlMaxThickness = this.config.getOrientation() == HORIZONTAL ? sizeHeight : sizeWidth;
        final int modeLength = this.config.getOrientation() == HORIZONTAL ? modeWidth : modeHeight;
        final int modeThickness = this.config.getOrientation() == HORIZONTAL ? modeHeight : modeWidth;

        lines.clear();
        LineDefinition currentLine = new LineDefinition(controlMaxLength, config);
        lines.add(currentLine);

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

            boolean newLine = lp.newLine || (modeLength != MeasureSpec.UNSPECIFIED && !currentLine.canFit(child));
            if (newLine) {
                currentLine = new LineDefinition(controlMaxLength, config);
                if (this.config.getOrientation() == VERTICAL && this.config.getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                    lines.add(0, currentLine);
                } else {
                    lines.add(currentLine);
                }
            }

            if (this.config.getOrientation() == HORIZONTAL && this.config.getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                currentLine.addView(0, child);
            } else {
                currentLine.addView(child);
            }
        }

        this.calculateLinesAndChildsPosition(lines);

        int contentLength = 0;
        for (LineDefinition l : lines) {
            contentLength = Math.max(contentLength, l.getLineLength());
        }
        int contentThickness = currentLine.getLineStartThickness() + currentLine.getLineThickness();

        int realControlLength;
        int realControlThickness;
        switch (modeLength) {
            case MeasureSpec.UNSPECIFIED:
                realControlLength = contentLength;
                realControlThickness = contentThickness;
                break;
            case MeasureSpec.AT_MOST:
                realControlLength = Math.min(contentLength, controlMaxLength);
                realControlThickness = Math.min(contentThickness, controlMaxThickness);
                break;
            case MeasureSpec.EXACTLY:
                realControlLength = controlMaxLength;
// With this line control will be exactly this size (an all child elements will be narrower in case of lack of thickness)
//                realControlThickness = controlMaxThickness;
                realControlThickness = Math.max(contentThickness, controlMaxThickness);
                break;
            default:
                realControlLength = contentLength;
                realControlThickness = contentThickness;
                break;
        }

        this.applyGravityToLines(lines, realControlLength, realControlThickness);

        for (LineDefinition line : lines) {
            this.applyGravityToLine(line);
            this.applyPositionsToViews(line);
        }

        /* need to take padding into account */
        int totalControlWidth = this.getPaddingLeft() + this.getPaddingRight();
        int totalControlHeight = this.getPaddingBottom() + this.getPaddingTop();
        if (this.config.getOrientation() == HORIZONTAL) {
            totalControlWidth += contentLength;
            totalControlHeight += contentThickness;
        } else {
            totalControlWidth += contentThickness;
            totalControlHeight += contentLength;
        }
        this.setMeasuredDimension(resolveSize(totalControlWidth, widthMeasureSpec), resolveSize(totalControlHeight, heightMeasureSpec));
    }

    private void calculateLinesAndChildsPosition(List<LineDefinition> lines) {
        int prevLinesThickness = 0;
        for (LineDefinition line : lines) {
            line.addLineStartThickness(prevLinesThickness);
            prevLinesThickness += line.getLineThickness();
            int prevChildThickness = 0;
            for (ViewContainer child : line.getViews()) {
                child.setInlineStartLength(prevChildThickness);
                prevChildThickness += child.getLength() + child.getSpacingLength();
            }
        }
    }

    private void applyPositionsToViews(LineDefinition line) {
        for (ViewContainer child : line.getViews()) {
            if (this.config.getOrientation() == HORIZONTAL) {
                ((LayoutParams) child.getView().getLayoutParams()).setPosition(
                        this.getPaddingLeft() + line.getLineStartLength() + child.getInlineStartLength(),
                        this.getPaddingTop() + line.getLineStartThickness() + child.getInlineStartThickness());
                child.getView().measure(
                        MeasureSpec.makeMeasureSpec(child.getLength(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(child.getThickness(), MeasureSpec.EXACTLY)
                );
            } else {
                ((LayoutParams) child.getView().getLayoutParams()).setPosition(
                        this.getPaddingLeft() + line.getLineStartThickness() + child.getInlineStartThickness(),
                        this.getPaddingTop() + line.getLineStartLength() + child.getInlineStartLength());
                child.getView().measure(
                        MeasureSpec.makeMeasureSpec(child.getThickness(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(child.getLength(), MeasureSpec.EXACTLY)
                );
            }
        }
    }

    private void applyGravityToLines(List<LineDefinition> lines, int realControlLength, int realControlThickness) {
        final int linesCount = lines.size();
        if (linesCount <= 0) {
            return;
        }

        final int totalWeight = linesCount;
        LineDefinition lastLine = lines.get(linesCount - 1);
        int excessThickness = realControlThickness - (lastLine.getLineThickness() + lastLine.getLineStartThickness());
        int excessOffset = 0;
        for (LineDefinition child : lines) {
            int weight = 1;
            int gravity = this.getGravity();
            int extraThickness = Math.round(excessThickness * weight / totalWeight);

            final int childLength = child.getLineLength();
            final int childThickness = child.getLineThickness();

            Rect container = new Rect();
            container.top = excessOffset;
            container.left = 0;
            container.right = realControlLength;
            container.bottom = childThickness + extraThickness + excessOffset;

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraThickness;
            child.addLineStartLength(result.left);
            child.addLineStartThickness(result.top);
            child.setLength(result.width());
            child.setThickness(result.height());
        }
    }

    private void applyGravityToLine(LineDefinition line) {
        final int viewCount = line.getViews().size();
        if (viewCount <= 0) {
            return;
        }

        float totalWeight = 0;
        for (ViewContainer prev : line.getViews()) {
            LayoutParams plp = (LayoutParams) prev.getView().getLayoutParams();
            totalWeight += this.getWeight(plp);
        }

        ViewContainer lastChild = line.getViews().get(viewCount - 1);
        int excessLength = line.getLineLength() - (lastChild.getLength() + lastChild.getInlineStartLength());
        int excessOffset = 0;
        for (ViewContainer child : line.getViews()) {
            LayoutParams plp = (LayoutParams) child.getView().getLayoutParams();

            float weight = this.getWeight(plp);
            int gravity = this.getGravity(plp);
            int extraLength = Math.round(excessLength * weight / totalWeight);

            final int childLength = child.getLength() + child.getSpacingLength();
            final int childThickness = child.getThickness() + child.getSpacingThickness();

            Rect container = new Rect();
            container.top = 0;
            container.left = excessOffset;
            container.right = childLength + extraLength + excessOffset;
            container.bottom = line.getLineThickness();

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraLength;
            child.addInlinePosition(result.left);
            child.addInlineStartThickness(result.top);
            child.setLength(result.width() - child.getSpacingLength());
            child.setThickness(result.height() - child.getSpacingThickness());
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

    public int getLayoutDirection() {
        return this.config.getLayoutDirection();
    }

    public void setLayoutDirection(int layoutDirection) {
        this.config.setLayoutDirection(layoutDirection);
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
