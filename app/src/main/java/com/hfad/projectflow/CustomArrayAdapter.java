package com.hfad.projectflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    private boolean isLargeScreen;

    public CustomArrayAdapter(@NonNull Context context, @NonNull List<String> objects, boolean isLargeScreen) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.isLargeScreen = isLargeScreen;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        String text = getItem(position);
        textView.setText(text);

        // Set text size programmatically
        if (isLargeScreen) textView.setTextSize(30);
        else textView.setTextSize(20);
        textView.setPadding(5, 10, 5, 10);

        return convertView;
    }
}
