package org.apmem.tools.example;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import org.apmem.tools.example.helpers.TestAdapter;
import org.apmem.tools.example.helpers.TestModel;
import org.apmem.tools.layouts.FlowLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class FlowLayoutManagerActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flow_layout_manager);

        List<TestModel> models = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            TestModel model = new TestModel();
            model.text = "test " + i;
            model.newLine = i == 7;
            models.add(model);
        }

        RecyclerView rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        TestAdapter adapter = new TestAdapter(models);
        rvContacts.setAdapter(adapter);
        rvContacts.setLayoutManager(new FlowLayoutManager());
    }
}
