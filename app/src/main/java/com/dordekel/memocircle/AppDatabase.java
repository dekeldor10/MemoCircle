package com.dordekel.memocircle;

import androidx.room.Database;
import androidx.room.RoomDatabase;
//This is an abstract class that extends RoomDatabase.
//This class will be used to create the database instance.

//declare the Note class as the only entity in the database, and set the version to 1.
@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    //connect the DAO and the Note entities.
    public abstract NoteDao noteDao();
}