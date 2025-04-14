package com.hfad.projectflow;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.hfad.projectflow.database.Drawing;
import com.hfad.projectflow.database.DrawingDao;

import java.util.List;

public class WhiteBoardViewModel extends ViewModel {
    private LiveData<List<Drawing>> drawingData;

    public WhiteBoardViewModel(DrawingDao drawingDao, int projectId, WhiteBoardFragment fragment) {
        // Fetch LiveData from DAO
        observeData(drawingDao, projectId, fragment);

    }

    public void observeData(DrawingDao drawingDao, int projectId, WhiteBoardFragment fragment){
        drawingData = drawingDao.getDrawingsForProjectLive(projectId);
        drawingData.observe(fragment, drawingDataList -> {
            if (drawingDataList != null && !drawingDataList.isEmpty()) {
                // Update the adapter with new data
                fragment.getNavDrawerAdapter().updateData(drawingDataList);
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
