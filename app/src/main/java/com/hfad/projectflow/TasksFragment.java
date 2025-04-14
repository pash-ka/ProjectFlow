package com.hfad.projectflow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
* need a list of tasks
* adding new ones
* checkboxes for each one
* maybe grouping them
* setting deadlines -> notifications
* tracking dates of adding and completion for overview
* */


public class TasksFragment extends Fragment {

    private static final String ARG_CURRENT_USER_ID = "current_user_id";
    private static final String ARG_PROJECT_ID = "project_id";

    private int userId;
    private int projectId;

    public TasksFragment() {
        // Required empty public constructor
    }


    public static TasksFragment newInstance(int userId, int projectId) {
        TasksFragment fragment = new TasksFragment();
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
        View root = inflater.inflate(R.layout.fragment_tasks, container, false);


        // Inflate the layout for this fragment
        return root;

    }
}