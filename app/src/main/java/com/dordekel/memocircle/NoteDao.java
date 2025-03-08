package com.dordekel.memocircle;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//This interface describes the methods that will be used to interact with the database.
//Basically, defining the Data Access Object (DAO).
@Dao
public interface NoteDao {
    //insert a note into the database (replace if already exists with same Primary Key, in this case, noteId).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertNote(Note note);
    //update a note in the database (works by Primary Key, in this case, noteId).
    @Update
    public void updateNote(Note note);
    //delete a note from the database (works by Primary Key, in this case, noteId).
    @Delete
    public void deleteNote(Note note);

    //simple SQL query to get all notes from the database. Order by descending noteId (latest is first).
    @Query("SELECT * FROM notes ORDER BY noteId DESC")
    public Note[] getAllNotes();
    //SQL query to get a note by its title.
    @Query("SELECT * FROM notes WHERE title = :title")
    public Note getNoteByTitle(String title);
    //SQL query to get a note by its noteId.
    @Query("SELECT * FROM notes WHERE noteID = :noteID")
    public Note getNoteByNoteID(int noteID);
    //SQL query to delete all notes from the database.
    @Query("DELETE FROM notes")
    public void deleteAllNotes();
}
