package com.dordekel.memocircle;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //declare the buttons
        Button returnButton = findViewById(R.id.returnButton);
        Button saveNoteButton = findViewById(R.id.saveNoteButton);
        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText textContentEditText = findViewById(R.id.textContentEditText);

        //initiate the database and DAO
        AppDatabase db = AppDatabase.getInstance(this);
        NoteDao noteDao = db.noteDao();



        //set the onClickListeners
        returnButton.setOnClickListener(v -> {
            //return to MainActivity
            Intent returnIntent = new Intent(NoteActivity.this, MainActivity.class);
            startActivity(returnIntent);
            //delete this activity and the stack behind it, for optimal user experience.
            finishAffinity();
        });

        saveNoteButton.setOnClickListener(v -> {
            //get the texts from the editTexts:
            String noteTitle = titleEditText.getText().toString();
            String textContent = textContentEditText.getText().toString();

            Note note = new Note(noteTitle, textContent);

            //save the note to the database:
            //when using Room, the note has to be created by a Thread.
            new Thread(() -> {
                noteDao.insertNote(note);
                //display note in logCat (for debugging purposes):
                System.out.println(noteDao.getAllNotes()[0].getTextContent());
            }).start();

            Toast.makeText(NoteActivity.this, "Note Saved!", Toast.LENGTH_SHORT).show();

        });

        //For the finished product, i'll use a textWatcher and a Handler for saving or updating the note when the user
        //stops writing. for now, i'll just use a button, due to lack of time.
        /*
        titleEditText.addTextChangedListener(textWatcherTitle);
        textContentEditText.addTextChangedListener(textWatcherTextContent);

         */

    }
    /*
    //The textWatcher fot the title EditText
    TextWatcher textWatcherTitle = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        //I will be using this method for creating / updating the note when the user stops writing. Just like in Google Keep.
        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    //The textWatcher for the textContent EditText
    TextWatcher textWatcherTextContent = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        //again, like the title, I will be using this method for creating / updating the note when the user stops writing.
        @Override
        public void afterTextChanged(Editable editable) {
            Toast.makeText(NoteActivity.this, "done editing textContent", Toast.LENGTH_SHORT).show();
        }
    };

     */

}