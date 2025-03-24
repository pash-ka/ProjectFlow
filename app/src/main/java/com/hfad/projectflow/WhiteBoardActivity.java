package com.hfad.projectflow;

/*TODO
* maybe having tabs for different sketches,
* with an option of either staying on the same tab
* or creating another one for a new sketch
*/

/*TODO
* pencils/brushes
* shapes
* grid on/off +
* place it all somewhere nice, menu
* infinite canvas +
*/



import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class WhiteBoardActivity extends AppCompatActivity{

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String EXTRA_CURRENT_USER_ID = "userId";
    private DrawShapeView drawShapeView;

    public int projectId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_whiteboard);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        drawShapeView = findViewById(R.id.draw_shape_view);
        projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);
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
        Log.d("Button", "Background: " + rectButton.getBackgroundTintMode());


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

}