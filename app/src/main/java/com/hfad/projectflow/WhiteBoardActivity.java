package com.hfad.projectflow;

/*TODO
* maybe having tabs for different sketches,
* with an option of either staying on the same tab
* or creating another one for a new sketch
* ------------------------
* have a navigation drawer  +
* showing thumbnails of the sketches  +
* on click the according sketch is shown, drawer is closed  +
*
* We’re using a group for
these items because the screen for each option is a fragment,
which we’ll display in MainActivity
* <group android:checkableBehavior="single">
You create a navigation drawer by adding a drawer layout to your activity’s
layout as its root element. The drawer layout needs to contain two things:
a view or view group for the activity’s content as its first element, and a
navigation view that defines the drawer as its second
* There are three things we need our activity code to do:
*   Add a drawer toggle +
*   Make the drawer respond to clicks +
*   Close the drawer when the user presses the Back button +
*/

/*TODO
* pencils/brushes
* shapes
* grid on/off +
* place it all somewhere nice, menu
* infinite canvas +
*/


/* TODO
* figures are static, no moving around, just need to keep track of changes to be able to UNDO/REDO
* buttons for drawing:
*   shapes
*   colors
*   stroke size
*   ? maybe fill color
*  */

/*
* need separate paint for every stroke width? or each shape?*/



