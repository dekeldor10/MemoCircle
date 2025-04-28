package com.dordekel.memocircle;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PersonalNotesRecyclerAdapter extends RecyclerView.Adapter<PersonalNoteViewHolder>{

    Context context;
    Note[] noteArray;
    PersonalNoteClickListener clickListener;

    public PersonalNotesRecyclerAdapter(Context context, Note[] noteArray, PersonalNoteClickListener clickListener) {
        this.context = context;
        this.noteArray = noteArray;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PersonalNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //create the new card for each personal note:
        return new PersonalNoteViewHolder(View.inflate(context, R.layout.card_view_personal, null));
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalNoteViewHolder holder, int position) {
        //set the data for each personal note:
        Note personalNote = noteArray[position];
        holder.roomNoteTitleTextView.setText(personalNote.getTitle());
        holder.roomNoteBodyTextView.setText(personalNote.getTextContent());

        //holder.roomNoteImageView.setImageResource(note.getImgUriString()); //TODO: make this work - view the image correctly


        //set the onclick listener:
        holder.roomNoteCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onPersonalNoteClick(personalNote);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteArray.length;
    }



    interface PersonalNoteClickListener {
        void onPersonalNoteClick(Note note);
    }

}

//a new class for the ViewHolder - necessary for the recycler view adapter.
class PersonalNoteViewHolder extends RecyclerView.ViewHolder {

    CardView roomNoteCardView;
    ImageView roomNoteImageView;
    TextView roomNoteTitleTextView, roomNoteBodyTextView;


    public PersonalNoteViewHolder(@NonNull View itemView) {
        super(itemView);
        roomNoteCardView = itemView.findViewById(R.id.roomNoteCardView);
        roomNoteImageView = itemView.findViewById(R.id.roomNoteImageView);
        roomNoteTitleTextView = itemView.findViewById(R.id.roomNoteTitleTextView);
        roomNoteBodyTextView = itemView.findViewById(R.id.roomNoteBodyTextView);
    }
}
