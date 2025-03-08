package com.dordekel.memocircle;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

//declare the class as an entity.
@Entity(tableName = "notes")
public class Note {
    //declare the primary key, and make him generate automatically
    @PrimaryKey(autoGenerate = true)
    private int noteId;
    //declare other columns (there is no need to edit the columns names)
    private String title;
    private String textContent;
    //I'll save the URI of the image as a String. this may create a problem when doing the shared part of the app, but i'll cross the bridge when i get there.
    private String imgUriString;


    //create two constructors: for notes with and without images.
    //The plan is to make every note text-only by default, and add images if the user wants to.

    //text-only note:
    public Note(String title, String textContent, String imgUriString) {
        this.title = title;
        this.textContent = textContent;
        //edited: I united the two constructors into one. if the user doesn't want to add an image, the default option is null.
        this.imgUriString = imgUriString;
    }

    //getters:
    public int getNoteId(){
        return noteId;
    }
    public String getTitle(){
        return title;
    }
    public String getTextContent(){
        return textContent;
    }
    public String getImgUriString(){
        return imgUriString;
    }

    //setters:
    public void setNoteId(int noteId){
        this.noteId = noteId;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setTextContent(String textContent){
        this.textContent = textContent;
    }
    public void setImgUriString(String imgUriString){this.imgUriString = imgUriString;}

}
