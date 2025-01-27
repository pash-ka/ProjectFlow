package com.hfad.projectflow.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseSingleton {
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "database-pf").build();
                }
            }
        }

        return instance;
    }
}

