package org.apmem.tools.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import org.apmem.tools.example.helpers.ContactsAdapter;
import org.apmem.tools.layouts.FlowLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class FlowLayoutManagerActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flow_layout_manager);

        List<String> models = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            models.add("test " + i);
        }

        RecyclerView rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        ContactsAdapter adapter = new ContactsAdapter(models);
        rvContacts.setAdapter(adapter);
        rvContacts.setLayoutManager(new FlowLayoutManager());
    }
}
