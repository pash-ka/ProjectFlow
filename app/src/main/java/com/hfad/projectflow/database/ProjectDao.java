package com.hfad.projectflow.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Project project);

    @Query("SELECT * FROM projects")
    List<Project> getAllProjects();

    @Query("SELECT * FROM projects WHERE owner_id = :userId")
    List<Project> getProjectsForUser(int userId);

    @Query("SELECT * FROM projects WHERE id = :projectId")
    Project getProjectById(long projectId);

    @Query("SELECT id FROM projects WHERE name = :projectName")
    long getProjectIdByName(String projectName);

    /*@Query("SELECT description FROM projects WHERE id = :projectId")
    String getDescriptionById(long projectId);*/

    @Query("DELETE FROM projects WHERE id = :projectId")
    void deleteProjectById(long projectId);

    @Delete
    void deleteProject(Project project);
}
