package org.apmem.tools.layouts.logic;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;

import java.util.List;

public class CommonLogic {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static void calculateLinesAndChildPosition(List<LineDefinition> lines) {
        int prevLinesThickness = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            final LineDefinition line = lines.get(i);
            line.setLineStartThickness(prevLinesThickness);
            prevLinesThickness += line.getLineThickness();
            int prevChildThickness = 0;
            final List<ViewDefinition> childViews = line.getViews();
            final int childCount = childViews.size();
            for (int j = 0; j < childCount; j++) {
                ViewDefinition child = childViews.get(j);
                child.setInlineStartLength(prevChildThickness);
                prevChildThickness += child.getLength() + child.getSpacingLength();
            }
        }
    }

    public static void applyGravityToLines(List<LineDefinition> lines, int realControlLength, int realControlThickness, ConfigDefinition config) {
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

            applyGravityToLine(child, config);
        }
    }

    public static void applyGravityToLine(LineDefinition line, ConfigDefinition config) {
        final List<ViewDefinition> views = line.getViews();
        final int viewCount = views.size();
        if (viewCount <= 0) {
            return;
        }

        float totalWeight = 0;
        for (int i = 0; i < viewCount; i++) {
            final ViewDefinition child = views.get(i);
            totalWeight += getWeight(child, config);
        }

        ViewDefinition lastChild = views.get(viewCount - 1);
        int excessLength = line.getLineLength() - (lastChild.getLength() + lastChild.getSpacingLength() + lastChild.getInlineStartLength());
        int excessOffset = 0;
        for (int i = 0; i < viewCount; i++) {
            final ViewDefinition child = views.get(i);
            float weight = getWeight(child, config);
            int gravity = getGravity(child, config);
            int extraLength;
            if (totalWeight == 0) {
                extraLength = excessLength / viewCount;
            } else {
                extraLength = Math.round(excessLength * weight / totalWeight);
            }

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
            child.setInlineStartLength(result.left + child.getInlineStartLength());
            child.setInlineStartThickness(result.top);
            child.setLength(result.width() - child.getSpacingLength());
            child.setThickness(result.height() - child.getSpacingThickness());
        }
    }

    public static int findSize(int modeSize, int controlMaxSize, int contentSize) {
        int realControlSize;
        switch (modeSize) {
            case View.MeasureSpec.UNSPECIFIED:
                realControlSize = contentSize;
                break;
            case View.MeasureSpec.AT_MOST:
                realControlSize = Math.min(contentSize, controlMaxSize);
                break;
            case View.MeasureSpec.EXACTLY:
                realControlSize = controlMaxSize;
                break;
            default:
                realControlSize = contentSize;
                break;
        }
        return realControlSize;
    }

    private static float getWeight(ViewDefinition child, ConfigDefinition config) {
        return child.weightSpecified() ? child.getWeight() : config.getWeightDefault();
    }


    private static int getGravity(ViewDefinition child, ConfigDefinition config) {
        int parentGravity = config.getGravity();

        int childGravity;
        // get childGravity of child view (if exists)
        if (child != null && child.gravitySpecified()) {
            childGravity = child.getGravity();
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


    public static int getGravityFromRelative(int childGravity, ConfigDefinition config) {
        // swap directions for vertical non relative view
        // if it is relative, then START is TOP, and we do not need to switch it here.
        // it will be switched later on onMeasure stage when calculations will be with length and thickness
        if (config.getOrientation() == CommonLogic.VERTICAL && (childGravity & Gravity.RELATIVE_LAYOUT_DIRECTION) == 0) {
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

    public static void fillLines(List<ViewDefinition> views, List<LineDefinition> lines, ConfigDefinition config) {
        LineDefinition currentLine = new LineDefinition(config);
        lines.add(currentLine);
        final int count = views.size();
        for (int i = 0; i < count; i++) {
            final ViewDefinition child = views.get(i);

            boolean newLine = child.isNewLine() || (config.isCheckCanFit() && !currentLine.canFit(child));
            if (newLine) {
                currentLine = new LineDefinition(config);
                if (config.getOrientation() == CommonLogic.VERTICAL && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    lines.add(0, currentLine);
                } else {
                    lines.add(currentLine);
                }
            }

            if (config.getOrientation() == CommonLogic.HORIZONTAL && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                currentLine.addView(0, child);
            } else {
                currentLine.addView(child);
            }
        }
    }
}
