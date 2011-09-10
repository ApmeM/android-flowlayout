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
 *
 * Using example:
 * <?xml version="4.0" encoding="utf-8"?>
<com.example.android.layout.FlowLayout
	xmlns:f="http://schemas.android.com/apk/res/org.apmem.android"
    xmlns:android="http://schemas.android.com/apk/res/android"
    f:horizontalSpacing="6dip"
    f:verticalSpacing="12dip"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:paddingLeft="6dip"
    android:paddingTop="6dip"
    android:paddingRight="12dip">
	<Button
	    android:layout_width="wrap_content"
	    android:layout_height="wrap_content"
	    f:layout_horizontalSpacing="32dip"
	    f:layout_breakLine="true"
	    android:text="Cancel" />

</com.example.android.layout.FlowLayout>
 */
public class FlowLayout extends ViewGroup {
    private int horizontalSpacing;
    private int verticalSpacing;
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
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        boolean growHeight = widthMode != MeasureSpec.UNSPECIFIED;

        int width = 0;
        int height = getPaddingTop();

        int currentWidth = getPaddingLeft();
        int currentHeight = 0;

        boolean breakLine = false;
        boolean newLine = false;
        int spacing = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            super.measureChild(child, widthMeasureSpec, heightMeasureSpec);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            spacing = horizontalSpacing;
            if (lp.horizontalSpacing >= 0) {
                spacing = lp.horizontalSpacing;
            }

            if (growHeight && (breakLine || currentWidth + child.getMeasuredWidth() > widthSize)) {
                height += currentHeight + verticalSpacing;
                currentHeight = 0;
                width = Math.max(width, currentWidth - spacing);
                currentWidth = getPaddingLeft();
                newLine = true;
            } else {
                newLine = false;
            }

            lp.x = currentWidth;
            lp.y = height;

            currentWidth += child.getMeasuredWidth() + spacing;
            currentHeight = Math.max(currentHeight, child.getMeasuredHeight());

            breakLine = lp.breakLine;
        }

        if (!newLine) {
            height += currentHeight;
            width = Math.max(width, currentWidth - spacing);
        }

        width += getPaddingRight();
        height += getPaddingBottom();

        this.setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
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
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p.width, p.height);
    }

    private void readStyleParameters(Context context, AttributeSet attributeSet){
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
            try {
                horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, 0);
                verticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 0);
            } finally {
                a.recycle();
            }
    }
    private void initializePaint(){
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
        if (lp.breakLine) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x, y + 6.0f, paint);
            canvas.drawLine(x, y + 6.0f, x + 6.0f, y + 6.0f, paint);
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        int x;
        int y;

        public int horizontalSpacing;
        public boolean breakLine;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);

            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout_LayoutParams);
			try {
				horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_horizontalSpacing, -1);
				breakLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_breakLine, false);
			} finally {
				a.recycle();
			}

        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
