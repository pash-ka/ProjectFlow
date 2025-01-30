package com.hfad.projectflow;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Project;
import com.hfad.projectflow.database.ProjectDao;
import com.hfad.projectflow.database.User;
import com.hfad.projectflow.database.UserDao;

import java.util.concurrent.Executors;


public class DocumentationActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String CURRENT_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        EditText descriptionView = findViewById(R.id.doc_text);
        int projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);
        int currentUserId = (int) getIntent().getExtras().get(CURRENT_USER_ID);

        TextView userNameTextView = findViewById(R.id.userName);
        TextView projectNameTextView = findViewById(R.id.projectName);

        AppDatabase db = DatabaseSingleton.getInstance(getBaseContext());
        ProjectDao projectDao = db.projectDao();
        UserDao userDao = db.userDao();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                User user = userDao.getUserById(currentUserId);
                Project project = projectDao.getProjectById(projectId);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userNameTextView.setText(user.name);
                        projectNameTextView.setText(project.name);
                        descriptionView.setText(project.description);
                    }
                });
            }
        });

        /*AppDatabase db = DatabaseSingleton.getInstance(getBaseContext());
        view.setText(db.projectDao().getAllProjects().get(projectId).description);*/
    }
}