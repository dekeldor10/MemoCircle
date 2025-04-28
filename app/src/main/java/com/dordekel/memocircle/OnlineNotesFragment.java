package com.dordekel.memocircle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class OnlineNotesFragment extends Fragment {


    public OnlineNotesFragment() {
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
        View view = inflater.inflate(R.layout.fragment_online_notes, container, false);

        //declare the views:
        Button sharedFragmentButton = view.findViewById(R.id.sharedFragmentButton);
        Button publicFragmentButton = view.findViewById(R.id.publicFragmentButton);

        //inflate the shared fragment automatically when this online fragment is opened:
        getChildFragmentManager().beginTransaction().replace(R.id.onlineFragmentsContainer, new SharedMemoFragment()).commit();
        //disable the shared fragment button to notify the user that he is shown the correct fragment:
        sharedFragmentButton.setEnabled(false);


        //set the OnClickListeners:
        sharedFragmentButton.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction().replace(R.id.onlineFragmentsContainer, new SharedMemoFragment()).commit();
            sharedFragmentButton.setEnabled(false);
            publicFragmentButton.setEnabled(true);
        });

        publicFragmentButton.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction().replace(R.id.onlineFragmentsContainer, new ForYouMemoFragment()).commit();
            sharedFragmentButton.setEnabled(true);
            publicFragmentButton.setEnabled(false);
        });


        //TODO: fix the light-mode color problems

        return view;
    }
}