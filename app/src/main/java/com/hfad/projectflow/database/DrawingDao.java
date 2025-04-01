package com.hfad.projectflow.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hfad.projectflow.Thumbnail;

import java.util.List;

@Dao
public interface DrawingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDrawing(Drawing drawing);

    @Query("SELECT * FROM drawings WHERE id = :drawingId")
    Drawing getDrawingById(int drawingId);

    @Query("SELECT * FROM drawings WHERE project_id = :projectId")
    List<Drawing> getDrawingsForProject(int projectId);

    @Query("SELECT * FROM drawings WHERE project_id = :projectId")
    LiveData<List<Drawing>> getDrawingsForProjectLive(int projectId);

    @Query("SELECT drawing_data FROM drawings WHERE project_id = :projectId")
    List<byte[]> getAllDrawingDataForProject(int projectId);

    @Update
    void updateDrawing(Drawing drawing);

    @Query("DELETE FROM drawings")
    void deleteAllDrawings();
}
