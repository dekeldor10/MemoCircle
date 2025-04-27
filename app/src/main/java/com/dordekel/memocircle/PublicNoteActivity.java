package com.dordekel.memocircle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PublicNoteActivity extends AppCompatActivity {


    static FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;
    DatabaseReference publicNotesDatabaseReference;
    DatabaseReference noteContentsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_public_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //Initialize firebase-related objects:
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser(); //TODO: make sure this isnt null!
        database = FirebaseDatabase.getInstance("https://memocircle-ac0c1-default-rtdb.europe-west1.firebasedatabase.app/");
        usersDatabaseReference = database.getReference("users");
        publicNotesDatabaseReference = database.getReference("publicNotes");
        noteContentsDatabaseReference = database.getReference("noteContents");


        //declare the views:
        TextView noteIdTextView = findViewById(R.id.noteIdTextView);
        EditText publicTitleEditText = findViewById(R.id.publicTitleEditText);
        EditText publicBodyEditText = findViewById(R.id.publicBodyEditTextMultiLine);
        Button publicReturnButton = findViewById(R.id.publicReturnButton);
        Button publicDeleteButton = findViewById(R.id.publicDeleteButton);
        Button updatePublicNoteButton = findViewById(R.id.updatePublicNoteButton);

        //intent to return to the MainActivity:
        Intent returnIntent = new Intent(PublicNoteActivity.this, MainActivity.class); //TODO: in the future, make it return specifically to the ForYou Fragment.

        //receive the publicNoteID:
        String publicNoteId = getIntent().getStringExtra("publicNoteId");

        //check if the current user is the note's owner:
        final String[] noteOwner = {""};
        final boolean[] isOwner = {true};
        publicNotesDatabaseReference.child(publicNoteId).child("noteOwnerId").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        noteOwner[0] = task.getResult().getValue(String.class);
                        isOwner[0] = (firebaseUser.getUid().equals(noteOwner[0]));
                    }
                });
        //if not, hide the delete button:
        if(!isOwner[0]){
            publicDeleteButton.setVisibility(View.GONE);
        }


        //show the content of the note in the editTexts:
        Toast.makeText(this, "getting note contents...", Toast.LENGTH_SHORT).show(); //TODO: make it better

        noteContentsDatabaseReference.child(publicNoteId).child("noteTitle").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        publicTitleEditText.setText(task.getResult().getValue(String.class));
                    }
                });

        noteContentsDatabaseReference.child(publicNoteId).child("noteBody").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        publicBodyEditText.setText(task.getResult().getValue(String.class));
                    }
                });




        //set the onClickListeners:
        publicReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve and save the data:
                Map<String, Object> publicNoteInfo = new HashMap<>();
                final boolean[] isUpdatingDone = {false};
                final boolean[] isReadingDone = {false};


                //add the current user to the editors list:
                publicNotesDatabaseReference.child(publicNoteId).child("noteEditorsId").get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            //getting the editorsId ArrayList from the database:
                            DataSnapshot snapshot = task.getResult();
                            GenericTypeIndicator<ArrayList<String>> arrListGenericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                            ArrayList<String> editorsIdArrList = snapshot.getValue(arrListGenericTypeIndicator);
                            //Log.d("FirebaseDebug", "editorsIdArrList: " + editorsIdArrList.toString());
                            //check if the current user is in the list of editors:
                            boolean isThere = false;
                            for(String editorId : editorsIdArrList){
                                if(editorId.equals(firebaseUser.getUid())){
                                    isThere = true;
                                }
                            }
                            //if not, add him:
                            if(!isThere){
                                Log.d("FirebaseDebug", "not there");
                                editorsIdArrList.add(firebaseUser.getUid());
                                //publicNotesDatabaseReference.child(publicNoteId).child("noteEditorsId").setValue(editorsIdArrList);
                                publicNoteInfo.put("noteEditorsId", editorsIdArrList);
                            }
                            isReadingDone[0] = true;
                        }
                    });

                //i'll skip the tags for now TODO: add them later.
                publicNoteInfo.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                publicNotesDatabaseReference.child(publicNoteId).updateChildren(publicNoteInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isUpdatingDone[0] = !isUpdatingDone[0];
                                if(isUpdatingDone[0] && isReadingDone[0]){
                                    startActivity(returnIntent);
                                    finishAffinity();
                                }
                            }
                        });

                Map <String, Object> publicNoteContents = new HashMap<>();
                publicNoteContents.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP); //do it first to minimize the time difference.
                String noteTitle = publicTitleEditText.getText().toString();
                String noteBody = publicBodyEditText.getText().toString();
                //make sure the content isn't blank (empty / spaces only):
                if(noteTitle.isBlank()){
                    noteTitle = "[ Untitled ]";
                }
                if(noteBody.isBlank()){
                    noteBody = "[ Empty ]";
                }
                publicNoteContents.put("noteTitle", noteTitle);
                publicNoteContents.put("noteBody", noteBody);
                noteContentsDatabaseReference.child(publicNoteId).updateChildren(publicNoteContents)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isUpdatingDone[0] = !isUpdatingDone[0];
                                if(isUpdatingDone[0] && isReadingDone[0]){
                                    startActivity(returnIntent);
                                    finishAffinity();
                                }
                            }
                        });
            }
        });

        //delete the note: only available if the user is the public note's owner.
        publicDeleteButton.setOnClickListener(new View.OnClickListener() { //TODO: add a confirmation dialog.
            @Override
            public void onClick(View v) {

                final boolean[] isUpdatingDone = {false};
                publicNotesDatabaseReference.child(publicNoteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        isUpdatingDone[0] = !isUpdatingDone[0];
                        if(isUpdatingDone[0]){
                            startActivity(returnIntent);
                            finishAffinity();
                        }
                    }
                });
                noteContentsDatabaseReference.child(publicNoteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        isUpdatingDone[0] = !isUpdatingDone[0];
                        if(isUpdatingDone[0]){
                            startActivity(returnIntent);
                            finishAffinity();
                        }
                    }
                });
                Log.d("FirebaseDebug", "public note deleted");
            }
        });

        updatePublicNoteButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Map<String, Object> publicNoteInfo = new HashMap<>();

                //add the current user to the editors list:
                publicNotesDatabaseReference.child(publicNoteId).child("noteEditorsId").get()
                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                //getting the editorsId ArrayList from the database:
                                DataSnapshot snapshot = task.getResult();
                                GenericTypeIndicator<ArrayList<String>> arrListGenericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                                ArrayList<String> editorsIdArrList = snapshot.getValue(arrListGenericTypeIndicator);
                                //Log.d("FirebaseDebug", "editorsIdArrList: " + editorsIdArrList.toString());
                                //check if the current user is in the list of editors:
                                boolean isThere = false;
                                for(String editorId : editorsIdArrList){
                                    if(editorId.equals(firebaseUser.getUid())){
                                        isThere = true;
                                    }
                                }
                                //if not, add him:
                                if(!isThere){
                                    Log.d("FirebaseDebug", "not there");
                                    editorsIdArrList.add(firebaseUser.getUid());
                                    //publicNotesDatabaseReference.child(publicNoteId).child("noteEditorsId").setValue(editorsIdArrList);
                                    publicNoteInfo.put("noteEditorsId", editorsIdArrList);
                                }
                            }
                        });

                //i'll skip the tags for now TODO: add them later.
                publicNoteInfo.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                publicNotesDatabaseReference.child(publicNoteId).updateChildren(publicNoteInfo); //dont need a listener, as i dont do anything after updating.

                Map <String, Object> publicNoteContents = new HashMap<>();
                publicNoteContents.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP); //do it first to minimize the time difference.
                String noteTitle = publicTitleEditText.getText().toString();
                String noteBody = publicBodyEditText.getText().toString();
                //make sure the content isn't blank (empty / spaces only):
                if(noteTitle.isBlank()){
                    noteTitle = "[ Untitled ]";
                }
                if(noteBody.isBlank()){
                    noteBody = "[ Empty ]";
                }
                publicNoteContents.put("noteTitle", noteTitle);
                publicNoteContents.put("noteBody", noteBody);
                noteContentsDatabaseReference.child(publicNoteId).updateChildren(publicNoteContents); //dont need a listener, as i dont do anything after updating.
            }
        });


        //show the publicNoteId in the textview:
        noteIdTextView.setText("publicNoteID: " + publicNoteId);
    }


    //TODO: add the whole tag system.
    //TODO: add listeners to real-time changes, and view them.
}