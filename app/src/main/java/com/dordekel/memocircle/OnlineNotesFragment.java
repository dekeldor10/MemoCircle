package com.dordekel.memocircle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OnlineNotesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnlineNotesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OnlineNotesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OnlineNotesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OnlineNotesFragment newInstance(String param1, String param2) {
        OnlineNotesFragment fragment = new OnlineNotesFragment();
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



        return view;
    }
}