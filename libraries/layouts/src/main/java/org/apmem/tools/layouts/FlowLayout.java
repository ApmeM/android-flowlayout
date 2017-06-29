package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;

import org.apmem.tools.layouts.logic.CommonLogic;
import org.apmem.tools.layouts.logic.ConfigDefinition;
import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private final ConfigDefinition config;
    List<LineDefinition> lines = new ArrayList<>();
    List<ViewDefinition> views = new ArrayList<>();

    public FlowLayout(Context context) {
        super(context);
        this.config = new ConfigDefinition();
        readStyleParameters(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.config = new ConfigDefinition();
        readStyleParameters(context, attributeSet);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.config = new ConfigDefinition();
        readStyleParameters(context, attributeSet);
    }

    private void readStyleParameters(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        try {
            this.config.setOrientation(a.getInteger(R.styleable.FlowLayout_android_orientation, CommonLogic.HORIZONTAL));
            this.config.setMaxLines(a.getInteger(R.styleable.FlowLayout_maxLines, 0));
            this.config.setDebugDraw(a.getBoolean(R.styleable.FlowLayout_debugDraw, false));
            this.config.setWeightDefault(a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f));
            this.config.setGravity(a.getInteger(R.styleable.FlowLayout_android_gravity, Gravity.NO_GRAVITY));

            int layoutDirection;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutDirection = a.getInteger(R.styleable.FlowLayout_layoutDirection, LAYOUT_DIRECTION_LTR);
            } else {
                layoutDirection = a.getInteger(R.styleable.FlowLayout_layoutDirection, super.getLayoutDirection());
            }
            //noinspection ResourceType
            this.setLayoutDirection(layoutDirection);

        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int count = this.getChildCount();
        views.clear();
        lines.clear();
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

            ViewDefinition view = new ViewDefinition(this.config, child);
            view.setWidth(child.getMeasuredWidth());
            view.setHeight(child.getMeasuredHeight());
            view.setNewLine(lp.isNewLine());
            view.setGravity(lp.getGravity());
            view.setWeight(lp.getWeight());
            view.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            views.add(view);
        }

        this.config.setMaxWidth(MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft());
        this.config.setMaxHeight(MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom());
        this.config.setWidthMode(MeasureSpec.getMode(widthMeasureSpec));
        this.config.setHeightMode(MeasureSpec.getMode(heightMeasureSpec));
        this.config.setCheckCanFit(this.config.getLengthMode() != View.MeasureSpec.UNSPECIFIED);

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

        /* need to take padding into account */
        int totalControlWidth = this.getPaddingLeft() + this.getPaddingRight();
        int totalControlHeight = this.getPaddingBottom() + this.getPaddingTop();
        if (this.config.getOrientation() == CommonLogic.HORIZONTAL) {
            totalControlWidth += contentLength;
            totalControlHeight += contentThickness;
        } else {
            totalControlWidth += contentThickness;
            totalControlHeight += contentLength;
        }
        this.setMeasuredDimension(resolveSize(totalControlWidth, widthMeasureSpec), resolveSize(totalControlHeight, heightMeasureSpec));
    }

    private void applyPositionsToViews(LineDefinition line) {
        final List<ViewDefinition> childViews = line.getViews();
        final int childCount = childViews.size();
        for (int i = 0; i < childCount; i++) {
            final ViewDefinition child = childViews.get(i);
            final View view = child.getView();
            view.measure(
                    MeasureSpec.makeMeasureSpec(child.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(child.getHeight(), MeasureSpec.EXACTLY)
            );
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int linesCount = this.lines.size();
        for (int i = 0; i < linesCount; i++) {
            final LineDefinition line = this.lines.get(i);

            final int count = line.getViews().size();
            for (int j = 0; j < count; j++) {
                ViewDefinition child = line.getViews().get(j);
                View view = child.getView();
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                view.layout(
                        this.getPaddingLeft() + line.getX() + child.getInlineX() + lp.leftMargin,
                        this.getPaddingTop() + line.getY() + child.getInlineY() + lp.topMargin,
                        this.getPaddingLeft() + line.getX() + child.getInlineX() + lp.leftMargin + child.getWidth(),
                        this.getPaddingTop() + line.getY() + child.getInlineY() + lp.topMargin + child.getHeight()
                );
            }
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
            if (this.config.getOrientation() == CommonLogic.HORIZONTAL) {
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

    @Override
    public int getLayoutDirection() {
        if (this.config == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return View.LAYOUT_DIRECTION_LTR;
            } else {
                return super.getLayoutDirection();
            }
        }

        //noinspection ResourceType
        return this.config.getLayoutDirection();
    }

    @Override
    public void setLayoutDirection(int layoutDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            super.setLayoutDirection(layoutDirection);
        }

        //noinspection ResourceType
        if (this.config.getLayoutDirection() != layoutDirection) {
            this.config.setLayoutDirection(layoutDirection);
            requestLayout();
        }
    }

    public int getMaxLines() {
        return this.config.getMaxLines();
    }

    public void setMaxLines(int maxLines) {
        this.config.setMaxLines(maxLines);
        this.requestLayout();
    }

    public static class LayoutParams extends MarginLayoutParams {
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

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
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
