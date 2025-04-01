package com.hfad.projectflow.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "drawings",
        foreignKeys = @ForeignKey(entity = Project.class,
                parentColumns = "id",
                childColumns = "project_id",
                onDelete = ForeignKey.CASCADE))
public class Drawing {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "project_id")
    public int projectId;

    @ColumnInfo(name = "drawing_data", typeAffinity = ColumnInfo.BLOB)
    public byte[] drawingData;

    public int getId() {
        return id;
    }

    public byte[] getDrawingData() {
        return drawingData;
    }

    public int getProjectId() {
        return projectId;
    }
}
