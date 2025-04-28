package com.dordekel.memocircle;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PersonalMemoFragment extends Fragment {

    Intent noteIntent;

    public PersonalMemoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_personal_memo, container, false);

        //initiate the database and DAO
        AppDatabase db = AppDatabase.getInstance(getContext());
        NoteDao noteDao = db.noteDao();

        //declare the views
        Button newNoteButton = view.findViewById(R.id.newNoteButton);
        RecyclerView notesRecyclerView = view.findViewById(R.id.notesRecyclerView);

        //declare the adapter for the RecyclerView:
        PersonalNotesRecyclerAdapter adapter = new PersonalNotesRecyclerAdapter(getContext(), noteDao.getAllNotes(), listener);

        //create the intent for the NoteActivity (used multiple times in this fragment):
        noteIntent = new Intent(getActivity(), NoteActivity.class);

        //set the onClickListeners
        newNoteButton.setOnClickListener(v -> {
            //move to NoteActivity:
            noteIntent.putExtra("isNewNote", true);
            startActivity(noteIntent);
        });


        //new RecyclerView with CardView layout:

        //some settings:
        notesRecyclerView.setHasFixedSize(true);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int space = 10; // pixels (not dp)
                outRect.left = space;
                outRect.right = space;
                outRect.top = space;
                outRect.bottom = space;
            }
        });
        //set the adapter:
        notesRecyclerView.setAdapter(adapter);


        return view;
    }

    private final PersonalNotesRecyclerAdapter.PersonalNoteClickListener listener = new PersonalNotesRecyclerAdapter.PersonalNoteClickListener() {
        @Override
        public void onPersonalNoteClick(Note note) {
            noteIntent.putExtra("isNewNote", false);
            //pass the noteID to the NoteActivity via putExtra - again, retrieving data from the room database should be done under a new thread.
            new Thread(() -> {
                noteIntent.putExtra("noteID", note.getNoteId());
            }).start();

            //start the NoteActivity:
            startActivity(noteIntent);
        }
    };
}