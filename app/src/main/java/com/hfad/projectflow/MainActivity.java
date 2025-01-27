package com.hfad.projectflow;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Project;
import com.hfad.projectflow.database.ProjectDao;
import com.hfad.projectflow.database.User;
import com.hfad.projectflow.database.UserDao;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ProjectList.Listener{

    protected AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*int projectCount = Project.projects.length;
        if (projectCount == 0){
            textView.setText(R.string.no_projects);
        }
        else {
            textView.setVisibility(View.GONE);
        }*/
        /*db = DatabaseSingleton.getInstance(getApplicationContext());
        Log.d("DatabaseCheck", "Database created successfully");
        UserDao userDao = db.userDao();
        ProjectDao projectDao = db.projectDao();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                User me = new User();
                me.name = "Pasha";
                me.email = "pasha@gmail.com";
                userDao.insert(me);
                long userId = userDao.insert(me);
                Project myPr = new Project();
                myPr.name = "Uni";
                myPr.description = "Standard uni project";
                myPr.ownerId = (int) userId;
                projectDao.insert(myPr);
            }
        });*/

        //long userId = userDao.insert(me);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if (item.getItemId() == R.id.action_create_order){
            Intent intent = new Intent(this, CreateProjectActivity.class);
            startActivity(intent);
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void itemClicked(long id) {
        Intent intent = new Intent(this, WorkActivity.class);
        intent.putExtra(WorkActivity.EXTRA_PROJECT_ID, (int) id);

        AppDatabase db = DatabaseSingleton.getInstance(getApplicationContext());
        ProjectDao projectDao = db.projectDao();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                List<Project> projectList = projectDao.getAllProjects();
                String[] names = new String[projectList.size()];
                for (int i=0; i< names.length; i++){
                    names[i] = projectList.get(i).name;
                }
                System.out.println(Arrays.toString(names));
            }
        });

        startActivity(intent);
    }
}