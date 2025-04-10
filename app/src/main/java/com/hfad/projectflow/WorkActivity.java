package com.hfad.projectflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;


public class WorkActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String EXTRA_CURRENT_USER_ID = "userId";
    int projectId;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            projectId = (int) intent.getExtras().get(EXTRA_PROJECT_ID);
            currentUserId = (int) intent.getExtras().get(EXTRA_CURRENT_USER_ID);
            System.out.println("intent: " + projectId + " : " + currentUserId);
        }
        else {
            SharedPreferences preferences = getSharedPreferences("ActivityAState", MODE_PRIVATE);
            projectId = preferences.getInt("projectId", 1);
            currentUserId = preferences.getInt("currentUserId", 1);
            System.out.println(projectId + " : " + currentUserId);
        }
        /*
        projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);
        currentUserId = (int) getIntent().getExtras().get(EXTRA_CURRENT_USER_ID);
        */

        // if change to tab -> need to have fragments, not activities!!

        TextView docView = findViewById(R.id.documentation);
        docView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), "Opening documentation", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(WorkActivity.this, DocumentationActivity.class);

                intent.putExtra(DocumentationActivity.EXTRA_PROJECT_ID, projectId);
                intent.putExtra(DocumentationActivity.EXTRA_CURRENT_USER_ID, currentUserId);
                //System.out.println(currentUserId);
                startActivity(intent);
            }
        });

        TextView whiteBoardView = findViewById(R.id.whiteboard);
        whiteBoardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkActivity.this, WhiteBoardActivity.class);

                intent.putExtra(WhiteBoardActivity.EXTRA_PROJECT_ID, projectId);
                intent.putExtra(WhiteBoardActivity.EXTRA_CURRENT_USER_ID, currentUserId);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getSharedPreferences("ActivityAState", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Save state variables
        editor.putInt("projectId", projectId);
        editor.putInt("currentUserId", currentUserId);

        editor.apply(); // Apply changes
    }

}