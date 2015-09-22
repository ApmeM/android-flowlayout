package org.apmem.tools.layouts;

import android.view.View;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest= Config.NONE)
public class LineDefinitionTests {
    @Test
    public void AddView_IncreaseLineLengthForChildLength() {
        View view1 = CreateView(12, 34);
        View view2 = CreateView(56, 78);

        LineDefinition def = new LineDefinition(100);
        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(68, def.getLineLength());
    }

    @Test
    public void AddView_AddViewIntoList() {
        View view1 = CreateView(12, 34);
        View view2 = CreateView(56, 78);

        LineDefinition def = new LineDefinition(100);
        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(view1, def.getViews().get(0));
        Assert.assertEquals(view2, def.getViews().get(1));
    }

    @Test
    public void AddView_SetThicknessToMaxBetweenThickness() {
        View view1 = CreateView(12, 34);
        View view2 = CreateView(56, 78);

        LineDefinition def = new LineDefinition(100);
        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(78, def.getLineThickness());
    }

    @Test
    public void AddView_TakesLayoutMarginsIntoAccount() {
        View view1 = CreateView(12, 34);
        View view2 = CreateView(56, 78);

        FlowLayout.LayoutParams lp1 = (FlowLayout.LayoutParams) view1.getLayoutParams();
        FlowLayout.LayoutParams lp2 = (FlowLayout.LayoutParams) view2.getLayoutParams();
        lp1.setMargins(1, 1, 1, 1);
        lp2.setMargins(1, 1, 1, 1);

        LineDefinition def = new LineDefinition(100);
        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(72, def.getLineLength());
        Assert.assertEquals(80, def.getLineThickness());
    }

    @Test
    public void CanFit_LengthLessThenRemainingIsOk() {
        View view1 = CreateView(12, 34);
        View view2 = CreateView(56, 78);

        LineDefinition def = new LineDefinition(100);
        def.addView(view1);
        def.addView(view2);

        boolean canFit = def.canFit(view1);

        Assert.assertTrue(canFit);
    }

    @Test
    public void CanFit_LengthMoreThenRemainingIsNotOk() {
        View view1 = CreateView(12, 34);
        View view2 = CreateView(56, 78);

        LineDefinition def = new LineDefinition(100);
        def.addView(view1);
        def.addView(view2);

        boolean canFit = def.canFit(view2);

        Assert.assertFalse(canFit);
    }

    @Test
    public void CanFit_TakesMarginIntoAccount() {
        View view1 = CreateView(12, 34);
        View view2 = CreateView(56, 78);

        FlowLayout.LayoutParams lp1 = (FlowLayout.LayoutParams) view1.getLayoutParams();
        FlowLayout.LayoutParams lp2 = (FlowLayout.LayoutParams) view2.getLayoutParams();
        lp1.setMargins(8, 1, 1, 1);
        lp2.setMargins(8, 1, 1, 1);

        LineDefinition def = new LineDefinition(100);
        def.addView(view1);
        def.addView(view2);

        boolean canFit = def.canFit(view1);

        Assert.assertFalse(canFit);
    }

    private View CreateView(int length, int thickness) {
        View view = Mockito.mock(View.class);
        FlowLayout.LayoutParams lp1 = new FlowLayout.LayoutParams(length, thickness);
        lp1.setLength(length);
        lp1.setThickness(thickness);
        Mockito.when(view.getLayoutParams()).thenReturn(lp1);
        return view;
    }

}