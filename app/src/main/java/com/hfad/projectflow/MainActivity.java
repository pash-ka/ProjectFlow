package com.hfad.projectflow;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

    private AppDatabase db;
    private long userId;
    private List<Project> projectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase db = DatabaseSingleton.getInstance(getApplicationContext());
        ProjectDao projectDao = db.projectDao();

        /*  try to get user from db
            if there is one -> egt his id to use it in intent
            if there is no -> create one
        */
        UserDao userDao = db.userDao();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (userDao.getAllUsers().isEmpty()){
                    User user = new User();
                    user.name = "Pasha";
                    user.email = "pasha@gmail.com";
                    userId = userDao.insert(user);
                }
                else {
                    userId = userDao.getAllUsers().get(0).getId();

                }
            }
        });

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

    }

    @Override
    public void onResume(){
        AppDatabase db = DatabaseSingleton.getInstance(getApplicationContext());
        ProjectDao projectDao = db.projectDao();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                projectList = projectDao.getAllProjects();
                TextView textView = findViewById(R.id.text_placeholder);
                if (projectList.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(R.string.no_projects);
                        }
                    });
                }
                else {
                    /*System.out.println(projectList.get(0).name);
                    System.out.println(projectList.get(0).id);*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setVisibility(TextView.GONE);
                        }
                    });

                }
            }
        });
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if (item.getItemId() == R.id.action_create_project){
            Intent intent = new Intent(this, CreateProjectActivity.class);
            intent.putExtra(CreateProjectActivity.CURRENT_USER_ID, (int) userId);

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
        // SEND WRONG ID WITH THE INTENT. NEED TO GET THE ID FROM DB
        intent.putExtra(WorkActivity.EXTRA_PROJECT_ID, (int) id);
        intent.putExtra(WorkActivity.CURRENT_USER_ID, (int) userId);
        System.out.println(userId);
        startActivity(intent);
    }
}