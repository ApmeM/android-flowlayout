package org.apmem.tools.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

        final FlowLayout layout = (FlowLayout) this.findViewById(R.id.flowLayout);

        layout.setOrientation(FlowLayout.HORIZONTAL);
        layout.setGravity(Gravity.FILL);

        final Button buttonOrientation = new Button(this);
        buttonOrientation.setLayoutParams(new FlowLayout.LayoutParams(100, 100));
        buttonOrientation.setTextSize(8);
        buttonOrientation.setText("Switch Orientation (Current: Horizontal)");
        buttonOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setOrientation(1 - layout.getOrientation());
                buttonOrientation.setText(layout.getOrientation() == FlowLayout.HORIZONTAL ?
                        "Switch Orientation (Current: Horizontal)" :
                        "Switch Orientation (Current: Vertical)");
            }
        });
        layout.addView(buttonOrientation, 0);

        final Button buttonGravity = new Button(this);
        buttonGravity.setLayoutParams(new FlowLayout.LayoutParams(100, 100));
        buttonGravity.setTextSize(8);
        buttonGravity.setText("Switch Gravity (Current: FILL)");
        buttonGravity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (layout.getGravity()) {
                    case Gravity.LEFT | Gravity.TOP:
                        layout.setGravity(Gravity.FILL);
                        buttonGravity.setText("Switch Gravity (Current: FILL)");
                        break;
                    case Gravity.FILL:
                        layout.setGravity(Gravity.CENTER);
                        buttonGravity.setText("Switch Gravity (Current: CENTER)");
                        break;
                    case Gravity.CENTER:
                        layout.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                        buttonGravity.setText("Switch Gravity (Current: RIGHT | BOTTOM)");
                        break;
                    case Gravity.RIGHT | Gravity.BOTTOM:
                        layout.setGravity(Gravity.LEFT | Gravity.TOP);
                        buttonGravity.setText("Switch Gravity (Current: LEFT | TOP)");
                        break;
                }
            }
        });
        layout.addView(buttonGravity, 0);
    }
}
