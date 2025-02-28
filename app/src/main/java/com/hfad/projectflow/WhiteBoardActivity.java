package com.hfad.projectflow;

/*TODO
* maybe having tabs for different sketches,
* with an option of either staying on the same tab
* or creating another one for a new sketch
*/

/*TODO
* pencils/brushes
* shapes
* grid on/off
* place it all somewhere nice, menu
* infinite canvas
*
* */

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class WhiteBoardActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String EXTRA_CURRENT_USER_ID = "userId";
    private DrawShapeView drawShapeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_schemes);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        drawShapeView = findViewById(R.id.draw_shape_view);

        Button rectButton = findViewById(R.id.rect_button);
        rectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawShapeView.setShapeType("RECTANGLE");
            }
        });

        Button circleButton = findViewById(R.id.circle_button);
        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawShapeView.setShapeType("CIRCLE");
            }
        });

        Button lineButton = findViewById(R.id.line_button);
        lineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawShapeView.setShapeType("LINE");
            }
        });

        Button diamondButton = findViewById(R.id.diamond_button);
        diamondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawShapeView.setShapeType("DIAMOND");
            }
        });
    }
}