import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Drawing;
import com.hfad.projectflow.database.DrawingDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhiteBoardActivity extends AppCompatActivity implements NavDrawerAdapter.OnClickListener{

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String EXTRA_CURRENT_USER_ID = "userId";
    private DrawShapeView drawShapeView;
    private LiveData<List<Thumbnail>> thumbnails;
    private NavDrawerAdapter navDrawerAdapter;
    private DrawerLayout drawerLayout;

    public int projectId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);

        setContentView(R.layout.activity_whiteboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_open_drawer, R.string.nav_close_drawer);

        RecyclerView navRecyclerView = findViewById(R.id.nav_recycler_view);

        AppDatabase db = DatabaseSingleton.getInstance(this);
        DrawingDao drawingDao = db.drawingDao();

        WhiteBoardViewModelFactory factory = new WhiteBoardViewModelFactory(drawingDao, projectId, this);
        WhiteBoardViewModel viewModel = new ViewModelProvider(this, factory).get(WhiteBoardViewModel.class);
        navDrawerAdapter = new NavDrawerAdapter(new ArrayList<>(),  WhiteBoardActivity.this);
        navRecyclerView.setLayoutManager(new LinearLayoutManager(WhiteBoardActivity.this));;
        navRecyclerView.setAdapter(navDrawerAdapter);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawShapeView = findViewById(R.id.draw_shape_view);

        drawShapeView.setProjectId(projectId);

        Button brushButton = findViewById(R.id.brush_button);
        brushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawShapeView.setShapeType(ShapeType.NONE);
            }
        });

        Button rectButton = findViewById(R.id.rect_button);
        rectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawShapeView.setShapeType(ShapeType.RECTANGLE);
            }
        });

        Button circleButton = findViewById(R.id.circle_button);
        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawShapeView.setShapeType(ShapeType.CIRCLE);
            }
        });

        Button lineButton = findViewById(R.id.line_button);
        lineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawShapeView.setShapeType(ShapeType.LINE);
            }
        });

        Button diamondButton = findViewById(R.id.diamond_button);
        diamondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawShapeView.setShapeType(ShapeType.DIAMOND);
            }
        });

        ToggleButton gridTB = findViewById(R.id.grid_toggle);
        gridTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                drawShapeView.grid = !drawShapeView.grid;
                drawShapeView.drawOnGrid = !drawShapeView.drawOnGrid;
                drawShapeView.invalidate();
            }
        });

        ToggleButton handTB = findViewById(R.id.hand_toggle);
        handTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                drawShapeView.hand = !drawShapeView.hand;
            }
        });


        LinearLayout stroke_buttons = findViewById(R.id.stroke_buttons);
        for (int i = 0; i < stroke_buttons.getChildCount(); i++) {
            View child = stroke_buttons.getChildAt(i);

            // Check if the child is a Button
            if (child instanceof AppCompatImageButton) {
                // Cast to Button and set OnClickListener
                AppCompatImageButton button = (AppCompatImageButton) child;
                button.setOnClickListener(new StrokeOnClickListener());
            }
        }


        AppCompatImageButton switch_shapes = findViewById(R.id.shapes_switch);
        switch_shapes.setOnClickListener(new SwitchesOnCLickListener());
        /*switch_shapes.setOnClickListener(new View.OnClickListener() {
            boolean visible = false;
            @Override
            public void onClick(View v) {
                if (!visible) {
                    shapeButtons.setVisibility(View.VISIBLE);
                    strokeButtons.setVisibility(View.GONE);
                    visible = true;
                }
                else {
                    shapeButtons.setVisibility(View.GONE);
                    visible = false;
                }
            }
        });*/


        AppCompatImageButton switch_stroke = findViewById(R.id.stroke_switch);
        switch_stroke.setOnClickListener(new SwitchesOnCLickListener());
        /*switch_stroke.setOnClickListener(new View.OnClickListener() {
            boolean visible = false;
            @Override
            public void onClick(View v) {
                if (!visible) {
                    strokeButtons.setVisibility(View.VISIBLE);
                    shapeButtons.setVisibility(View.GONE);
                    visible = true;
                }
                else {
                    strokeButtons.setVisibility(View.GONE);
                    visible = false;
                }
            }
        });*/



    }

    @Override
    protected void onStart() {
        super.onStart();

        drawShapeView.setProjectId(projectId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_whiteboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_save_image){

            try {
                drawShapeView.saveImageToExternalStorage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        } else if (item.getItemId() == R.id.action_new_image) {
            drawShapeView.saveDrawingToStorage();
            drawShapeView.grid = true;
            drawShapeView.newDrawing = true;
            drawShapeView.clearCanvas();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {

        drawShapeView.saveDrawingToStorage();

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        ArrayList<ArrayList<float[]>> points = convertPathToContours(drawShapeView.getPath());
        //System.out.println(points);
        outState.putSerializable("mainPathPoints", points);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<ArrayList<float[]>> mainPathPoints = (ArrayList<ArrayList<float[]>>) savedInstanceState.getSerializable("mainPathPoints");

        // Recreate the paths from the stored points
        drawShapeView.setPath(convertContoursToPath(Objects.requireNonNull(mainPathPoints)));

    }

    private ArrayList<ArrayList<float[]>> convertPathToContours(Path path) {
        ArrayList<ArrayList<float[]>> contours = new ArrayList<>();
        PathMeasure pathMeasure = new PathMeasure(path, false);
        float[] coordinates = new float[2];

        // Iterate over all contours in the Path
        do {
            ArrayList<float[]> contourPoints = new ArrayList<>();
            float pathLength = pathMeasure.getLength();

            // Extract points for the current contour
            for (float distance = 0; distance < pathLength; distance += 5) { // Adjust step size as needed
                pathMeasure.getPosTan(distance, coordinates, null);
                contourPoints.add(new float[]{coordinates[0], coordinates[1]});
            }

            if (!contourPoints.isEmpty()) {
                contours.add(contourPoints); // Save the current contour
            }
        } while (pathMeasure.nextContour()); // Move to the next contour if available

        return contours;
    }


    private Path convertContoursToPath(ArrayList<ArrayList<float[]>> contours) {
        Path path = new Path();

        for (ArrayList<float[]> points : contours) {
            if (!points.isEmpty()) {
                float[] startPoint = points.get(0);
                path.moveTo(startPoint[0], startPoint[1]); // Start a new contour

                for (int i = 1; i < points.size(); i++) {
                    float[] point = points.get(i);
                    path.lineTo(point[0], point[1]); // Continue the contour
                }
            }
        }

        return path;
    }


    @Override
    public void onItemClick(int position) {
        Drawing thumbnail = navDrawerAdapter.getThumbnails().get(position);
        drawShapeView.saveDrawingToStorage();
        drawShapeView.newDrawing = false;
        drawShapeView.setBitmap(drawShapeView.convertByteArrayToBitmap(thumbnail.getDrawingData()));
        drawShapeView.invalidate();
        drawShapeView.setDrawingId(thumbnail.getId());
        drawerLayout.closeDrawer(GravityCompat.START);
    }


    public NavDrawerAdapter getNavDrawerAdapter(){
        return navDrawerAdapter;
    }

    private class SwitchesOnCLickListener implements View.OnClickListener {

        AppCompatImageButton strokeSwitch = findViewById(R.id.stroke_switch);
        AppCompatImageButton shapeSwitch = findViewById(R.id.shapes_switch);

        LinearLayout shapeButtons = findViewById(R.id.shape_buttons);
        LinearLayout strokeButtons = findViewById(R.id.stroke_buttons);


        @Override
        public void onClick(View v) {
            if (v == strokeSwitch){
                int visible = strokeButtons.getVisibility();
                if (visible == View.GONE){
                    strokeButtons.setVisibility(View.VISIBLE);
                    shapeButtons.setVisibility(View.GONE);
                }
                else {
                    strokeButtons.setVisibility(View.GONE);
                }
            }
            else if (v == shapeSwitch){
                int visible = shapeButtons.getVisibility();
                if (visible == View.GONE){
                    shapeButtons.setVisibility(View.VISIBLE);
                    strokeButtons.setVisibility(View.GONE);
                }
                else {
                    shapeButtons.setVisibility(View.GONE);
                }
            }
        }
    }

    private class StrokeOnClickListener implements View.OnClickListener {

        AppCompatImageButton strokeSmall = findViewById(R.id.stroke_small);
        AppCompatImageButton strokeDefault = findViewById(R.id.stroke_default);
        AppCompatImageButton strokeNormal = findViewById(R.id.stroke_normal);
        AppCompatImageButton strokeBig = findViewById(R.id.stroke_big);

        LinearLayout strokeButtons = findViewById(R.id.stroke_buttons);

        @Override
        public void onClick(View v) {
            if (v == strokeSmall){
                drawShapeView.setPaintStrokeWidth(7f);
            } else if (v == strokeDefault) {
                drawShapeView.setPaintStrokeWidth(10f);
            } else if (v == strokeNormal) {
                drawShapeView.setPaintStrokeWidth(13f);
            } else if (v == strokeBig) {
                drawShapeView.setPaintStrokeWidth(16f);
            }

        }

    }
}

