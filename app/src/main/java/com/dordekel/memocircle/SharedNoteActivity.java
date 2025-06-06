package com.dordekel.memocircle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedNoteActivity extends AppCompatActivity {

    static FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;
    DatabaseReference sharedNotesDatabaseReference;
    DatabaseReference sharedNotesPermissionsDatabaseReference;
    DatabaseReference noteContentsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initialize firebase-related objects:
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance("https://memocircle-ac0c1-default-rtdb.europe-west1.firebasedatabase.app/");
        usersDatabaseReference = database.getReference("users");
        sharedNotesDatabaseReference = database.getReference("sharedNotes");
        sharedNotesPermissionsDatabaseReference = database.getReference("sharedNotesPermissions");
        noteContentsDatabaseReference = database.getReference("noteContents");

        //declare the views:
        TextView usersListTextView = findViewById(R.id.usersListTextView);
        EditText sharedTitleEditText = findViewById(R.id.sharedTitleEditText);
        EditText sharedBodyEditText = findViewById(R.id.sharedBodyEditText);
        MultiAutoCompleteTextView usersCompleteTextView = findViewById(R.id.usersCompleteTextView);
        Button sharedReturnButton = findViewById(R.id.sharedReturnButton);
        Button sharedDeleteButton = findViewById(R.id.sharedDeleteButton);
        Button sharedUpdateNoteButton = findViewById(R.id.sharedUpdateNoteButton);

        //intent to return to the MainActivity:
        Intent returnIntent = new Intent(SharedNoteActivity.this, MainActivity.class);

        //receive the sharedNoteID:
        String sharedNoteId = getIntent().getStringExtra("sharedNoteId");


        //check if the current user is the note's owner:
        final String[] noteOwner = {""};
        final boolean[] isOwner = {true};
        sharedNotesDatabaseReference.child(sharedNoteId).child("noteOwnerId").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        noteOwner[0] = task.getResult().getValue(String.class);
                        isOwner[0] = firebaseUser.getUid().equals(noteOwner[0]);
                        //if not, hide the delete button and the option to invite users:
                        if(!isOwner[0]){
                            sharedDeleteButton.setVisibility(View.GONE);
                            usersCompleteTextView.setVisibility(View.GONE);
                        }
                        Log.d("FirebaseAA", "is owner: " + isOwner[0]);
                    }
                });

        //show the content of the note in the editTexts:
        Toast.makeText(this, "getting note contents...", Toast.LENGTH_SHORT).show();

        noteContentsDatabaseReference.child(sharedNoteId).child("noteTitle").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        sharedTitleEditText.setText(task.getResult().getValue(String.class));
                    }
                });

        noteContentsDatabaseReference.child(sharedNoteId).child("noteBody").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        sharedBodyEditText.setText(task.getResult().getValue(String.class));
                    }
                });

        //show the allowed users:

        final List<String> allowedUsernamesIdList = new ArrayList<>();
        allowedUsernamesIdList.add(firebaseUser.getUid()); //the current user is obviously allowed
        sharedNotesPermissionsDatabaseReference.child(sharedNoteId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() { //the users inside the note's permissions
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for(DataSnapshot userSnapshot : task.getResult().getChildren()){ //for each of the users
                    if(userSnapshot.getValue(Boolean.class)){ //if the user is allowed to edit the note
                        allowedUsernamesIdList.add(userSnapshot.getKey()); //add the user's ID to the list
                    }
                }

            }
        });
        final String[] allowedUsernames = {" " + firebaseUser.getDisplayName()};
        //get the usernames for the allowed users (by ID):
        usersDatabaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for(DataSnapshot userSnapshot : task.getResult().getChildren()) { //for every user in this node
                    if(allowedUsernamesIdList.contains(userSnapshot.getKey()) && !userSnapshot.getKey().equals(firebaseUser.getUid())){ //if the user is allowed to edit the note
                        allowedUsernames[0] += ", " + userSnapshot.getValue(String.class);
                        Log.d("FirebaseAA", "allowed user: " + allowedUsernames[0]);
                    }
                }
                usersListTextView.setText(allowedUsernames[0]);
            }
        });





        //working with the multiAutoCompleteTextView:
        List<String> usersIdList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        usersCompleteTextView.setAdapter(adapter);
        usersDatabaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() { //get all the authorized users from the users node
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for(DataSnapshot userSnapshot : task.getResult().getChildren()){
                    usersIdList.add(userSnapshot.getKey());
                    adapter.add(userSnapshot.getValue(String.class));
                }
            }
        });
        usersCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        usersCompleteTextView.setThreshold(-1);
        usersCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("FirebaseAA", "item clicked: " + parent.getItemAtPosition(position));
                String[] userToAdd = {""};
                //get the userID via username:
                usersDatabaseReference.orderByValue().equalTo((String) parent.getItemAtPosition(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot userSnapshot : snapshot.getChildren()){ //I search by username, so there should only be one!
                            userToAdd[0] = userSnapshot.getKey();
                        }

                        //permit adding users only if you are the note's owner:
                        //it is done here to avoid issues with latency.
                        if(isOwner[0]){
                            Log.d("FirebaseAA", "usertoadd: " + userToAdd[0]); //this is ""! something wrong here.
                            //usersDatabaseReference.child(firebaseUser.getUid()).setValue(firebaseUser.getDisplayName());

                        sharedNotesPermissionsDatabaseReference.child(sharedNoteId).child(userToAdd[0]).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              Toast.makeText(SharedNoteActivity.this, "user " + usersIdList.get(position) + "has been added to the note", Toast.LENGTH_SHORT).show();
                           }
                        });

                        } else{
                            Toast.makeText(SharedNoteActivity.this, "only the owner is allowed to add users", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        //set the OnClickListeners:
        sharedReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean[] isUpdatingDone = {true};
                //update the note in sharedNotes:
                Map<String, Object> sharedNoteInfo = new HashMap<>();
                //update the time last edited:
                sharedNoteInfo.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                sharedNotesDatabaseReference.child(sharedNoteId).updateChildren(sharedNoteInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isUpdatingDone[0] = !isUpdatingDone[0];
                                if(isUpdatingDone[0]){
                                    startActivity(returnIntent);
                                    finishAffinity();
                                }
                            }
                        });

                //update the note in noteContents:
                Map<String, Object> sharedNoteContents = new HashMap<>();
                //update the time last edited:
                sharedNoteContents.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                String noteTitle = sharedTitleEditText.getText().toString();
                String noteBody = sharedBodyEditText.getText().toString();
                //make sure the content isn't blank (empty / spaces only):
                if(noteTitle.isBlank()){
                    noteTitle = "[ Untitled ]";
                }
                if(noteBody.isBlank()){
                    noteBody = "[ Empty ]";
                }
                sharedNoteContents.put("noteTitle", noteTitle);
                sharedNoteContents.put("noteBody", noteBody);
                noteContentsDatabaseReference.child(sharedNoteId).updateChildren(sharedNoteContents)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isUpdatingDone[0] = !isUpdatingDone[0];
                                if(isUpdatingDone[0]){
                                    startActivity(returnIntent);
                                    finishAffinity();
                                }
                            }
                        });

                sharedNotesDatabaseReference.child(sharedNoteId).updateChildren(sharedNoteInfo);
            }
        });

        sharedDeleteButton.setOnClickListener(new View.OnClickListener() { //TODO: add a confirmation dialog.
            @Override
            public void onClick(View v) {

                //delete from sharedNotes:
                final int[] processesDone = {0};
                sharedNotesDatabaseReference.child(sharedNoteId).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                processesDone[0]++;
                                if(processesDone[0] == 3){
                                    startActivity(returnIntent);
                                    finishAffinity();
                                }
                            }
                        });

                //delete from noteContents:
                noteContentsDatabaseReference.child(sharedNoteId).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                processesDone[0]++;
                                if(processesDone[0] == 3){
                                    startActivity(returnIntent);
                                    finishAffinity();
                                }
                            }
                        });

                //delete from sharedNotesPermissions:
                sharedNotesPermissionsDatabaseReference.child(sharedNoteId).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                processesDone[0]++;
                                if(processesDone[0] == 3){
                                    startActivity(returnIntent);
                                    finishAffinity();
                                }
                            }
                        });

                Log.d("FirebaseDebug", "shared note deleted");
            }
        });

        sharedUpdateNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update the note in sharedNotes:
                Map<String, Object> sharedNoteInfo = new HashMap<>();
                //update the time last edited:
                sharedNoteInfo.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                sharedNotesDatabaseReference.child(sharedNoteId).updateChildren(sharedNoteInfo);

                //update the note in noteContents:
                Map<String, Object> sharedNoteContents = new HashMap<>();
                //update the time last edited:
                sharedNoteContents.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                String noteTitle = sharedTitleEditText.getText().toString();
                String noteBody = sharedBodyEditText.getText().toString();
                //make sure the content isn't blank (empty / spaces only):
                if(noteTitle.isBlank()){
                    noteTitle = "[ Untitled ]";
                }
                if(noteBody.isBlank()){
                    noteBody = "[ Empty ]";
                }
                sharedNoteContents.put("noteTitle", noteTitle);
                sharedNoteContents.put("noteBody", noteBody);
                noteContentsDatabaseReference.child(sharedNoteId).updateChildren(sharedNoteContents);
            }
        });
    }
}