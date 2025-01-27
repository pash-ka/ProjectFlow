package com.hfad.projectflow.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects",
    foreignKeys = @ForeignKey(entity = User.class,
                                parentColumns = "id",
                                childColumns = "owner_id",
                                onDelete = ForeignKey.CASCADE))
public class Project {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "owner_id")
    public int ownerId;

    /*private String name;
    private String description;

    public static Project[] projects = {
            new Project("Uni", "test text"),
            new Project("Coll", "another test text")
    };


    public Project(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

    public String getName(){
        return this.name;
    }

    @NonNull
    @Override
    public String toString(){
        return this.name;
    }*/
}
