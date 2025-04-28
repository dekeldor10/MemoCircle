package com.dordekel.memocircle;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SharedMemoFragment extends Fragment {

    static FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;
    DatabaseReference sharedNotesDatabaseReference;
    DatabaseReference sharedNotesPermissionsDatabaseReference;
    DatabaseReference noteContentsDatabaseReference;


    public SharedMemoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shared_memo, container, false);

        //Initialize firebase-related objects:
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance("https://memocircle-ac0c1-default-rtdb.europe-west1.firebasedatabase.app/");
        usersDatabaseReference = database.getReference("users");
        sharedNotesDatabaseReference = database.getReference("sharedNotes");
        sharedNotesPermissionsDatabaseReference = database.getReference("sharedNotesPermissions");
        noteContentsDatabaseReference = database.getReference("noteContents");

        //initialize views:
        Button createSharedNoteButton = view.findViewById(R.id.createSharedNoteButton);
        ListView sharedNotesListView = view.findViewById(R.id.sharedNotesListView);
        TextView messageTextView = view.findViewById(R.id.messageTextView);
        //by default, hide the message text view:
        messageTextView.setVisibility(View.GONE);

        //the intent to move to SharedNoteActivity:
        Intent sharedNoteActivityIntent = new Intent(getActivity(), SharedNoteActivity.class);

        //get the public notes from the database: (to a list)
        List<String> sharedNotesIdsArr = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, new ArrayList<>()); // Initialize adapter with an empty list
        sharedNotesListView.setAdapter(adapter); // Set the adapter initially


        if(firebaseUser == null){
            //the user isn't signed in. cant continue.
            createSharedNoteButton.setVisibility(View.GONE);
            sharedNotesListView.setVisibility(View.GONE);
            //show the message:
            messageTextView.setVisibility(View.VISIBLE);
        }else {
            sharedNotesDatabaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() { //get all of the notes under the sharedNotes node
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        final int[] allowedNotesCount = {0};
                        for (DataSnapshot snapshot : task.getResult().getChildren()) { //for each note
                            String noteId = snapshot.getKey();
                            sharedNotesPermissionsDatabaseReference.child(noteId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() { //get the allowed users under this note'sId
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DataSnapshot userSnapshot : task.getResult().getChildren()) { //for each user in this note
                                            if (userSnapshot.getKey().equals(firebaseUser.getUid()) && userSnapshot.getValue(Boolean.class)) { //if the user is there and allowed
                                                Log.d("FirebaseDebug", "User is allowed to: " + noteId);
                                                allowedNotesCount[0]++;
                                                sharedNotesIdsArr.add(noteId); //add the noteId to the list
                                                noteContentsDatabaseReference.child(noteId).child("noteTitle").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() { //get the title of this note
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            String title = task.getResult().getValue(String.class);
                                                            Log.d("FirebaseDebug", "Title: " + title);
                                                            adapter.add(title); // add directly to the adapter
                                                        }
                                                    }
                                                });
                                                break; // exit inner permission loop and go to the next user in this note
                                            }
                                        }
                                    }
                                }
                            });
                            Log.d("FirebaseDebug", "iterated");
                        }

                        //set the click listener for each item in the list view:
                        sharedNotesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String noteId = sharedNotesIdsArr.get(position);
                                sharedNoteActivityIntent.putExtra("sharedNoteId", noteId);
                                startActivity(sharedNoteActivityIntent);
                            }
                        });
                    }
                }
            });
            Toast.makeText(getContext(), "Searching for notes shared with you...", Toast.LENGTH_SHORT).show(); //TODO: make it prettier and more real (some sort of loading screen)
        }

        //create a new shared note:
        createSharedNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //for moving to the next activity:
                //move to the publicNoteActivity, and pass the sharedNoteId:
                //for making sure that the note has been created at both locations at the time of launching the intent:
                final int[] processesDone = {0}; //this is a trick: to change this variable from inside the onComplete, it needs to be final.

                //create a new public note in the publicNotes node (with unique id):
                //publicNotesDatabaseReference.push();
                DatabaseReference newPublicNoteRef = sharedNotesDatabaseReference.push();
                String sharedNoteId = newPublicNoteRef.getKey();
                Log.d("FirebaseDebug", "Public note id: " + sharedNoteId);

                //insert basic note info (with a HashMap - the most suitable for working with a JSON file):
                Map<String, Object> sharedNoteInfo = new HashMap<>();
                //the user who created the public note is the owner
                sharedNoteInfo.put("noteOwnerId", firebaseUser.getUid());
                //set the time created (and also last edited, this will change one the owner or others write something) for the note:
                //i'm using ServerValue.TIMESTAMP [returns an Object] as this is the correct way for uniform and accurate timestamps across multiple users in my database.
                sharedNoteInfo.put("noteTimeCreatedStamp", ServerValue.TIMESTAMP);
                sharedNoteInfo.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                //and finally, add the info to the note:
                sharedNotesDatabaseReference.child(sharedNoteId).setValue(sharedNoteInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        processesDone[0]++;
                        if(processesDone[0] == 3){
                            sharedNoteActivityIntent.putExtra("sharedNoteId", sharedNoteId);
                            startActivity(sharedNoteActivityIntent);
                        }
                    }
                });
                //Great!

                //create a new note in the noteContents node (with the same id as the shared note):
                Map < String, Object > sharedNoteContents = new HashMap<>();
                sharedNoteContents.put("noteTitle", "");
                sharedNoteContents.put("noteBody", "");
                sharedNoteContents.put("noteTimeCreatedStamp", ServerValue.TIMESTAMP);
                sharedNoteContents.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);

                //create the note itself using the sharedNoteId:
                noteContentsDatabaseReference.child(sharedNoteId).setValue(sharedNoteContents).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        processesDone[0]++;
                        if(processesDone[0] == 3){
                            sharedNoteActivityIntent.putExtra("sharedNoteId", sharedNoteId);
                            startActivity(sharedNoteActivityIntent);
                        }
                    }
                });

                //create the note in the sharedNotePermissions with the same shared note ID:
                Map<String, Object> sharedNotePermissions = new HashMap<>();
                //add the owner as allowed:
                sharedNotePermissions.put(firebaseUser.getUid(), true);
                //create the note itselt using the sharedNoteId:
                sharedNotesPermissionsDatabaseReference.child(sharedNoteId).setValue(sharedNotePermissions).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        processesDone[0]++;
                        if(processesDone[0] == 3){
                            sharedNoteActivityIntent.putExtra("sharedNoteId", sharedNoteId);
                            startActivity(sharedNoteActivityIntent);
                        }
                    }
                });

            }
        });


        return view;
    }
}