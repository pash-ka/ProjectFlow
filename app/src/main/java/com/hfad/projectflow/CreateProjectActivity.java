package com.hfad.projectflow;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class CreateProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<InputField> inputFieldList = new ArrayList<>();
        inputFieldList.add(new InputField("Project Name"));
        inputFieldList.add(new InputField("Description"));

        InputFieldAdapter inputFieldAdapter = new InputFieldAdapter(inputFieldList);
        recyclerView.setAdapter(inputFieldAdapter);
    }
}