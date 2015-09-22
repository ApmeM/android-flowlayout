package org.apmem.tools.layouts;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import junit.framework.Assert;
import org.apmem.tools.example.MyActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class FlowLayoutGravityTests {
    TestActivity activity = Robolectric.setupActivity(TestActivity.class);

    @Test
    public void MoveChildToRightBottomCorner() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.RIGHT | Gravity.BOTTOM);

        final Button btn = new Button(activity);
        FlowLayout.LayoutParams lp = new FlowLayout.LayoutParams(30, 40);
        lp.setMargins(1, 2, 3, 4);
        btn.setLayoutParams(lp);
        layout.addView(btn);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(50, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(60, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals(16, lp.getX());
        Assert.assertEquals(14, lp.getY());
    }
    @Test
    public void MoveChildToCenter() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.CENTER);

        final Button btn = new Button(activity);
        FlowLayout.LayoutParams lp = new FlowLayout.LayoutParams(30, 40);
        lp.setMargins(1, 2, 3, 4);
        btn.setLayoutParams(lp);
        layout.addView(btn);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(50, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(60, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals(8, lp.getX());
        Assert.assertEquals(7, lp.getY());
    }

    @Test
    public void ChildLessThenSizeWithFillGravity_IncreaseChildSize() {
        final FlowLayout layout = new FlowLayout(activity.getApplicationContext());
        layout.setGravity(Gravity.FILL);

        final Button btn = new Button(activity);
        FlowLayout.LayoutParams lp = new FlowLayout.LayoutParams(30, 40);
        lp.setMargins(1, 2, 3, 4);
        btn.setLayoutParams(lp);
        layout.addView(btn);

        layout.measure(
                View.MeasureSpec.makeMeasureSpec(50, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(60, View.MeasureSpec.EXACTLY)
        );

        Assert.assertEquals(46, lp.getLength());
        Assert.assertEquals(54, lp.getThickness());
    }
}
