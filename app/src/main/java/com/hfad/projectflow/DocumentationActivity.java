package com.hfad.projectflow;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Project;


public class DocumentationActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        TextView view = findViewById(R.id.doc_text);
        int projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);


        /*AppDatabase db = DatabaseSingleton.getInstance(getBaseContext());
        view.setText(db.projectDao().getAllProjects().get(projectId).description);*/
    }
}