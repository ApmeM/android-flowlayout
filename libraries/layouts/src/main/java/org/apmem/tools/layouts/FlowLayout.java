package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
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

    private float mWeightSum;
    private float mWeightDefault;
    private int mGravity = Gravity.LEFT | Gravity.TOP;
    private FillLines mFillLines = FillLines.NONE;

    private final List<View> mCurrentRow = new ArrayList<View>();

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

        if (orientation == HORIZONTAL) {
            size = sizeWidth;
            mode = modeWidth;
        } else {
            size = sizeHeight;
            mode = modeHeight;
        }

        int lineThicknessWithSpacing = 0;
        int lineThickness = 0;
        int lineLengthWithSpacing = 0;
        int lineLength;

        int prevLinePosition = 0;
        int prevLineLength = 0;

        int controlMaxLength = 0;
        int controlMaxThickness = 0;

        mCurrentRow.clear();
        List<View> row = mCurrentRow;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), lp.height)
            );

            int hSpacing = this.getHorizontalSpacing(lp);
            int vSpacing = this.getVerticalSpacing(lp);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            int childLength;
            int childThickness;
            int spacingLength;
            int spacingThickness;

            if (orientation == HORIZONTAL) {
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

            lineLength = lineLengthWithSpacing + childLength;
            lineLengthWithSpacing = lineLength + spacingLength;

            boolean newLine = lp.newLine || (mode != MeasureSpec.UNSPECIFIED && lineLength > size);
            if (newLine) {
                if (mFillLines != FillLines.NONE) {
                    fillLine(row, size, prevLineLength, lineThickness);
                }

                prevLinePosition = prevLinePosition + lineThicknessWithSpacing;

                lineThickness = childThickness;
                lineLength = childLength;
                lineThicknessWithSpacing = childThickness + spacingThickness;
                lineLengthWithSpacing = lineLength + spacingLength;
            }

            prevLineLength = lineLength;
            lineThicknessWithSpacing = Math.max(lineThicknessWithSpacing, childThickness + spacingThickness);
            lineThickness = Math.max(lineThickness, childThickness);

            int posX;
            int posY;
            if (orientation == HORIZONTAL) {
                posX = getPaddingLeft() + lineLength - childLength;
                posY = getPaddingTop() + prevLinePosition;
            } else {
                posX = getPaddingLeft() + prevLinePosition;
                posY = getPaddingTop() + lineLength - childHeight;
            }
            lp.setPosition(posX, posY);

            controlMaxLength = Math.max(controlMaxLength, lineLength);
            controlMaxThickness = prevLinePosition + lineThickness;

            row.add(child);
        }
        if (mFillLines == FillLines.ALL) {
            fillLine(row, size, prevLineLength, lineThickness);
        }

        /* need to take paddings into account */
        if (orientation == HORIZONTAL) {
            controlMaxLength += getPaddingLeft() + getPaddingRight();
            controlMaxThickness += getPaddingBottom() + getPaddingTop();
        } else {
            controlMaxLength += getPaddingBottom() + getPaddingTop();
            controlMaxThickness += getPaddingLeft() + getPaddingRight();
        }

        if (orientation == HORIZONTAL) {
            this.setMeasuredDimension(resolveSize(controlMaxLength, widthMeasureSpec), resolveSize(controlMaxThickness, heightMeasureSpec));
        } else {
            this.setMeasuredDimension(resolveSize(controlMaxThickness, widthMeasureSpec), resolveSize(controlMaxLength, heightMeasureSpec));
        }
    }

    private void fillLine(List<View> row, int size, int prevLineLength, int prevLineThickness) {
        int lineCount = row.size();
        float totalWeight = 0;
        if (lineCount > 0) {
            if (mWeightSum > 0) {
                totalWeight = mWeightSum;
            } else {
                for (View prev : row) {
                    LayoutParams plp = (LayoutParams) prev.getLayoutParams();
                    float weight = plp.weight < 0.0f ? mWeightDefault : plp.weight;
                    totalWeight += weight;
                }
            }
            if (totalWeight > 0) {
                int excess = size - prevLineLength;
                int accOffsetX = 0, accOffsetY = 0;
                for (View prev : row) {
                    int offsetX = 0, offsetY = 0;
                    LayoutParams plp = (LayoutParams) prev.getLayoutParams();

                    float weight = plp.weight < 0.0f ? mWeightDefault : plp.weight;
                    int extraPrimary = Math.round(excess * weight / totalWeight);
                    totalWeight -= weight;
                    excess -= extraPrimary;

                    int gravity = plp.gravity == Gravity.NO_GRAVITY ? mGravity : plp.gravity;
                    boolean scalePrimary = false, scaleSecondary = false;
                    int movePrimary = 0, moveSecondary = 0;
                    if (orientation == HORIZONTAL) {
                        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) != Gravity.LEFT) {
                            if ((gravity & (Gravity.FILL_HORIZONTAL ^ Gravity.RIGHT)) != 0) {
                                scalePrimary = true;
                            } else if ((gravity & (Gravity.RIGHT ^ Gravity.CENTER_HORIZONTAL)) != 0) {
                                movePrimary = 2;
                            } else if ((gravity & Gravity.CENTER_HORIZONTAL) != 0) {
                                movePrimary = 1;
                            }
                        }
                        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
                            if ((gravity & (Gravity.FILL_VERTICAL ^ Gravity.BOTTOM)) != 0) {
                                scaleSecondary = true;
                            } else if ((gravity & (Gravity.BOTTOM ^ Gravity.CENTER_VERTICAL)) != 0) {
                                moveSecondary = 2;
                            } else if ((gravity & Gravity.CENTER_VERTICAL) != 0) {
                                moveSecondary = 1;
                            }
                        }
                    } else {
                        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) != Gravity.LEFT) {
                            if ((gravity & (Gravity.FILL_HORIZONTAL ^ Gravity.RIGHT)) != 0) {
                                scaleSecondary = true;
                            } else if ((gravity & (Gravity.RIGHT ^ Gravity.CENTER_HORIZONTAL)) != 0) {
                                moveSecondary = 2;
                            } else if ((gravity & Gravity.CENTER_HORIZONTAL) != 0) {
                                moveSecondary = 1;
                            }
                        }
                        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
                            if ((gravity & (Gravity.FILL_VERTICAL ^ Gravity.BOTTOM)) != 0) {
                                scalePrimary = true;
                            } else if ((gravity & (Gravity.BOTTOM ^ Gravity.CENTER_VERTICAL)) != 0) {
                                movePrimary = 2;
                            } else if ((gravity & Gravity.CENTER_VERTICAL) != 0) {
                                movePrimary = 1;
                            }
                        }
                    }
                    int extraSecondary;
                    if (orientation == HORIZONTAL) {
                        extraSecondary = prevLineThickness - prev.getMeasuredHeight();
                    } else {
                        extraSecondary = prevLineThickness - prev.getMeasuredWidth();
                    }
                    int fillX = 0, fillY = 0;
                    if (orientation == HORIZONTAL) {
                        offsetX += (extraPrimary * movePrimary) / 2;
                    } else {
                        offsetY += (extraPrimary * movePrimary) / 2;
                    }
                    if (orientation == HORIZONTAL) {
                        offsetY += (extraSecondary * moveSecondary) / 2;
                    } else {
                        offsetX += (extraSecondary * moveSecondary) / 2;
                    }
                    plp.setPosition(plp.x + accOffsetX + offsetX, plp.y + accOffsetY + offsetY);
                    if (scalePrimary) {
                        if (orientation == HORIZONTAL) {
                            fillX += extraPrimary;
                        } else {
                            fillY += extraPrimary;
                        }
                    }
                    if (scaleSecondary) {
                        if (orientation == HORIZONTAL) {
                            fillY += extraSecondary;
                        } else {
                            fillX += extraSecondary;
                        }
                    }
                    if (fillX != 0 || fillY != 0) {
                        prev.measure(
                                MeasureSpec.makeMeasureSpec(prev.getMeasuredWidth() + fillX, MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(prev.getMeasuredHeight() + fillY, MeasureSpec.EXACTLY)
                        );
                    }
                    if (orientation == HORIZONTAL) {
                        accOffsetX += extraPrimary;
                    } else {
                        accOffsetY += extraPrimary;
                    }
                }
            }
            row.clear();
        }
    }

    private int getVerticalSpacing(LayoutParams lp) {
        int vSpacing;
        if (lp.verticalSpacingSpecified()) {
            vSpacing = lp.verticalSpacing;
        } else {
            vSpacing = this.verticalSpacing;
        }
        return vSpacing;
    }

    private int getHorizontalSpacing(LayoutParams lp) {
        int hSpacing;
        if (lp.horizontalSpacingSpecified()) {
            hSpacing = lp.horizontalSpacing;
        } else {
            hSpacing = this.horizontalSpacing;
        }
        return hSpacing;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
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
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    private void readStyleParameters(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        try {
            horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, 0);
            verticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 0);
            orientation = a.getInteger(R.styleable.FlowLayout_orientation, HORIZONTAL);
            debugDraw = a.getBoolean(R.styleable.FlowLayout_debugDraw, false);
            mWeightSum = a.getFloat(R.styleable.FlowLayout_weightSum, 0.0f);
            mWeightDefault = a.getFloat(R.styleable.FlowLayout_weightDefault, 0.0f);
            int gravityIndex = a.getInt(R.styleable.FlowLayout_android_gravity, -1);
            if (gravityIndex >= 0) {
                setGravity(gravityIndex);
            }
            int fillLinesIndex = a.getInt(R.styleable.FlowLayout_fillLines, -1);
            if (fillLinesIndex >= 0) {
                setFillLines(FillLines.from(fillLinesIndex));
            }
        } finally {
            a.recycle();
        }
    }

    private void drawDebugInfo(Canvas canvas, View child) {
        if (!debugDraw) {
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
            if (orientation == HORIZONTAL) {
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

    public float getWeightSum() {
        return mWeightSum;
    }

    public void setWeightSum(float weightSum) {
        mWeightSum = Math.max(0, weightSum);
        requestLayout();
    }

    public float getWeightDefault() {
        return mWeightDefault;
    }

    public void setWeightDefault(float weightDefault) {
        mWeightDefault = Math.max(0, weightDefault);
        requestLayout();
    }

    public int getGravity() {
        return mGravity;
    }

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.LEFT;
            }
            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }
            mGravity = gravity;
            requestLayout();
        }
    }

    public FillLines getFillLines() {
        return mFillLines;
    }

    public void setFillLines(FillLines fillLines) {
        mFillLines = fillLines != null ? fillLines : FillLines.NONE;
        requestLayout();
    }

    public enum FillLines {
        NONE(FILL_LINES_NONE),
        EXCEPT_LAST(FILL_LINES_EXCEPT_LAST),
        ALL(FILL_LINES_ALL);

        private final int mIntValue;

        FillLines(int intValue) {
            mIntValue = intValue;
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
            return mIntValue;
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        private static int NO_SPACING = -1;
        private int x;
        private int y;
        public int horizontalSpacing = NO_SPACING;
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
            return horizontalSpacing != NO_SPACING;
        }

        public boolean verticalSpacingSpecified() {
            return verticalSpacing != NO_SPACING;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private void readStyleParameters(Context context, AttributeSet attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout_LayoutParams);
            try {
                horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_horizontalSpacing, NO_SPACING);
                verticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_verticalSpacing, NO_SPACING);
                newLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_newLine, false);
                gravity = a.getInt(R.styleable.FlowLayout_LayoutParams_android_layout_gravity, gravity);
                weight = a.getFloat(R.styleable.FlowLayout_LayoutParams_layout_weight, weight);
            } finally {
                a.recycle();
            }
        }
    }
}
