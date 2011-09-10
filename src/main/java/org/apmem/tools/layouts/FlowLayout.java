package org.apmem.tools.layouts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import org.apmem.tools.R;

/**
 * User: Romain Guy
 * <p/>
 * Using example:
 * <?xml version="4.0" encoding="utf-8"?>
 * <com.example.android.layout.FlowLayout
 * xmlns:f="http://schemas.android.com/apk/res/org.apmem.android"
 * xmlns:android="http://schemas.android.com/apk/res/android"
 * f:horizontalSpacing="6dip"
 * f:verticalSpacing="12dip"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * android:paddingLeft="6dip"
 * android:paddingTop="6dip"
 * android:paddingRight="12dip">
 * <Button
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content"
 * f:layout_horizontalSpacing="32dip"
 * f:layout_breakLine="true"
 * android:text="Cancel" />
 * <p/>
 * </com.example.android.layout.FlowLayout>
 */
public class FlowLayout extends ViewGroup {
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private Paint paint;

    public FlowLayout(Context context) {
        super(context);

        this.readStyleParameters(context, null);
        this.initializePaint();
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.readStyleParameters(context, attributeSet);
        this.initializePaint();
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);

        this.readStyleParameters(context, attributeSet);
        this.initializePaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
        int sizeHeight = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int lineHeightWithSpacing = 0;
        int lineHeight = 0;
        int lineWidthWithSpacing = 0;
        int lineWidth;

        int controlMaxWidth = 0;
        int controlMaxHeight = 0;
        int prevLinePosY = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            child.measure(
                    MeasureSpec.makeMeasureSpec(sizeWidth, modeWidth == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeWidth),
                    MeasureSpec.makeMeasureSpec(sizeHeight, modeHeight == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : modeHeight)
            );

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int hSpacing = this.getHorizontalSpacing(lp);
            int vSpacing = this.getVerticalSpacing(lp);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            /* depend on orientation */
            lineWidth = lineWidthWithSpacing + childWidth;
            lineWidthWithSpacing = lineWidth + hSpacing;

            boolean newLine = lp.newLine || (modeWidth != MeasureSpec.UNSPECIFIED && lineWidth > sizeWidth);
            if (newLine) {
                prevLinePosY = prevLinePosY + lineHeightWithSpacing;

                lineHeight = childHeight;
                lineHeightWithSpacing = childHeight + vSpacing;

                lineWidth = childWidth;
                lineWidthWithSpacing = lineWidth + hSpacing;
            }

            lineHeightWithSpacing = Math.max(lineHeightWithSpacing, childHeight + vSpacing);
            lineHeight = Math.max(lineHeight, childHeight);

            controlMaxWidth = Math.max(controlMaxWidth, lineWidth);
            controlMaxHeight = prevLinePosY + lineHeight;

            int posX = getPaddingLeft() + lineWidth - childWidth;
            int posY = getPaddingTop() + prevLinePosY;

            lp.setPosition(posX, posY);
        }

        this.setMeasuredDimension(resolveSize(controlMaxWidth, widthMeasureSpec), resolveSize(controlMaxHeight, heightMeasureSpec));
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
        } finally {
            a.recycle();
        }
    }

    private void initializePaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xffff0000);
        paint.setStrokeWidth(2.0f);
    }

    private void drawDebugInfo(Canvas canvas, View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.horizontalSpacing > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y - 4.0f, x, y + 4.0f, paint);
            canvas.drawLine(x, y, x + lp.horizontalSpacing, y, paint);
            canvas.drawLine(x + lp.horizontalSpacing, y - 4.0f, x + lp.horizontalSpacing, y + 4.0f, paint);
        }

        if (lp.newLine) {
            float x = child.getLeft();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x, y + 6.0f, paint);
            canvas.drawLine(x, y + 6.0f, x + 6.0f, y + 6.0f, paint);
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        private static int NO_SPACING = -1;

        private int x;
        private int y;
        private int horizontalSpacing = NO_SPACING;
        private int verticalSpacing = NO_SPACING;
        private boolean newLine = false;

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
            } finally {
                a.recycle();
            }
        }
    }
}
