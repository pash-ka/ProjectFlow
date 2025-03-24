package com.hfad.projectflow.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseSingleton {
    private static volatile AppDatabase instance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `drawings` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `project_id` INTEGER NOT NULL, " +
                    "`drawing_data` BLOB, FOREIGN KEY(`project_id`) REFERENCES `projects`(`id`) ON DELETE CASCADE)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create a temporary table
            database.execSQL("CREATE TABLE projects_temp (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT UNIQUE," +
                    "description TEXT, owner_id INTEGER NOT NULL, FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE)");
            // Copy the data from the old table to the temporary table
            database.execSQL("INSERT INTO projects_temp (id, name, description, owner_id) SELECT id, " +
                    "name, description, owner_id FROM projects");
            // Drop the old table
            database.execSQL("DROP TABLE projects");
            // Rename the temporary table to the old table name
            database.execSQL("ALTER TABLE projects_temp RENAME TO projects");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "database-pf").addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3).build();
                }
            }
        }

        return instance;
    }
}

