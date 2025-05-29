package com.dordekel.memocircle;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        Button personalMemoButton = findViewById(R.id.PersonalMemoButton);
        Button onlineMemoButton = findViewById(R.id.OnlineMemoButton);


        //Please note: I will be inflating every fragment with a tag.
        //This will mainly help me make sure I don't inflate an already-inflated fragment.

        //inflate PersonalMemoFragment by default, every time the app is opened (just like Google Keep).
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new PersonalMemoFragment(), "PERSONAL_MEMO_FRAGMENT")
                .commit();
        personalMemoButton.setEnabled(false);


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
            personalMemoButton.setEnabled(false);
            onlineMemoButton.setEnabled(true);
            profileButton.setEnabled(true);

        });

        onlineMemoButton.setOnClickListener(v -> {
            //create a OnlineNotesFragment object, and get the fragment by tag. this will be used to check if the fragment is already inflated.
            OnlineNotesFragment onlineNotesFragment = (OnlineNotesFragment) getSupportFragmentManager().findFragmentByTag("ONLINE_NOTES_FRAGMENT");
            //check if the fragment is already inflated and visible.
            if(onlineNotesFragment == null || !onlineNotesFragment.isVisible()){
                //the fragment is not visible, inflate it.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new OnlineNotesFragment(), "ONLINE_NOTES_FRAGMENT")
                        .commit();
            }
            personalMemoButton.setEnabled(true);
            onlineMemoButton.setEnabled(false);
            profileButton.setEnabled(true);
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
            personalMemoButton.setEnabled(true);
            onlineMemoButton.setEnabled(true);
            profileButton.setEnabled(false);
        });

    }
}