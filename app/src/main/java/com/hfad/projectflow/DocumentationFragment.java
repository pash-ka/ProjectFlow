package com.hfad.projectflow;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Project;
import com.hfad.projectflow.database.ProjectDao;

import java.util.concurrent.Executors;

public class DocumentationFragment extends Fragment {

    private static final String ARG_CURRENT_USER_ID = "current_user_id";
    private static final String ARG_PROJECT_ID = "project_id";

    private int userId;
    private int projectId;

    private View root;

    private ProjectDao projectDao;
    private AppDatabase db;

    public static DocumentationFragment newInstance(int userId, int projectId) {
        DocumentationFragment fragment = new DocumentationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_USER_ID, userId);
        args.putInt(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(ARG_CURRENT_USER_ID);
            projectId = getArguments().getInt(ARG_PROJECT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_documentation, container, false);

        db = DatabaseSingleton.getInstance(getActivity());
        projectDao = db.projectDao();

        //Toolbar toolbar = root.findViewById(R.id.toolbar);
        //((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        EditText descriptionView = root.findViewById(R.id.doc_text);
        TextView projectNameTextView = root.findViewById(R.id.projectName);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Project project = projectDao.getProjectById(projectId);

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        projectNameTextView.setText(project.name);
                        descriptionView.setText(project.description);
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onPause(){
        super.onPause();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                EditText docText = root.findViewById(R.id.doc_text);
                projectDao.updateProjectDescription(projectId, docText.getText().toString());
            }
        });
    }
}