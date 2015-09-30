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
public class FlowLayoutMultilineTests {
    TestActivity activity = Robolectric.setupActivity(TestActivity.class);

    @Test
    public void MultilineLineTopLeftGravity() {
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

        final Button btn3 = new Button(activity);
        FlowLayout.LayoutParams lp3 = new FlowLayout.LayoutParams(50, 10);
        lp3.setMargins(1, 2, 3, 4);
        btn3.setLayoutParams(lp3);
        layout.addView(btn3);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.AT_MOST)
        );
        layout.layout(0, 0, 0, 0);

        Assert.assertEquals(1, btn1.getLeft());
        Assert.assertEquals(2, btn1.getTop());
        Assert.assertEquals(30 + 1 + 3 + 1, btn2.getLeft());
        Assert.assertEquals(2, btn2.getTop());
        Assert.assertEquals(1, btn3.getLeft());
        Assert.assertEquals(40 + 2 + 4 + 2, btn3.getTop());

        Assert.assertEquals(30, btn1.getWidth());
        Assert.assertEquals(40, btn1.getHeight());
        Assert.assertEquals(10, btn2.getWidth());
        Assert.assertEquals(20, btn2.getHeight());
        Assert.assertEquals(50, btn3.getWidth());
        Assert.assertEquals(10, btn3.getHeight());
    }

    @Test
    public void MultilineLineTopLeftGravityWithExtraSpaceBetweenLine() {
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

        final Button btn3 = new Button(activity);
        FlowLayout.LayoutParams lp3 = new FlowLayout.LayoutParams(50, 10);
        lp3.setMargins(1, 2, 3, 4);
        btn3.setLayoutParams(lp3);
        layout.addView(btn3);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );
        layout.layout(0, 0, 0, 0);

        Assert.assertEquals(1, btn1.getLeft());
        Assert.assertEquals(2, btn1.getTop());
        Assert.assertEquals(30 + 1 + 3 + 1, btn2.getLeft());
        Assert.assertEquals(2, btn2.getTop());
        Assert.assertEquals(1, btn3.getLeft());
        Assert.assertEquals(40 + 2 + 4 + (80 - (40 + 2 + 4 + 10 + 2 + 4)) / 2 + 2, btn3.getTop());

        Assert.assertEquals(30, btn1.getWidth());
        Assert.assertEquals(40, btn1.getHeight());
        Assert.assertEquals(10, btn2.getWidth());
        Assert.assertEquals(20, btn2.getHeight());
        Assert.assertEquals(50, btn3.getWidth());
        Assert.assertEquals(10, btn3.getHeight());
    }

    @Test
    public void MultilineLineTopLeftGravitySecondLineNotFit() {
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

        final Button btn3 = new Button(activity);
        FlowLayout.LayoutParams lp3 = new FlowLayout.LayoutParams(50, 30);
        lp3.setMargins(1, 2, 3, 4);
        btn3.setLayoutParams(lp3);
        layout.addView(btn3);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        );
        layout.layout(0, 0, 0, 0);

        Assert.assertEquals(1, btn1.getLeft());
        Assert.assertEquals(2, btn1.getTop());
        Assert.assertEquals(30 + 1 + 3 + 1, btn2.getLeft());
        Assert.assertEquals(2, btn2.getTop());
        Assert.assertEquals(1, btn3.getLeft());
        Assert.assertEquals(40 + 2 + 4 + 2, btn3.getTop());

        Assert.assertEquals(30, btn1.getWidth());
        Assert.assertEquals(40, btn1.getHeight());
        Assert.assertEquals(10, btn2.getWidth());
        Assert.assertEquals(20, btn2.getHeight());
        Assert.assertEquals(50, btn3.getWidth());
        Assert.assertEquals(30, btn3.getHeight());
    }


    @Test
    public void NewLineCreateSecondLine() {
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
        lp2.setNewLine(true);
        btn2.setLayoutParams(lp2);
        layout.addView(btn2);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(70, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.AT_MOST)
        );
        layout.layout(0, 0, 0, 0);

        Assert.assertEquals(1, btn1.getLeft());
        Assert.assertEquals(2, btn1.getTop());
        Assert.assertEquals(1, btn2.getLeft());
        Assert.assertEquals(40 + 2 + 4 + 2, btn2.getTop());

        Assert.assertEquals(30, btn1.getWidth());
        Assert.assertEquals(40, btn1.getHeight());
        Assert.assertEquals(10, btn2.getWidth());
        Assert.assertEquals(20, btn2.getHeight());
    }
}
