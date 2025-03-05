package com.dordekel.memocircle;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
//MainActivity is used for fragment navigation.
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //declare the buttons
        Button profileButton = findViewById(R.id.ProfileButton);
        Button forYouMemoButton = findViewById(R.id.ForYouMemoButton);
        Button sharedMemoButton = findViewById(R.id.SharedMemoButton);
        Button personalMemoButton = findViewById(R.id.PersonalMemoButton);



        //Please note: I will be inflating every fragment with a tag.
        //This will mainly help me make sure I don't inflate an already-inflated fragment.

        //inflate PersonalMemoFragment by default, every time the app is opened (just like Google Keep).
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new PersonalMemoFragment(), "PERSONAL_MEMO_FRAGMENT")
                .commit();



        //setting onClickListeners for the buttons. The navigation itself.
        personalMemoButton.setOnClickListener(v -> {
            //create a PersonalMemoFragment object, and get the fragment by tag. this will be used to check if the fragment is already inflated.
            PersonalMemoFragment personalMemoFragment = (PersonalMemoFragment) getSupportFragmentManager().findFragmentByTag("PERSONAL_MEMO_FRAGMENT");
            //check if the fragment is already inflated and visible.
            if(personalMemoFragment == null || !personalMemoFragment.isVisible()){
                //the fragment is not visible, inflate it.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new PersonalMemoFragment(), "PERSONAL_MEMO_FRAGMENT")
                        .commit();
            }
        });

        sharedMemoButton.setOnClickListener(v -> {
            //create a SharedMemoFragment object, and get the fragment by tag. this will be used to check if the fragment is already inflated.
            SharedMemoFragment sharedMemoFragment = (SharedMemoFragment) getSupportFragmentManager().findFragmentByTag("SHARED_MEMO_FRAGMENT");
            //check if the fragment is already inflated and visible.
            if(sharedMemoFragment == null || !sharedMemoFragment.isVisible()){
                //the fragment is not visible, inflate it.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new SharedMemoFragment(), "SHARED_MEMO_FRAGMENT")
                        .commit();
            }
        });

        forYouMemoButton.setOnClickListener(v -> {
            //create a ForYouMemoFragment object, and get the fragment by tag. this will be used to check if the fragment is already inflated.
            ForYouMemoFragment forYouMemoFragment = (ForYouMemoFragment) getSupportFragmentManager().findFragmentByTag("FOR_YOU_MEMO_FRAGMENT");
            //check if the fragment is already inflated and visible.
            if(forYouMemoFragment == null || !forYouMemoFragment.isVisible()){
                //the fragment is not visible, inflate it.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ForYouMemoFragment(), "FOR_YOU_MEMO_FRAGMENT")
                        .commit();
            }
        });

        profileButton.setOnClickListener(v -> {
            //create a ProfileFragment object, and get the fragment by tag. this will be used to check if the fragment is already inflated.
            ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE_FRAGMENT");
            //check if the fragment is already inflated and visible.
            if(profileFragment == null || !profileFragment.isVisible()){
                //the fragment is not visible, inflate it.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment(), "PROFILE_FRAGMENT")
                        .commit();
            }
        });

    }
}
// delete later, just using this to organize my stuff:
/*
TODO: 1. finish the PersonalMemo:
TODO 1.1. make it possible to see all the notes properties (view image at least)
TODO 1.2. fix the activityForResult ao that the picture is higher quality and you can pick from the gallery.
 */
