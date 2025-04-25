package com.dordekel.memocircle;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalMemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalMemoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PersonalMemoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalMemoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalMemoFragment newInstance(String param1, String param2) {
        PersonalMemoFragment fragment = new PersonalMemoFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personal_memo, container, false);

        //initiate the database and DAO
        AppDatabase db = AppDatabase.getInstance(getContext());
        NoteDao noteDao = db.noteDao();

        //declare the buttons
        Button newNoteButton = view.findViewById(R.id.newNoteButton);
        ListView notesListView = view.findViewById(R.id.notesListView);
        //TextView textView = view.findViewById(R.id.textView);

        //create the intent for the NoteActivity (used multiple times in this fragment):
        Intent noteIntent = new Intent(getActivity(), NoteActivity.class);

        //set the onClickListeners
        newNoteButton.setOnClickListener(v -> {
            //move to NoteActivity:
            noteIntent.putExtra("isNewNote", true);
            startActivity(noteIntent);
        });

        //retrieving data from database should be done under a thread
        new Thread(() -> {
           Note[] noteArr = noteDao.getAllNotes();

           //create a String arr of the titles:
            String[] notesTitlesArr = new String[noteArr.length];
            for(int i = 0; i < noteArr.length; i++){{
                notesTitlesArr[i] = noteArr[i].getTitle();
            }}

            //use an ArrayAdapter, with a built-in layout (for simplicity for now, this will be changed)
            ArrayAdapter<String> adapter = new ArrayAdapter<>
                    (getContext(), android.R.layout.simple_selectable_list_item, notesTitlesArr);

            //onClick for a note (item in the ListView):
            notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    //get item title in order to find the note from the database via a query (specified in the DAO)
                    String noteTitle = adapter.getItem(position);

                    //use the existing noteIntent:
                    //the NoteActivity should know if this is a new note or not.
                    //do this by adding another putExtra here, where it specifies what initiated the intent (pressing a note or creating a new one)
                    noteIntent.putExtra("isNewNote", false);
                    //pass the noteID to the NoteActivity via putExtra
                    noteIntent.putExtra("noteID", noteArr[position].getNoteId());

                    //start the NoteActivity:
                    startActivity(noteIntent);

                }
            });


            //set the adapter on the ListView. this will display the list of notes.
            notesListView.setAdapter(adapter);

        }).start();

        return view;
    }
    //TODO: something is wrong. for some reason, the app asks me to pick a number of images every time i open a saved note. Check later
}