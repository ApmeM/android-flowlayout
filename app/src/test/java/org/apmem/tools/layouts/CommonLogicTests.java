package org.apmem.tools.layouts;

import junit.framework.Assert;
import org.apmem.tools.layouts.logic.CommonLogic;
import org.apmem.tools.layouts.logic.ConfigDefinition;
import org.apmem.tools.layouts.logic.LineDefinition;
import org.apmem.tools.layouts.logic.ViewDefinition;
import org.junit.Test;

import java.util.ArrayList;

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
}
