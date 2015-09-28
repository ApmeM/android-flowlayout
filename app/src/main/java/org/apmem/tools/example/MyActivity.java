package org.apmem.tools.example;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MyActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);

        TabHost tabHost = super.getTabHost();

        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("tag1");
        tabSpec1.setIndicator(getResources().getString(R.string.flow_layout_activity));
        tabSpec1.setContent(new Intent(this, FlowLayoutActivity.class));
        tabHost.addTab(tabSpec1);

        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("tag2");
        tabSpec2.setIndicator(getResources().getString(R.string.flow_layout_manager_activity));
        tabSpec2.setContent(new Intent(this, FlowLayoutManagerActivity.class));
        tabHost.addTab(tabSpec2);
    }
}
