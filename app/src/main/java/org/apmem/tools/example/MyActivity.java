package org.apmem.tools.example;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apmem.tools.layouts.FlowLayout;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FlowLayout layout = (FlowLayout)this.findViewById(R.id.flowLayout);

        layout.setOrientation(FlowLayout.VERTICAL);

        TextView textView;
        textView = new TextView(this);
        textView.setLayoutParams(new FlowLayout.LayoutParams(100, 100));
        textView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        textView.setText("appearance");
        layout.addView(textView, 0);

        textView = new TextView(this);
        textView.setLayoutParams(new FlowLayout.LayoutParams(100, 100));
        textView.setText("appearance");
        layout.addView(textView, 0);
    }
}
