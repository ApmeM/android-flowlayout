package org.apmem.tools.layouts;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;

import java.util.List;

public class Common {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    static void calculateLinesAndChildPosition(List<LineDefinition> lines) {
        int prevLinesThickness = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            final LineDefinition line = lines.get(i);
            line.setLineStartThickness(prevLinesThickness);
            prevLinesThickness += line.getLineThickness();
            int prevChildThickness = 0;
            final List<View> childViews = line.getViews();
            final int childCount = childViews.size();
            for (int j = 0; j < childCount; j++) {
                View child = childViews.get(j);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                lp.setInlineStartLength(prevChildThickness);
                prevChildThickness += lp.getLength() + lp.getSpacingLength();
            }
        }
    }

    static void applyGravityToLines(List<LineDefinition> lines, int realControlLength, int realControlThickness, LayoutConfiguration config) {
        final int linesCount = lines.size();
        if (linesCount <= 0) {
            return;
        }

        final int totalWeight = linesCount;
        LineDefinition lastLine = lines.get(linesCount - 1);
        int excessThickness = realControlThickness - (lastLine.getLineThickness() + lastLine.getLineStartThickness());

        if (excessThickness < 0) {
            excessThickness = 0;
        }

        int excessOffset = 0;
        for (int i = 0; i < linesCount; i++) {
            final LineDefinition child = lines.get(i);
            int weight = 1;
            int gravity = getGravity(null, config);
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
            child.setLineStartLength(child.getLineStartLength() + result.left);
            child.setLineStartThickness(child.getLineStartThickness() + result.top);
            child.setLength(result.width());
            child.setThickness(result.height());
        }
    }

    static void applyGravityToLine(LineDefinition line, LayoutConfiguration config) {
        final List<View> views = line.getViews();
        final int viewCount = views.size();
        if (viewCount <= 0) {
            return;
        }

        float totalWeight = 0;
        for (int i = 0; i < viewCount; i++) {
            final View prev = views.get(i);
            LayoutParams plp = (LayoutParams) prev.getLayoutParams();
            totalWeight += getWeight(plp, config);
        }

        View lastChild = views.get(viewCount - 1);
        LayoutParams lastChildLayoutParams = (LayoutParams) lastChild.getLayoutParams();
        int excessLength = line.getLineLength() - (lastChildLayoutParams.getLength() + lastChildLayoutParams.getSpacingLength() + lastChildLayoutParams.getInlineStartLength());
        int excessOffset = 0;
        for (int i = 0; i < viewCount; i++) {
            final View child = views.get(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

            float weight = getWeight(layoutParams, config);
            int gravity = getGravity(layoutParams, config);
            int extraLength;
            if (totalWeight == 0) {
                extraLength = excessLength / viewCount;
            } else {
                extraLength = Math.round(excessLength * weight / totalWeight);
            }

            final int childLength = layoutParams.getLength() + layoutParams.getSpacingLength();
            final int childThickness = layoutParams.getThickness() + layoutParams.getSpacingThickness();

            Rect container = new Rect();
            container.top = 0;
            container.left = excessOffset;
            container.right = childLength + extraLength + excessOffset;
            container.bottom = line.getLineThickness();

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraLength;
            layoutParams.setInlineStartLength(result.left + layoutParams.getInlineStartLength());
            layoutParams.setInlineStartThickness(result.top);
            layoutParams.setLength(result.width() - layoutParams.getSpacingLength());
            layoutParams.setThickness(result.height() - layoutParams.getSpacingThickness());
        }
    }

    static int findSize(int modeSize, int controlMaxSize, int contentSize) {
        int realControlLength;
        switch (modeSize) {
            case View.MeasureSpec.UNSPECIFIED:
                realControlLength = contentSize;
                break;
            case View.MeasureSpec.AT_MOST:
                realControlLength = Math.min(contentSize, controlMaxSize);
                break;
            case View.MeasureSpec.EXACTLY:
                realControlLength = controlMaxSize;
                break;
            default:
                realControlLength = contentSize;
                break;
        }
        return realControlLength;
    }

    private static float getWeight(LayoutParams lp, LayoutConfiguration config) {
        return lp.weightSpecified() ? lp.getWeight() : config.getWeightDefault();
    }


    private static int getGravity(LayoutParams lp, LayoutConfiguration config) {
        int parentGravity = config.getGravity();

        int childGravity;
        // get childGravity of child view (if exists)
        if (lp != null && lp.gravitySpecified()) {
            childGravity = lp.getGravity();
        } else {
            childGravity = parentGravity;
        }

        childGravity = getGravityFromRelative(childGravity, config);
        parentGravity = getGravityFromRelative(parentGravity, config);

        // add parent gravity to child gravity if child gravity is not specified
        if ((childGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            childGravity |= parentGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        }
        if ((childGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            childGravity |= parentGravity & Gravity.VERTICAL_GRAVITY_MASK;
        }

        // if childGravity is still not specified - set default top - left gravity
        if ((childGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            childGravity |= Gravity.LEFT;
        }
        if ((childGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            childGravity |= Gravity.TOP;
        }

        return childGravity;
    }


    static int getGravityFromRelative(int childGravity, LayoutConfiguration config) {
        // swap directions for vertical non relative view
        // if it is relative, then START is TOP, and we do not need to switch it here.
        // it will be switched later on onMeasure stage when calculations will be with length and thickness
        if (config.getOrientation() == Common.VERTICAL && (childGravity & Gravity.RELATIVE_LAYOUT_DIRECTION) == 0) {
            int horizontalGravity = childGravity;
            childGravity = 0;
            childGravity |= (horizontalGravity & Gravity.HORIZONTAL_GRAVITY_MASK) >> Gravity.AXIS_X_SHIFT << Gravity.AXIS_Y_SHIFT;
            childGravity |= (horizontalGravity & Gravity.VERTICAL_GRAVITY_MASK) >> Gravity.AXIS_Y_SHIFT << Gravity.AXIS_X_SHIFT;
        }

        // for relative layout and RTL direction swap left and right gravity
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL && (childGravity & Gravity.RELATIVE_LAYOUT_DIRECTION) != 0) {
            int ltrGravity = childGravity;
            childGravity = 0;
            childGravity |= (ltrGravity & Gravity.LEFT) == Gravity.LEFT ? Gravity.RIGHT : 0;
            childGravity |= (ltrGravity & Gravity.RIGHT) == Gravity.RIGHT ? Gravity.LEFT : 0;
        }

        return childGravity;
    }

    static void fillLines(ChildProvider childProvider, List<LineDefinition> lines, LayoutConfiguration config, int controlMaxLength, boolean checkCanFit) {
        lines.clear();
        LineDefinition currentLine = new LineDefinition(controlMaxLength);
        lines.add(currentLine);
        final int count = childProvider.provideChildCount();
        for (int i = 0; i < count; i++) {
            final View child = childProvider.provideChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (config.getOrientation() == Common.HORIZONTAL) {
                lp.setLength(child.getMeasuredWidth());
                lp.setThickness(child.getMeasuredHeight());
            } else {
                lp.setLength(child.getMeasuredHeight());
                lp.setThickness(child.getMeasuredWidth());
            }

            boolean newLine = lp.isNewLine() || (checkCanFit && !currentLine.canFit(child));
            if (newLine) {
                currentLine = new LineDefinition(controlMaxLength);
                if (config.getOrientation() == Common.VERTICAL && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    lines.add(0, currentLine);
                } else {
                    lines.add(currentLine);
                }
            }

            if (config.getOrientation() == Common.HORIZONTAL && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                currentLine.addView(0, child);
            } else {
                currentLine.addView(child);
            }
        }
    }

    interface LayoutParams {
        int getLength();

        void setLength(int length);

        int getSpacingLength();

        boolean isNewLine();

        int getThickness();

        void setThickness(int thickness);

        int getSpacingThickness();

        int getInlineStartLength();

        void setInlineStartLength(int prevChildThickness);

        int getGravity();

        float getWeight();

        void setInlineStartThickness(int inlineStartThickness);

        boolean gravitySpecified();

        boolean weightSpecified();
    }

    interface ChildProvider {

        int provideChildCount();

        View provideChildAt(int i);
    }
}
