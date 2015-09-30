package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private final LayoutConfiguration config;
    private final ChildProvider childProvider = new ChildProvider();
    List<LineDefinition> lines = new ArrayList<>();

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
        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            lp.orientation = this.config.getOrientation();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), lp.height)
            );
        }

        final int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
        final int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom();
        final int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        final int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        final int controlMaxLength = this.config.getOrientation() == Common.HORIZONTAL ? sizeWidth : sizeHeight;
        final int controlMaxThickness = this.config.getOrientation() == Common.HORIZONTAL ? sizeHeight : sizeWidth;
        final int modeLength = this.config.getOrientation() == Common.HORIZONTAL ? modeWidth : modeHeight;
        final int modeThickness = this.config.getOrientation() == Common.HORIZONTAL ? modeHeight : modeWidth;

        Common.fillLines(childProvider, lines, config, controlMaxLength, modeLength != View.MeasureSpec.UNSPECIFIED);

        Common.calculateLinesAndChildPosition(lines);

        int contentLength = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            LineDefinition l = lines.get(i);
            contentLength = Math.max(contentLength, l.getLineLength());
        }

        LineDefinition currentLine = lines.get(lines.size() - 1);
        int contentThickness = currentLine.getLineStartThickness() + currentLine.getLineThickness();

        int realControlLength = Common.findSize(modeLength, controlMaxLength, contentLength);
        int realControlThickness = Common.findSize(modeHeight, controlMaxThickness, contentThickness);

        Common.applyGravityToLines(lines, realControlLength, realControlThickness, config);

        for (int i = 0; i < linesCount; i++) {
            LineDefinition line = lines.get(i);
            Common.applyGravityToLine(line, config);
            applyPositionsToViews(line);
        }

        /* need to take padding into account */
        int totalControlWidth = this.getPaddingLeft() + this.getPaddingRight();
        int totalControlHeight = this.getPaddingBottom() + this.getPaddingTop();
        if (this.config.getOrientation() == Common.HORIZONTAL) {
            totalControlWidth += contentLength;
            totalControlHeight += contentThickness;
        } else {
            totalControlWidth += contentThickness;
            totalControlHeight += contentLength;
        }
        this.setMeasuredDimension(resolveSize(totalControlWidth, widthMeasureSpec), resolveSize(totalControlHeight, heightMeasureSpec));
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
                child.measure(
                        MeasureSpec.makeMeasureSpec(lp.getLength(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(lp.getThickness(), MeasureSpec.EXACTLY)
                );
            } else {
                lp.setPosition(
                        this.getPaddingLeft() + line.getLineStartThickness() + lp.getInlineStartThickness(),
                        this.getPaddingTop() + line.getLineStartLength() + lp.getInlineStartLength());
                child.measure(
                        MeasureSpec.makeMeasureSpec(lp.getThickness(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(lp.getLength(), MeasureSpec.EXACTLY)
                );
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x + lp.leftMargin, lp.y + lp.topMargin,
                    lp.x + lp.leftMargin + child.getMeasuredWidth(), lp.y + lp.topMargin + child.getMeasuredHeight());
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
        if (!isDebugDraw()) {
            return;
        }

        Paint childPaint = this.createPaint(0xffffff00);
        Paint newLinePaint = this.createPaint(0xffff0000);

        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        if (lp.rightMargin > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y - 4.0f, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y + 4.0f, x + lp.rightMargin, y, childPaint);
        }

        if (lp.leftMargin > 0) {
            float x = child.getLeft();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y - 4.0f, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y + 4.0f, x - lp.leftMargin, y, childPaint);
        }

        if (lp.bottomMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x - 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x + 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
        }

        if (lp.topMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getTop();
            canvas.drawLine(x, y, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x - 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x + 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
        }

        if (lp.isNewLine()) {
            if (this.config.getOrientation() == Common.HORIZONTAL) {
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

    public int getOrientation() {
        return this.config.getOrientation();
    }

    public void setOrientation(int orientation) {
        this.config.setOrientation(orientation);
        this.requestLayout();
    }

    public boolean isDebugDraw() {
        return this.config.isDebugDraw() || debugDraw();
    }

    public void setDebugDraw(boolean debugDraw) {
        this.config.setDebugDraw(debugDraw);
        this.invalidate();
    }

    private boolean debugDraw() {
        try {
            // android add this method at 4.1
            Method m = ViewGroup.class.getDeclaredMethod("debugDraw", (Class[]) null);
            m.setAccessible(true);
            return (boolean) m.invoke(this, new Object[]{null});
        } catch (Exception e) {
            // if no such method (android not support this at lower api level), return false
            // ignore this, it's safe here
        }

        return false;
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
        if (this.config == null) {
            // Workaround for android sdk that wants to use virtual methods within constructor.
            return View.LAYOUT_DIRECTION_LTR;
        }

        return this.config.getLayoutDirection();
    }

    public void setLayoutDirection(int layoutDirection) {
        this.config.setLayoutDirection(layoutDirection);
        this.requestLayout();
    }

    public static class LayoutParams extends MarginLayoutParams implements Common.LayoutParams {
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

    private class ChildProvider implements Common.ChildProvider {
        @Override
        public int provideChildCount() {
            return getChildCount();
        }

        @Override
        public View provideChildAt(int i) {
            return getChildAt(i);
        }
    }
}
