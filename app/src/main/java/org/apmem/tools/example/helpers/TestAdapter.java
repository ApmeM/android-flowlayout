package org.apmem.tools.example.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.apmem.tools.example.R;
import org.apmem.tools.layouts.FlowLayoutManager;

import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    private List<TestModel> models;

    public TestAdapter(List<TestModel> models) {
        this.models = models;
    }

    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.inflating_layout, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(TestAdapter.ViewHolder viewHolder, int position) {
        TestModel model = this.models.get(position);

        TextView textView = viewHolder.nameTextView;
        textView.setText(model.text);
        FlowLayoutManager.LayoutParams params = new FlowLayoutManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setNewLine(model.newLine);
        textView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.text_view);
        }
    }
}