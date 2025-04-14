package com.hfad.projectflow;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hfad.projectflow.database.AppDatabase;
import com.hfad.projectflow.database.DatabaseSingleton;
import com.hfad.projectflow.database.Drawing;
import com.hfad.projectflow.database.DrawingDao;
import com.hfad.projectflow.database.ProjectDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class WhiteBoardFragment extends Fragment implements NavDrawerAdapter.OnClickListener{

    private static final String ARG_CURRENT_USER_ID = "current_user_id";
    private static final String ARG_PROJECT_ID = "project_id";

    private int userId;
    private int projectId;

    private View root;

    private ProjectDao projectDao;
    private AppDatabase db;

    private DrawShapeView drawShapeView;
    private NavDrawerAdapter navDrawerAdapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private WhiteBoardViewModel viewModel;

    public WhiteBoardFragment() {
        // Required empty public constructor
    }

    public static WhiteBoardFragment newInstance(int userId, int projectId) {
        WhiteBoardFragment fragment = new WhiteBoardFragment();
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
        root = inflater.inflate(R.layout.fragment_white_board, container, false);
        if (savedInstanceState != null) drawShapeView.setBitmap(savedInstanceState.getParcelable("bitmap"));


        drawShapeView = root.findViewById(R.id.draw_shape_view);

        drawShapeView.setProjectId(projectId);

        navDrawerAdapter = new NavDrawerAdapter(new ArrayList<>(), WhiteBoardFragment.this);
        AppDatabase db = DatabaseSingleton.getInstance(requireActivity());
        DrawingDao drawingDao = db.drawingDao();

        // updating drawer after all the thumbnails are loaded
        WhiteBoardViewModelFactory factory = new WhiteBoardViewModelFactory(drawingDao, projectId, this);
        viewModel = new ViewModelProvider(this, factory).get(WhiteBoardViewModel.class);


        ToggleButton gridTB = root.findViewById(R.id.grid_toggle);
        gridTB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            drawShapeView.grid = !drawShapeView.grid;
            drawShapeView.invalidate();
        });

        ToggleButton handTB = root.findViewById(R.id.hand_toggle);
        handTB.setOnCheckedChangeListener((buttonView, isChecked) -> drawShapeView.hand = !drawShapeView.hand);


        LinearLayout stroke_buttons = root.findViewById(R.id.stroke_buttons);
        for (int i = 0; i < stroke_buttons.getChildCount(); i++) {
            View child = stroke_buttons.getChildAt(i);

            // Check if the child is a Button
            if (child instanceof AppCompatImageButton) {
                // Cast to Button and set OnClickListener
                AppCompatImageButton button = (AppCompatImageButton) child;
                button.setOnClickListener(new StrokeOnClickListener());
            }
        }
        LinearLayout shape_buttons = root.findViewById(R.id.shape_buttons);
        for (int i = 0; i < shape_buttons.getChildCount(); i++) {
            View child = shape_buttons.getChildAt(i);
            // Check if the child is a Button
            if (child instanceof AppCompatImageButton) {
                // Cast to Button and set OnClickListener
                AppCompatImageButton button = (AppCompatImageButton) child;
                button.setOnClickListener(new ShapeOnClickListener());
            }
        }

        AppCompatImageButton switch_shapes = root.findViewById(R.id.shapes_switch);
        switch_shapes.setOnClickListener(new SwitchesOnCLickListener());

        AppCompatImageButton switch_stroke = root.findViewById(R.id.stroke_switch);
        switch_stroke.setOnClickListener(new SwitchesOnCLickListener());

        AppCompatImageButton eraserButton = root.findViewById(R.id.eraser_button);
        eraserButton.setOnClickListener(new SwitchesOnCLickListener());

        AppCompatImageButton colorButton = root.findViewById(R.id.color_switch);
        colorButton.setOnClickListener(new SwitchesOnCLickListener());
        setUpColorDrawables();
        // Inflate the layout for this fragment
        return root;
    }

    @Override
    public void onItemClick(int position) {
        Drawing thumbnail = navDrawerAdapter.getThumbnails().get(position);

        drawShapeView.saveDrawingToStorage();

        drawShapeView.clearCanvas();
        drawShapeView.resetBounds();

        drawShapeView.newDrawing = false;
        drawShapeView.setBitmap(drawShapeView.convertByteArrayToBitmap(thumbnail.getDrawingData()));
        drawShapeView.setDrawingId(thumbnail.getId());

        drawerLayout.closeDrawer(GravityCompat.START);
        drawShapeView.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();

        drawShapeView.setProjectId(projectId);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        drawerLayout = root.findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        toggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar,
                R.string.nav_open_drawer, R.string.nav_close_drawer);

        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);

        RecyclerView navRecyclerView = root.findViewById(R.id.nav_recycler_view);

        AppDatabase db = DatabaseSingleton.getInstance(requireActivity());
        DrawingDao drawingDao = db.drawingDao();

        viewModel.observeData(drawingDao, projectId, this);

        navRecyclerView.setLayoutManager(new LinearLayoutManager(getContext())); //might cause issues
        navRecyclerView.setAdapter(navDrawerAdapter);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        drawShapeView.resetView();
    }

    @Override
    public void onPause() {
        super.onPause();

        float currentProgress = toggle.getDrawerArrowDrawable().getProgress();
        if (currentProgress != 0f) {
            // Animate from currentProgress to 0f
            ValueAnimator animator = ValueAnimator.ofFloat(currentProgress, 0f);
            animator.setDuration(50); // Faster transition (150ms)
            animator.addUpdateListener(animation -> {
                float progress = (float) animation.getAnimatedValue();
                toggle.getDrawerArrowDrawable().setProgress(progress);
            });
            animator.start();
        }

        drawerLayout.removeDrawerListener(toggle);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawShapeView.saveDrawingToStorage();
        drawShapeView.clearCanvas();
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //ArrayList<ArrayList<float[]>> points = convertPathToContours(drawShapeView.getPath());
        Bitmap bitmap = drawShapeView.getBitmap();
        //System.out.println(points);
        //outState.putSerializable("mainPathPoints", points);
        outState.putParcelable("bitmap", bitmap);
        super.onSaveInstanceState(outState);
    }

    /*@Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ArrayList<ArrayList<float[]>> mainPathPoints = (ArrayList<ArrayList<float[]>>) savedInstanceState.getSerializable("mainPathPoints");

        // Recreate the paths from the stored points
        //drawShapeView.setPath(convertContoursToPath(Objects.requireNonNull(mainPathPoints)));
        drawShapeView.setBitmap(savedInstanceState.getParcelable("bitmap"));
    }*/

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

    public NavDrawerAdapter getNavDrawerAdapter() {
        return navDrawerAdapter;
    }

    private void setUpColorDrawables() {
        Drawable baseDrawable = ContextCompat.getDrawable(requireActivity().getApplicationContext(), R.drawable.baseline_circle_24);
        int[] colors = new int[]{
                Color.BLACK,
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW
        };

        LinearLayout color_buttons = root.findViewById(R.id.color_buttons);
        for (int i = 0; i < color_buttons.getChildCount(); i++) {
            View child = color_buttons.getChildAt(i);

            // Check if the child is a Button
            if (child instanceof AppCompatImageButton) {
                // Cast to Button and set OnClickListener
                AppCompatImageButton button = (AppCompatImageButton) child;
                assert baseDrawable != null;
                Drawable buttonDrawable = Objects.requireNonNull(baseDrawable.getConstantState()).newDrawable().mutate();
                buttonDrawable.setTint(colors[i]);
                button.setImageDrawable(buttonDrawable);

                button.setOnClickListener(new ColorOnClickListener());
            }
        }
    }

    private class StrokeOnClickListener implements View.OnClickListener {

        AppCompatImageButton strokeSmall = root.findViewById(R.id.stroke_small);
        AppCompatImageButton strokeDefault = root.findViewById(R.id.stroke_default);
        AppCompatImageButton strokeNormal = root.findViewById(R.id.stroke_normal);
        AppCompatImageButton strokeBig = root.findViewById(R.id.stroke_big);

        //LinearLayout strokeButtons = findViewById(R.id.stroke_buttons);

        @Override
        public void onClick(View v) {
            if (v == strokeSmall) {
                drawShapeView.setPaintStrokeWidth(5f);
            } else if (v == strokeDefault) {
                drawShapeView.setPaintStrokeWidth(10f);
            } else if (v == strokeNormal) {
                drawShapeView.setPaintStrokeWidth(20f);
            } else if (v == strokeBig) {
                drawShapeView.setPaintStrokeWidth(40f);
            }
        }
    }

    private class SwitchesOnCLickListener implements View.OnClickListener {

        AppCompatImageButton strokeSwitch = root.findViewById(R.id.stroke_switch);
        AppCompatImageButton shapeSwitch = root.findViewById(R.id.shapes_switch);
        AppCompatImageButton eraserButton = root.findViewById(R.id.eraser_button);
        AppCompatImageButton colorButton = root.findViewById(R.id.color_switch);

        LinearLayout shapeButtons = root.findViewById(R.id.shape_buttons);
        LinearLayout strokeButtons = root.findViewById(R.id.stroke_buttons);
        LinearLayout colorButtons = root.findViewById(R.id.color_buttons);

        @Override
        public void onClick(View v) {
            if (v == strokeSwitch) {
                if (drawShapeView.getPaintColor() == Color.WHITE) {
                    drawShapeView.setPaintColor(Color.BLACK);
                    if (drawShapeView.getEraser()) {
                        drawShapeView.setPaintStrokeWidth(drawShapeView.lastStrokeWidth);
                        drawShapeView.setEraser(false);
                    }
                }

                int visible = strokeButtons.getVisibility();
                if (visible == View.GONE) {
                    strokeButtons.setVisibility(View.VISIBLE);
                    shapeButtons.setVisibility(View.GONE);
                    colorButtons.setVisibility(View.GONE);
                } else {
                    strokeButtons.setVisibility(View.GONE);
                }
            } else if (v == shapeSwitch) {
                if (drawShapeView.getPaintColor() == Color.WHITE) {
                    drawShapeView.setPaintColor(Color.BLACK);
                    if (drawShapeView.getEraser()) {
                        drawShapeView.setPaintStrokeWidth(drawShapeView.lastStrokeWidth);
                        drawShapeView.setEraser(false);
                    }
                }
                int visible = shapeButtons.getVisibility();
                if (visible == View.GONE) {
                    shapeButtons.setVisibility(View.VISIBLE);
                    strokeButtons.setVisibility(View.GONE);
                    colorButtons.setVisibility(View.GONE);
                } else {
                    shapeButtons.setVisibility(View.GONE);
                }
            } else if (v == eraserButton) {
                drawShapeView.eraserOn();
                strokeButtons.setVisibility(View.GONE);
                shapeButtons.setVisibility(View.GONE);
            } else if (v == colorButton) {
                if (drawShapeView.getPaintColor() == Color.WHITE) {
                    drawShapeView.setPaintColor(Color.BLACK);
                    if (drawShapeView.getEraser()) {
                        drawShapeView.setPaintStrokeWidth(drawShapeView.lastStrokeWidth);
                        drawShapeView.setEraser(false);
                    }
                }
                int visible = colorButtons.getVisibility();
                if (visible == View.GONE) {
                    colorButtons.setVisibility(View.VISIBLE);
                    strokeButtons.setVisibility(View.GONE);
                    shapeButtons.setVisibility(View.GONE);
                } else {
                    colorButtons.setVisibility(View.GONE);
                }
            }
        }
    }

    private class ColorOnClickListener implements View.OnClickListener {
        AppCompatImageButton colorBlack = root.findViewById(R.id.black_color_button);
        AppCompatImageButton colorRed = root.findViewById(R.id.red_color_button);
        AppCompatImageButton colorGreen = root.findViewById(R.id.green_color_button);
        AppCompatImageButton colorBlue = root.findViewById(R.id.blue_color_button);
        AppCompatImageButton colorYellow = root.findViewById(R.id.yellow_color_button);

        @Override
        public void onClick(View v) {
            if (v == colorBlack) drawShapeView.setPaintColor(Color.BLACK);
            else if (v == colorRed) drawShapeView.setPaintColor(Color.RED);
            else if (v == colorGreen) drawShapeView.setPaintColor(Color.GREEN);
            else if (v == colorBlue) drawShapeView.setPaintColor(Color.BLUE);
            else if (v == colorYellow) drawShapeView.setPaintColor(Color.YELLOW);
        }
    }

    private class ShapeOnClickListener implements View.OnClickListener{
        AppCompatImageButton brushButton = root.findViewById(R.id.brush_button);
        AppCompatImageButton rectButton = root.findViewById(R.id.rect_button);
        AppCompatImageButton roundedRectButton = root.findViewById(R.id.rounded_rect_button);
        AppCompatImageButton circleButton = root.findViewById(R.id.circle_button);
        AppCompatImageButton lineButton = root.findViewById(R.id.line_button);
        AppCompatImageButton diamondButton = root.findViewById(R.id.diamond_button);


        @Override
        public void onClick(View v) {
            if (v == brushButton) {
                drawShapeView.setShapeType(ShapeType.NONE);
            } else if (v == rectButton) {
                drawShapeView.setShapeType(ShapeType.RECTANGLE);
            } else if (v == circleButton) {
                drawShapeView.setShapeType(ShapeType.CIRCLE);
            } else if (v == lineButton) {
                drawShapeView.setShapeType(ShapeType.LINE);
            } else if (v == diamondButton) {
                drawShapeView.setShapeType(ShapeType.DIAMOND);
            } else if (v == roundedRectButton) {
                drawShapeView.setShapeType(ShapeType.ROUNDED_RECT);
            }
        }
    }
}