package com.hfad.projectflow;

import androidx.annotation.NonNull;

public class Project {
    private String name;
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
    }
}
