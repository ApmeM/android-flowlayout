package org.apmem.tools.layouts;

import android.view.Gravity;
import junit.framework.Assert;
import org.apmem.tools.layouts.logic.CommonLogic;
import org.apmem.tools.layouts.logic.ConfigDefinition;
import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class CommonLogicTests {
    @Test
    public void MaxLinesNotSetAllLinesAddedWhenNotSet() {
        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(20);
        ArrayList<LineDefinition> lines = new ArrayList<>();
        ArrayList<ViewDefinition> views = new ArrayList<>();
        for(int i = 0; i < 9; i++){
            ViewDefinition view = new ViewDefinition(config, null);
            view.setWidth(10);
            view.setHeight(10);
            views.add(view);
        }

        CommonLogic.fillLines(views, lines, config);

        Assert.assertEquals(5, lines.size());
        Assert.assertEquals(2, lines.get(0).getViews().size());
        Assert.assertEquals(2, lines.get(1).getViews().size());
        Assert.assertEquals(2, lines.get(2).getViews().size());
        Assert.assertEquals(2, lines.get(3).getViews().size());
        Assert.assertEquals(1, lines.get(4).getViews().size());
    }

    @Test
    public void MaxLinesSetLinesCountEqualToMaxLines() {
        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(20);
        config.setMaxLines(2);
        ArrayList<LineDefinition> lines = new ArrayList<>();
        ArrayList<ViewDefinition> views = new ArrayList<>();
        for(int i = 0; i < 9; i++){
            ViewDefinition view = new ViewDefinition(config, null);
            view.setWidth(10);
            view.setHeight(10);
            views.add(view);
        }

        CommonLogic.fillLines(views, lines, config);

        Assert.assertEquals(2, lines.size());
        Assert.assertEquals(2, lines.get(0).getViews().size());
        Assert.assertEquals(2, lines.get(1).getViews().size());
    }

    @Test
    public void GravityApplyUseAllAvailableSpaceHorizontally() {
        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(30);
        config.setMaxHeight(20);
        config.setGravity(Gravity.FILL);
        ArrayList<LineDefinition> lines = new ArrayList<>();
        ArrayList<ViewDefinition> views = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            ViewDefinition view = new ViewDefinition(config, null);
            view.setWidth(10);
            view.setHeight(10);
            views.add(view);
        }

        CommonLogic.fillLines(views, lines, config);
        CommonLogic.calculateLinesAndChildPosition(lines);
        CommonLogic.applyGravityToLines(lines, 30, 20, config);

        Assert.assertEquals(2, lines.size());
        Assert.assertEquals(10, lines.get(0).getViews().get(0).getWidth());
        Assert.assertEquals(10, lines.get(0).getViews().get(1).getWidth());
        Assert.assertEquals(10, lines.get(0).getViews().get(2).getWidth());
    }

    @Test
    public void GravityApplyUseAllAvailableSpaceHorizontallyWithLastPixel() {
        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(31);
        config.setMaxHeight(20);
        config.setGravity(Gravity.FILL);
        ArrayList<LineDefinition> lines = new ArrayList<>();
        ArrayList<ViewDefinition> views = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            ViewDefinition view = new ViewDefinition(config, null);
            view.setWidth(10);
            view.setHeight(10);
            views.add(view);
        }

        CommonLogic.fillLines(views, lines, config);
        CommonLogic.calculateLinesAndChildPosition(lines);
        CommonLogic.applyGravityToLines(lines, 31, 20, config);

        Assert.assertEquals(2, lines.size());
        Assert.assertEquals(10, lines.get(0).getViews().get(0).getWidth());
        Assert.assertEquals(10, lines.get(0).getViews().get(1).getWidth());
        Assert.assertEquals(11, lines.get(0).getViews().get(2).getWidth());
    }

    @Test
    public void GravityApplyUseAllAvailableSpaceVertically() {
        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(30);
        config.setMaxHeight(20);
        config.setGravity(Gravity.FILL);
        ArrayList<LineDefinition> lines = new ArrayList<>();
        ArrayList<ViewDefinition> views = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            ViewDefinition view = new ViewDefinition(config, null);
            view.setWidth(10);
            view.setHeight(10);
            views.add(view);
        }

        CommonLogic.fillLines(views, lines, config);
        CommonLogic.calculateLinesAndChildPosition(lines);
        CommonLogic.applyGravityToLines(lines, 30, 20, config);

        Assert.assertEquals(2, lines.size());
        Assert.assertEquals(10, lines.get(0).getViews().get(0).getHeight());
        Assert.assertEquals(10, lines.get(1).getViews().get(0).getHeight());
    }

    @Test
    public void GravityApplyUseAllAvailableSpaceVerticallyWithLastPixel() {
        ConfigDefinition config = new ConfigDefinition();
        config.setMaxWidth(30);
        config.setMaxHeight(21);
        config.setGravity(Gravity.FILL);
        ArrayList<LineDefinition> lines = new ArrayList<>();
        ArrayList<ViewDefinition> views = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            ViewDefinition view = new ViewDefinition(config, null);
            view.setWidth(10);
            view.setHeight(10);
            views.add(view);
        }

        CommonLogic.fillLines(views, lines, config);
        CommonLogic.calculateLinesAndChildPosition(lines);
        CommonLogic.applyGravityToLines(lines, 30, 21, config);

        Assert.assertEquals(2, lines.size());
        Assert.assertEquals(10, lines.get(0).getViews().get(0).getHeight());
        Assert.assertEquals(11, lines.get(1).getViews().get(0).getHeight());
    }
}
