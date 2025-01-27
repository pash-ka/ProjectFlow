package com.hfad.projectflow;

// InputFieldAdapter.java (Java)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InputFieldAdapter extends RecyclerView.Adapter<InputFieldAdapter.ViewHolder> {

    private List<InputField> inputFieldList;

    public InputFieldAdapter(List<InputField> inputFields) {
        this.inputFieldList = inputFields;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_input_field, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InputField inputField = inputFieldList.get(position);
        holder.label.setText(inputField.getLabel());
        holder.value.setText(inputField.getValue());

        holder.value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    inputField.setValue(holder.value.getText().toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return inputFieldList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView label;
        public EditText value;

        public ViewHolder(View view) {
            super(view);
            label = view.findViewById(R.id.label);
            value = view.findViewById(R.id.value);
        }
    }
}
