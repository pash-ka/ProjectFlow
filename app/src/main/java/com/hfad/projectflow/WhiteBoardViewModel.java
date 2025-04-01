package com.hfad.projectflow;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.hfad.projectflow.database.Drawing;
import com.hfad.projectflow.database.DrawingDao;

import java.util.List;

public class WhiteBoardViewModel extends ViewModel {
    private final LiveData<List<Drawing>> drawingData;

    public WhiteBoardViewModel(DrawingDao drawingDao, int projectId, WhiteBoardActivity activity) {
        // Fetch LiveData from DAO
        drawingData = drawingDao.getDrawingsForProjectLive(projectId);
        drawingData.observe(activity, drawingDataList -> {
            if (drawingDataList != null && !drawingDataList.isEmpty()) {
                // Update the adapter with new data
                activity.getNavDrawerAdapter().updateData(drawingDataList);
                System.out.println("List size: " + drawingDataList.size());
            } else {
                System.out.println("No drawing data found for project_id: " + projectId);
                // Optionally, display a "no data available" message
            }
        });
    }

    public LiveData<List<Drawing>> getDrawingData() {
        return drawingData;
    }
}
