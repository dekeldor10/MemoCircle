package com.dordekel.memocircle;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
//This is an abstract class that extends RoomDatabase.
//This class will be used to create the database instance.

//declare the Note class as the only entity in the database, and set the version to 1.
@Database(entities = {Note.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    //connect the DAO and the Note entities.
    public abstract NoteDao noteDao();

    private static AppDatabase instance;

    //build the database instance. It's easier and more organised to do it here.
    public static synchronized AppDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "note_database")
                    .fallbackToDestructiveMigration().allowMainThreadQueries()//helps in developing stage (avoids app crashes).
                    .build();
        }

        return instance;
    }
}
