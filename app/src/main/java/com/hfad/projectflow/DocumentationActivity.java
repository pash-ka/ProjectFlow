package com.hfad.projectflow;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DocumentationActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        TextView view = findViewById(R.id.doc_text);
        int projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);

        view.setText(String.valueOf(projectId));
    }
}