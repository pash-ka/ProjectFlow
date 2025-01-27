package com.hfad.projectflow;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Project;
import com.hfad.projectflow.database.ProjectDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;


public class ProjectList extends ListFragment{

    static interface Listener{
        void itemClicked(long id);
    }

    private Listener listener;
    private List<String> names = new ArrayList<>();
    private List<Project> projectList;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*String[] names = new String[2];
        names[0] = "Project1";
        names[1] = "Project2";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                inflater.getContext(),
                android.R.layout.simple_list_item_1,
                names);
        setListAdapter(arrayAdapter);*/

        AppDatabase db = DatabaseSingleton.getInstance(requireContext());
        ProjectDao projectDao = db.projectDao();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                projectList = projectDao.getAllProjects();
                for (Project p: projectList){
                    names.add(p.name);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        arrayAdapter = new ArrayAdapter<>(
                                getActivity().getLayoutInflater().getContext(),
                                android.R.layout.simple_list_item_1,
                                names);

                        setListAdapter(arrayAdapter);
                    }
                });
            }
        });

        // Inflate the layout for this fragment
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.listener = (Listener) context;

    }

    @Override
    public void onListItemClick(ListView listView, View itemView, int position, long id){
        if (listener != null){
            listener.itemClicked(id);
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Project project = projectList.get(position);
                                names.remove(position);
                                arrayAdapter.notifyDataSetChanged();
                                Executors.newSingleThreadExecutor().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppDatabase db = DatabaseSingleton.getInstance(getActivity().getApplicationContext());
                                        ProjectDao projectDao = db.projectDao();
                                        projectDao.deleteProject(project);

                                    }
                                });

                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            }
        });
    }
}