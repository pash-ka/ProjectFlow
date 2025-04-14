package com.hfad.projectflow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.util.Objects;


public class WorkActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String EXTRA_CURRENT_USER_ID = "userId";
    private int projectId;
    private int currentUserId;

    private ViewPager2 pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        WorkSectionsPagerAdapter pagerAdapter = new WorkSectionsPagerAdapter(this, currentUserId, projectId);
        pager = findViewById(R.id.pager);
        pager.setUserInputEnabled(false);
        pager.setAdapter(pagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(pager);
        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            tab.setText(pagerAdapter.getPageTitle(position));
        }).attach();

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position != 1) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + 1);
                    if (fragment instanceof WhiteBoardFragment){
                        ((WhiteBoardFragment) fragment).getDrawerLayout().closeDrawer(GravityCompat.START);
                    }
                }

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

        // Apply changes
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_whiteboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int currentPosition = pager.getCurrentItem();
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + currentPosition);

        if (currentFragment == getSupportFragmentManager().findFragmentByTag("f" + 1) && currentFragment != null) {

            DrawShapeView drawShapeView = currentFragment.requireView().findViewById(R.id.draw_shape_view);
            if (item.getItemId() == R.id.action_save_image) {

                try {
                    drawShapeView.saveImageToExternalStorage();
                    Toast.makeText(getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            } else if (item.getItemId() == R.id.action_new_image) {
                drawShapeView.saveDrawingToStorage();
                drawShapeView.newDrawing = true;
                drawShapeView.clearCanvas();
                drawShapeView.resetBounds();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}