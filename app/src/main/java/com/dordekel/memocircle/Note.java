package com.dordekel.memocircle;

import android.graphics.Bitmap;

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
    //I'll store the image in a byte array. in order to store it with room, the column has to be of the BLOB type.
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] imgByteArr;


    //create two constructors: for notes with and without images.
    //The plan is to make every note text-only by default, and add images if the user wants to.

    //text-only note:
    public Note(String title, String textContent, byte[] imgByteArr) {
        this.title = title;
        this.textContent = textContent;
        //edited: I united the two constructors into one. if the user doesn't want to add an image, the default option is null.
        this.imgByteArr = imgByteArr;
    }
    //Unfortunately, Room can only persist one constructor.
    //I'll use the default for now.
    /*
    @Ignore
    //note with image:
    public Note(String title, String textContent, byte[] imgByteArr) {
        this.title = title;
        this.textContent = textContent;
        this.imgByteArr = imgByteArr;
    }

     */

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
    public byte[] getImgByteArr(){
        return imgByteArr;
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
    public void setImgByteArr(byte[] imgByteArr) {
        this.imgByteArr = imgByteArr;
    }
}
