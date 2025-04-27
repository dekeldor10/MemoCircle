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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ForYouMemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForYouMemoFragment extends Fragment {

    static FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;
    DatabaseReference publicNotesDatabaseReference;
    DatabaseReference noteContentsDatabaseReference;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ForYouMemoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ForYouMemoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ForYouMemoFragment newInstance(String param1, String param2) {
        ForYouMemoFragment fragment = new ForYouMemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_for_you_memo, container, false);

        //Initialize firebase-related objects:
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser(); //TODO: make sure this isnt null!
        database = FirebaseDatabase.getInstance("https://memocircle-ac0c1-default-rtdb.europe-west1.firebasedatabase.app/");
        usersDatabaseReference = database.getReference("users");
        publicNotesDatabaseReference = database.getReference("publicNotes");
        noteContentsDatabaseReference = database.getReference("noteContents");

        //initialize views:
        Button createPublicNoteButton = view.findViewById(R.id.createPublicNoteButton);
        ListView publicNotesListView = view.findViewById(R.id.publicNotesListView);

        //the intent to move to the publicNoteActivity:
        Intent publicNoteActivityIntent = new Intent(getActivity(), PublicNoteActivity.class);

        //get the public notes from the database: (to a list)
        List<String> publicNotesTitlesArr = new ArrayList<>();
        ArrayList<String> publicNotesIdsArr = new ArrayList<>();
        //displaying the notes in the ListView:
        publicNotesDatabaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() { //access all notes in publicNotes node
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                int publicNotesNum = (int) task.getResult().getChildrenCount();
                if(publicNotesNum == 0){
                    //there are no public notes
                    Log.d("FirebaseDebug", "No public notes");
                    return;
                }
                final int[] completedNotes = {0}; //this is a trick: to change this variable from inside the onComplete, it needs to be final.
                if(task.isSuccessful()){
                    for(DataSnapshot snapshot : task.getResult().getChildren()){ //interact with each public note
                        noteContentsDatabaseReference.child(snapshot.getKey())//use the public note's key to access the note's contents
                                .child("noteTitle").get()//get the note's title
                                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) { //do stuff with the title
                                        if(task.isSuccessful()){
                                            Log.d("FirebaseDebug", "Title: " + task.getResult().getValue(String.class));
                                            publicNotesTitlesArr.add(task.getResult().getValue(String.class));
                                            publicNotesIdsArr.add(snapshot.getKey());
                                        }
                                        completedNotes[0]++;
                                        if(completedNotes[0] == publicNotesNum){
                                            //now, i can be sure that it is done.
                                            Log.d("FirebaseDebug", "All done");
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, publicNotesTitlesArr);

                                            publicNotesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //launch the correct public note when a note is clicked:
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    String noteId = publicNotesIdsArr.get(position);
                                                    //Log.d("FirebaseDebug", "noteID: " + noteId);
                                                    publicNoteActivityIntent.putExtra("publicNoteId", noteId);
                                                    startActivity(publicNoteActivityIntent);
                                                }
                                            });

                                            publicNotesListView.setAdapter(adapter);
                                        }
                                    }
                                });
                    }
                }
            }
        });
        Toast.makeText(getContext(), "Searching for public notes...", Toast.LENGTH_SHORT).show(); //TODO: make it prettier and more real (some sort of loading screen)

        //on click for creating a new public note:
        createPublicNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //for moving to the next activity:
                    //move to the publicNoteActivity, and pass the publicNoteId:
                    //for making sure that the note has been created at both locations at the time of launching the intent:
                    final boolean[] isDone = {true}; //this is a trick: to change this variable from inside the onComplete, it needs to be final.

                    //create a new public note in the publicNotes node (with unique id):
                    //publicNotesDatabaseReference.push();
                    DatabaseReference newPublicNoteRef = publicNotesDatabaseReference.push();
                    String publicNoteId = newPublicNoteRef.getKey();
                    Log.d("FirebaseDebug", "Public note id: " + publicNoteId);

                    //insert basic note info (with a HashMap - the most suitable for working with a JSON file):
                    Map<String, Object> publicNoteInfo = new HashMap<>();
                    //the user who created the public note
                    publicNoteInfo.put("noteOwnerId", firebaseUser.getUid());
                    //add the owner to the editors list: (an ArrayList)
                    ArrayList<String> editorsArrList = new ArrayList<>();
                    editorsArrList.add(firebaseUser.getUid());
                    publicNoteInfo.put("noteEditorsId", editorsArrList);
                    //add the empty tags list: (an ArrayList)
                    ArrayList<String> tagsArrList = new ArrayList<>();
                    publicNoteInfo.put("noteTagsId", tagsArrList);
                    //set the time created (and also last edited, this will change one the owner writes something) for the note:
                    //i'm using ServerValue.TIMESTAMP [returns an Object] as this is the correct way for uniform and accurate timestamps across multiple users in my database.
                    publicNoteInfo.put("noteTimeCreatedStamp", ServerValue.TIMESTAMP);
                    publicNoteInfo.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);
                    //and finally, add the info to the note:
                    publicNotesDatabaseReference.child(publicNoteId).setValue(publicNoteInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isDone[0] = !isDone[0];
                                if(isDone[0]){
                                    publicNoteActivityIntent.putExtra("publicNoteId", publicNoteId);
                                    startActivity(publicNoteActivityIntent);
                                }
                            }
                        });
                    //Great!

                    //create a new note in the noteContents node (with the same id as the public note):
                    Map < String, Object > publicNoteContents = new HashMap<>();
                    publicNoteContents.put("noteTitle", "");
                    publicNoteContents.put("noteBody", "");
                    publicNoteContents.put("noteTimeCreatedStamp", ServerValue.TIMESTAMP);
                    publicNoteContents.put("noteTimeLastEditedStamp", ServerValue.TIMESTAMP);

                    //create the note itself using the publicNoteId:
                    noteContentsDatabaseReference.child(publicNoteId).setValue(publicNoteContents).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            isDone[0] = !isDone[0];
                            if(isDone[0]){
                                publicNoteActivityIntent.putExtra("publicNoteId", publicNoteId);
                                startActivity(publicNoteActivityIntent);
                            }
                        }
                    });
                }
            });

        // Inflate the layout for this fragment
        return view;
    }


    //TODO: ListView is outdated and should be replaced before final version.
    //TODO: show the notes by latest edited.
}