package org.apmem.tools.layouts;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class FlowLayoutSinglelineTests {
    TestActivity activity = Robolectric.setupActivity(TestActivity.class);

    @Test
    public void SingleLineTopLeftGravity() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.LEFT | Gravity.TOP);

        final Button btn1 = new Button(activity);
        FlowLayout.LayoutParams lp1 = new FlowLayout.LayoutParams(30, 40);
        lp1.setMargins(1, 2, 3, 4);
        btn1.setLayoutParams(lp1);
        layout.addView(btn1);

        final Button btn2 = new Button(activity);
        FlowLayout.LayoutParams lp2 = new FlowLayout.LayoutParams(10, 20);
        lp2.setMargins(1, 2, 3, 4);
        btn2.setLayoutParams(lp2);
        layout.addView(btn2);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals(0, lp1.getX());
        Assert.assertEquals(0, lp1.getY());
        Assert.assertEquals(30 + 1 + 3, lp2.getX());
        Assert.assertEquals(0, lp2.getY());

        Assert.assertEquals(30, lp1.getLength());
        Assert.assertEquals(40, lp1.getThickness());
        Assert.assertEquals(10, lp2.getLength());
        Assert.assertEquals(20, lp2.getThickness());
    }

    @Test
    public void SingleLineRightBottomGravity() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.RIGHT | Gravity.BOTTOM);

        final Button btn1 = new Button(activity);
        FlowLayout.LayoutParams lp1 = new FlowLayout.LayoutParams(30, 40);
        lp1.setMargins(1, 2, 3, 4);
        btn1.setLayoutParams(lp1);
        layout.addView(btn1);

        final Button btn2 = new Button(activity);
        FlowLayout.LayoutParams lp2 = new FlowLayout.LayoutParams(10, 20);
        lp2.setMargins(1, 2, 3, 4);
        btn2.setLayoutParams(lp2);
        layout.addView(btn2);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals((70 - (10 + 1 + 3 + 30 + 1 + 3)), lp1.getX());
        Assert.assertEquals((80 - (40 + 2 + 4)), lp1.getY());
        Assert.assertEquals((70 - (10 + 1 + 3)), lp2.getX());
        Assert.assertEquals((80 - (20 + 2 + 4)), lp2.getY());

        Assert.assertEquals(30, lp1.getLength());
        Assert.assertEquals(40, lp1.getThickness());
        Assert.assertEquals(10, lp2.getLength());
        Assert.assertEquals(20, lp2.getThickness());
    }

    @Test
    public void SingleLineCenterGravity() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.CENTER);

        final Button btn1 = new Button(activity);
        FlowLayout.LayoutParams lp1 = new FlowLayout.LayoutParams(30, 40);
        lp1.setMargins(1, 2, 3, 4);
        btn1.setLayoutParams(lp1);
        layout.addView(btn1);

        final Button btn2 = new Button(activity);
        FlowLayout.LayoutParams lp2 = new FlowLayout.LayoutParams(10, 20);
        lp2.setMargins(1, 2, 3, 4);
        btn2.setLayoutParams(lp2);
        layout.addView(btn2);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2, lp1.getX());
        Assert.assertEquals((80 - (40 + 2 + 4)) / 2, lp1.getY());
        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2 + 30 + 1 + 3, lp2.getX());
        Assert.assertEquals((80 - (20 + 2 + 4)) / 2, lp2.getY());

        Assert.assertEquals(30, lp1.getLength());
        Assert.assertEquals(40, lp1.getThickness());
        Assert.assertEquals(10, lp2.getLength());
        Assert.assertEquals(20, lp2.getThickness());
    }

    @Test
    public void SingleLineCenterGravityWithBigMarginOfSecondItem() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.CENTER);

        final Button btn1 = new Button(activity);
        FlowLayout.LayoutParams lp1 = new FlowLayout.LayoutParams(30, 40);
        lp1.setMargins(1, 2, 3, 4);
        btn1.setLayoutParams(lp1);
        layout.addView(btn1);

        final Button btn2 = new Button(activity);
        FlowLayout.LayoutParams lp2 = new FlowLayout.LayoutParams(10, 20);
        lp2.setMargins(1, 16, 3, 20);
        btn2.setLayoutParams(lp2);
        layout.addView(btn2);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2, lp1.getX());
        Assert.assertEquals((80 - (40 + 2 + 4)) / 2, lp1.getY());
        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2 + 30 + 1 + 3, lp2.getX());
        Assert.assertEquals((80 - (20 + 16 + 20)) / 2, lp2.getY());

        Assert.assertEquals(30, lp1.getLength());
        Assert.assertEquals(40, lp1.getThickness());
        Assert.assertEquals(10, lp2.getLength());
        Assert.assertEquals(20, lp2.getThickness());
    }

    @Test
    public void SingleLineFillGravityWithBigMarginOfSecondItem() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.FILL);

        final Button btn1 = new Button(activity);
        FlowLayout.LayoutParams lp1 = new FlowLayout.LayoutParams(30, 40);
        lp1.setMargins(1, 2, 3, 4);
        btn1.setLayoutParams(lp1);
        layout.addView(btn1);

        final Button btn2 = new Button(activity);
        FlowLayout.LayoutParams lp2 = new FlowLayout.LayoutParams(10, 20);
        lp2.setMargins(1, 16, 3, 20);
        btn2.setLayoutParams(lp2);
        layout.addView(btn2);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals(0, lp1.getX());
        Assert.assertEquals(0, lp1.getY());
        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2 + 30 + 1 + 3, lp2.getX());
        Assert.assertEquals(0, lp2.getY());

        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2 + 30 + 1 + 3 - (1 + 3), lp1.getLength());
        Assert.assertEquals(80 - (2 + 4), lp1.getThickness());
        Assert.assertEquals(70 - ((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2 + 30 + 1 + 3) - (1 + 3), lp2.getLength());
        Assert.assertEquals(80 - (16 + 20), lp2.getThickness());
    }

    @Test
    public void SingleLineCenterGravitySecondViewGravityTop() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.CENTER);

        final Button btn1 = new Button(activity);
        FlowLayout.LayoutParams lp1 = new FlowLayout.LayoutParams(30, 40);
        lp1.setMargins(1, 2, 3, 4);
        btn1.setLayoutParams(lp1);
        layout.addView(btn1);

        final Button btn2 = new Button(activity);
        FlowLayout.LayoutParams lp2 = new FlowLayout.LayoutParams(10, 20);
        lp2.setMargins(1, 2, 3, 4);
        lp2.setGravity(Gravity.TOP);
        btn2.setLayoutParams(lp2);
        layout.addView(btn2);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2, lp1.getX());
        Assert.assertEquals((80 - (40 + 2 + 4)) / 2, lp1.getY());
        Assert.assertEquals((70 - (30 + 1 + 3 + 10 + 1 + 3)) / 2 + 30 + 1 + 3, lp2.getX());
        Assert.assertEquals((80 - (40 + 2 + 4)) / 2, lp2.getY());

        Assert.assertEquals(30, lp1.getLength());
        Assert.assertEquals(40, lp1.getThickness());
        Assert.assertEquals(10, lp2.getLength());
        Assert.assertEquals(20, lp2.getThickness());
    }
}
