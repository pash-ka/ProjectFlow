package com.hfad.projectflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements ProjectList.Listener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.text_placeholder);
        int projectCount = Project.projects.length;
        if (projectCount == 0){
            textView.setText(R.string.no_projects);
        }
    }

    @Override
    public void itemClicked(long id) {
        Intent intent = new Intent(this, WorkActivity.class);
        intent.putExtra(WorkActivity.EXTRA_PROJECT_ID, (int) id);
        startActivity(intent);
    }
}