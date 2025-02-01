package com.hfad.projectflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class WorkActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "id";
    public static final String EXTRA_CURRENT_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        int projectId = (int) getIntent().getExtras().get(EXTRA_PROJECT_ID);
        int currentUserId = (int) getIntent().getExtras().get(EXTRA_CURRENT_USER_ID);


        TextView docView = findViewById(R.id.documentation);
        docView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Opening documentation", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(WorkActivity.this, DocumentationActivity.class);

                intent.putExtra(DocumentationActivity.EXTRA_PROJECT_ID, projectId);
                intent.putExtra(DocumentationActivity.EXTRA_CURRENT_USER_ID, currentUserId);
                //System.out.println(currentUserId);
                startActivity(intent);
            }
        });

        TextView blockSchemesView = findViewById(R.id.block_schemes);
        blockSchemesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkActivity.this, BlockSchemesActivity.class);

                intent.putExtra(BlockSchemesActivity.EXTRA_PROJECT_ID, projectId);
                intent.putExtra(BlockSchemesActivity.EXTRA_CURRENT_USER_ID, currentUserId);

                startActivity(intent);
            }
        });
    }
}