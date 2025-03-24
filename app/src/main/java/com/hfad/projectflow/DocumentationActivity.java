package com.hfad.projectflow;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Project;
import com.hfad.projectflow.database.ProjectDao;
import com.hfad.projectflow.database.User;
import com.hfad.projectflow.database.UserDao;

import java.util.Objects;
import java.util.concurrent.Executors;


public class DocumentationActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String EXTRA_CURRENT_USER_ID = "userId";

    AppDatabase db = DatabaseSingleton.getInstance(getBaseContext());
    ProjectDao projectDao = db.projectDao();
    UserDao userDao = db.userDao();
    int projectId;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);
        currentUserId = (int) getIntent().getExtras().get(EXTRA_CURRENT_USER_ID);

        EditText descriptionView = findViewById(R.id.doc_text);
        TextView projectNameTextView = findViewById(R.id.projectName);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Project project = projectDao.getProjectById(projectId);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        projectNameTextView.setText(project.name);
                        descriptionView.setText(project.description);
                    }
                });
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                EditText docText = findViewById(R.id.doc_text);
                projectDao.updateProjectDescription(projectId, docText.getText().toString());
            }
        });
    }
}