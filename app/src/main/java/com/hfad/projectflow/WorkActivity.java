package com.hfad.projectflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class WorkActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        int projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);

        TextView view = findViewById(R.id.documentation);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Opening documentation", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(WorkActivity.this, DocumentationActivity.class);
                //Toast.makeText(v.getContext(), "created", Toast.LENGTH_SHORT).show();
                intent.putExtra(DocumentationActivity.EXTRA_PROJECT_ID, projectId);
                //Toast.makeText(v.getContext(), "created", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });
    }
}