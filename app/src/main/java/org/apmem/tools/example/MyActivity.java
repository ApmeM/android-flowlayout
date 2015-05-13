package org.apmem.tools.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;

public class MyActivity extends Activity {

    private FlowLayout flow;
    private Button unwrappedButton;
    private Button wrappedButton;
    private EditText edit;

    private void bindViews() {
        flow = (FlowLayout) findViewById(R.id.flow_container);
        unwrappedButton = (Button) findViewById(R.id.button_unwrapped);
        wrappedButton = (Button) findViewById(R.id.button_wrapped);
        edit = (EditText) findViewById(R.id.edit_text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bindViews();

        unwrappedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View child = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.child, flow, false);
                TextView text = (TextView) child.findViewById(R.id.child_text);
                text.setText(edit.getText());
                flow.addView(child);
            }
        });
        wrappedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View child = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.child_wrapped, flow, false);
                TextView text = (TextView) child.findViewById(R.id.child_text);
                text.setText(edit.getText());
                flow.addView(child);
            }
        });

    }
}
