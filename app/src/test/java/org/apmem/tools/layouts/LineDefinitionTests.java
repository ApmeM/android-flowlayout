package org.apmem.tools.layouts;

import android.view.View;
import junit.framework.Assert;
import org.apmem.tools.layouts.logic.ConfigDefinition;
import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;
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
        ViewDefinition view1 = CreateView(12, 34);
        ViewDefinition view2 = CreateView(56, 78);

        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(100);
        LineDefinition def = new LineDefinition(config);

        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(68, def.getLineLength());
    }

    @Test
    public void AddView_AddViewIntoList() {
        ViewDefinition view1 = CreateView(12, 34);
        ViewDefinition view2 = CreateView(56, 78);

        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(100);
        LineDefinition def = new LineDefinition(config);

        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(view1, def.getViews().get(0));
        Assert.assertEquals(view2, def.getViews().get(1));
    }

    @Test
    public void AddView_SetThicknessToMaxBetweenThickness() {
        ViewDefinition view1 = CreateView(12, 34);
        ViewDefinition view2 = CreateView(56, 78);

        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(100);
        LineDefinition def = new LineDefinition(config);

        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(78, def.getLineThickness());
    }

    @Test
    public void AddView_TakesLayoutMarginsIntoAccount() {
        ViewDefinition view1 = CreateView(12, 34);
        ViewDefinition view2 = CreateView(56, 78);

        view1.setMargins(1, 1, 1, 1);
        view2.setMargins(1, 1, 1, 1);

        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(100);
        LineDefinition def = new LineDefinition(config);

        def.addView(view1);
        def.addView(view2);

        Assert.assertEquals(72, def.getLineLength());
        Assert.assertEquals(80, def.getLineThickness());
    }

    @Test
    public void CanFit_LengthLessThenRemainingIsOk() {
        ViewDefinition view1 = CreateView(12, 34);
        ViewDefinition view2 = CreateView(56, 78);

        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(100);
        LineDefinition def = new LineDefinition(config);

        def.addView(view1);
        def.addView(view2);

        boolean canFit = def.canFit(view1);

        Assert.assertTrue(canFit);
    }

    @Test
    public void CanFit_LengthMoreThenRemainingIsNotOk() {
        ViewDefinition view1 = CreateView(12, 34);
        ViewDefinition view2 = CreateView(56, 78);

        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(100);
        LineDefinition def = new LineDefinition(config);

        def.addView(view1);
        def.addView(view2);

        boolean canFit = def.canFit(view2);

        Assert.assertFalse(canFit);
    }

    @Test
    public void CanFit_TakesMarginIntoAccount() {
        ViewDefinition view1 = CreateView(12, 34);
        ViewDefinition view2 = CreateView(56, 78);

        view1.setMargins(8, 1, 1, 1);
        view2.setMargins(8, 1, 1, 1);

        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(100);
        LineDefinition def = new LineDefinition(config);
        def.addView(view1);
        def.addView(view2);

        boolean canFit = def.canFit(view1);

        Assert.assertFalse(canFit);
    }

    private ViewDefinition CreateView(int length, int thickness) {
        ViewDefinition view = new ViewDefinition(new ConfigDefinition(), null);
        view.setLength(length);
        view.setThickness(thickness);
        return view;
    }

}