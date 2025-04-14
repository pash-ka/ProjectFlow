package com.hfad.projectflow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Project;
import com.hfad.projectflow.database.ProjectDao;
import com.hfad.projectflow.database.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;


public class CreateProjectActivity extends AppCompatActivity {

    public static final String CURRENT_USER_ID = "userId";

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

        Button btnCreateProject = findViewById(R.id.button_create_project);

        btnCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String projectName = ((EditText) Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(0)).itemView.findViewById(R.id.value)).getText().toString();
                String projectDescription = ((EditText) Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(1)).itemView.findViewById(R.id.value)).getText().toString();

                // Create a new project
                Project project = new Project();
                project.name = projectName.strip();
                project.description = projectDescription;
                project.ownerId = (int) getIntent().getExtras().get(CURRENT_USER_ID);

                AppDatabase db = DatabaseSingleton.getInstance(getApplicationContext());
                ProjectDao projectDao = db.projectDao();
                // Insert the project into the database
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        projectDao.insert(project);
                        // Finish activity and return to the previous screen
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }
}