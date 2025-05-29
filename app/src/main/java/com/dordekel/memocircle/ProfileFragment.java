package com.dordekel.memocircle;

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;


public class ProfileFragment extends Fragment {

    //new variables for authentication with firebase:
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacks;
    private PhoneAuthCredential mPhoneAuthCredential;
    static FirebaseUser firebaseUser; //not originally static, make sure it doesn't cause problems.
    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;
    String phoneNumber;
    String mVerificationId;
    String userVerificationCode;
    String emailToVerify;
    boolean isSignInButton = false;

    TextView isSignedInView, textViewUserId, userName, userEmail;
    Button updateInfoButton, confirmPhoneNumberButton, signOutButton, signInEmail;
    EditText editTextPhone;


    public ProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //declare all the views objects:
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        isSignedInView = view.findViewById(R.id.isSignedIn);
        textViewUserId = view.findViewById(R.id.textViewUserId);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        confirmPhoneNumberButton = view.findViewById(R.id.confirmPhoneNumberButton);
        signOutButton = view.findViewById(R.id.signOutButton);
        updateInfoButton = view.findViewById(R.id.updateInfoButton);
        signInEmail = view.findViewById(R.id.signInEmail);

        emailToVerify = "dekeldor10@gmail.com";


        //mAuth as an instance of FirebaseAuth:
        mAuth = FirebaseAuth.getInstance();
        //instance the FirebaseDatabase:
        database = FirebaseDatabase.getInstance("https://memocircle-ac0c1-default-rtdb.europe-west1.firebasedatabase.app/"); //the correct URL of the database.
        //instance the DatabaseReference for the users node:
        usersDatabaseReference = database.getReference("users");



        //the dialog for updating or inserting the name and email:
        AlertDialog.Builder emailAndNameDialogBuilder = new AlertDialog.Builder(getContext());
        emailAndNameDialogBuilder.setTitle("Set Name and Email");
        emailAndNameDialogBuilder.setMessage("Please set your name and email to continue.");

        //in order to add the two editTexts, i need to create a custom layout and inflate it.
        View emailAndNameDialogView = getLayoutInflater().inflate(R.layout.email_name_dialog, null);
        emailAndNameDialogBuilder.setView(emailAndNameDialogView);
        //get the two editTexts:
        EditText editTextName = emailAndNameDialogView.findViewById(R.id.editTextName);
        EditText editTextEmail = emailAndNameDialogView.findViewById(R.id.editTextEmail);

        //set up the dialog buttons:
        emailAndNameDialogBuilder.setPositiveButton("Submit", (dialog, which) -> {
            //get the name and email from the editTexts:
            String email = editTextEmail.getText().toString();
            //update the user profile in firebase:
            firebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(editTextName.getText().toString()).build())
                    .addOnCompleteListener(new OnCompleteListener<Void>() { //the correct way is to update the UI after the info has been sent to firebase.
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //and finally, update the UI:
                            userName.setText(firebaseUser.getDisplayName());
                            userEmail.setText(firebaseUser.getEmail());
                            textViewUserId.setText(firebaseUser.getUid());
                            editTextPhone.setVisibility(View.GONE);
                            confirmPhoneNumberButton.setVisibility(View.GONE);

                            //remove the email_name_dialog from the view (so that you could re-do the dialog):
                            ViewGroup parent = (ViewGroup) emailAndNameDialogView.getParent();
                            if (parent != null) {
                                parent.removeView(emailAndNameDialogView);
                            }

                            //update the user's info in the database:
                            // (if this is a new user, it will automatically create it in the users node.)
                            usersDatabaseReference.child(firebaseUser.getUid()).setValue(firebaseUser.getDisplayName());

                        }
                    });

            //verify the user's Email:
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {//TODO: make it work, i dont think it's working properly now.
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //Toast.makeText(getContext(), "Verification E-mail sent.", Toast.LENGTH_SHORT).show();
                    //update the user in firebase: (maybe also here? i'll work on E-mail verification later.)
                    //updateUserInDatabase
                }
            });
            //update the user in firebase:
            mAuth.updateCurrentUser(firebaseUser);

            dialog.dismiss();
        });
        emailAndNameDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            //dismiss the dialog:
            dialog.dismiss();
        }).create();


        //check if the user is already signed-in:
        if(mAuth.getCurrentUser() != null){
            //the user is signed in:
            isSignedInView.setText("The user is signed in");
            //get the user:
            firebaseUser = mAuth.getCurrentUser();
            //update the views:
            try{
                userName.setText(firebaseUser.getDisplayName());
                userEmail.setText(firebaseUser.getEmail());
                textViewUserId.setText(firebaseUser.getUid());
                textViewUserId.setVisibility(View.VISIBLE);
                updateInfoButton.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.VISIBLE);
                editTextPhone.setVisibility(View.GONE);
                confirmPhoneNumberButton.setVisibility(View.GONE);


            } catch (NullPointerException e){
                //this means that the user still hasn't set his name and email.
                //start the name and email dialog:
                Toast.makeText(getContext(), "NullPointerException2", Toast.LENGTH_SHORT).show();
                emailAndNameDialogBuilder.show();
            }

        } else{
            //the user is not signed in yet.
            isSignedInView.setText("The user is not signed in");
            textViewUserId.setVisibility(View.GONE);
            updateInfoButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.GONE);
        }


        //set the onClickListeners:
        //for the confirm phone number and sign-in with phone number button:
        confirmPhoneNumberButton.setOnClickListener(v -> {
            if(!isSignInButton){ //if the button is to send the SMS, not to sign-in.
                //retrieve the phone number from the EditText:
                phoneNumber = editTextPhone.getText().toString(); //the FirebaseAuth requires a phone number with a country code.
                //TODO: enable Israel in the firebase webpage, and make it work also when you type 05x-xxx-xxxx.
                Log.d("PhoneAuth", "the phone number is: " + phoneNumber);

                //some settings for the authentication with firebase via phone number:
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(90L, TimeUnit.SECONDS)
                        .setCallbacks(mPhoneCallbacks)
                        .build();

                //mAuth.setLanguageCode("he");
                //mAuth.useAppLanguage();
                //launch the SMS authentication with firebase:
                PhoneAuthProvider.verifyPhoneNumber(options);
            } else{ //if the button is to sign-in.
                //retrieve the verification code from the EditText:
                userVerificationCode = editTextPhone.getText().toString();
                //start the sign-in process:
                mPhoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId, userVerificationCode);
                signInWithPhoneAuthCredential(mPhoneAuthCredential);
                //update the Views:
                isSignedInView.setText("The user is signed in");
                try{
                    userName.setText(firebaseUser.getDisplayName());
                    userEmail.setText(firebaseUser.getEmail());
                    textViewUserId.setText(firebaseUser.getUid());
                    textViewUserId.setVisibility(View.VISIBLE);
                    updateInfoButton.setVisibility(View.VISIBLE);
                    signOutButton.setVisibility(View.VISIBLE);
                    editTextPhone.setVisibility(View.GONE);
                    confirmPhoneNumberButton.setVisibility(View.GONE);

                } catch (NullPointerException e){
                    //this means that the user still hasn't set his name and email.
                    //start the name and email dialog:
                    Toast.makeText(getContext(), "NullPointerException2", Toast.LENGTH_SHORT).show();
                    emailAndNameDialogBuilder.show();
                }
            }

        });


        //for the sign-in with google button:
        updateInfoButton.setOnClickListener(v -> {
            //set the fields to the curent values from the database:
            editTextName.setText(firebaseUser.getDisplayName());
            editTextEmail.setText(firebaseUser.getEmail());
            //show the update email and name dialog:
            emailAndNameDialogBuilder.show();
        });


        //for the sign-out button:
        signOutButton.setOnClickListener(v -> {
            //preform sign-out:
            mAuth.signOut();
            //update the views:
            isSignedInView.setText("the user is not signed in");
            userName.setText("");
            userEmail.setText("");
            //update the views used for sign-in:
            editTextPhone.setVisibility(View.VISIBLE);
            confirmPhoneNumberButton.setVisibility(View.VISIBLE);
            textViewUserId.setVisibility(View.GONE);
            updateInfoButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.GONE);
            editTextPhone.setText("");
            editTextPhone.setHint("Your phone number for SMS authentication");
            confirmPhoneNumberButton.setText("Send SMS");
            isSignInButton = false;
        });

        signInEmail.setOnClickListener(v -> {

            sendSignInLink(emailToVerify);

        });



        //important for firebase authentication with phone number: handling possible callbacks for the possible request results
        mPhoneCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //success! the request has been approved, so sign-in with the appropriate credential.
                Toast.makeText(getContext(), "Verification completed successfully", Toast.LENGTH_SHORT).show();
                mPhoneAuthCredential = phoneAuthCredential;
                signInWithPhoneAuthCredential(mPhoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //failed to verify, possibly because of faulty phone number. TODO: handle it with a dialog later.
                Toast.makeText(getContext(), "Verification Failed. please check phone number and try again", Toast.LENGTH_SHORT).show();
                Log.e("PhoneAuth", e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                //very important method. called when the SMS has been sent.
                //the app needs to ask the user for the verification code sent via SMS,
                //and construct a credential with the code and the verificationID.
                Toast.makeText(getContext(), "Code has been sent. Please check your SMS app.", Toast.LENGTH_SHORT).show();
                //save the verificationID and for later use.
                mVerificationId = verificationId;

                //update the editTextPhone to input the verification code by the user:
                editTextPhone.setText("");
                editTextPhone.setHint("Insert Verification Code");
                //update the button to finally sign in:
                confirmPhoneNumberButton.setText("Sign In");
                isSignInButton = true;
            }
        };

        return view;
    }


    //the method to sign the user in with the given phone credential (final step):
    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential){
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                handleSignInResult(task);
            }
        });
    }

    //unified result handle method for both sign-in methods:
    private void handleSignInResult(Task<AuthResult> task){
        if(task.isSuccessful()){
            //great! the user has been approved and the sign in was successful.
            Toast.makeText(getContext(), "Signed-in successfully!", Toast.LENGTH_SHORT).show();
            //insert the user:
            firebaseUser = task.getResult().getUser();
            //update the views:
            isSignedInView.setText("The user is signed in");
            textViewUserId.setVisibility(View.VISIBLE);
            updateInfoButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            userName.setText(firebaseUser.getDisplayName());
            userEmail.setText(firebaseUser.getEmail());
            textViewUserId.setText(firebaseUser.getUid());
            editTextPhone.setVisibility(View.GONE);
            confirmPhoneNumberButton.setVisibility(View.GONE);
            //Done!
        } else{
            //the sign-in has not been approved.
            Toast.makeText(getContext(), "Sign-in failed.", Toast.LENGTH_SHORT).show();
            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                // The verification code entered was invalid:
                Toast.makeText(getContext(), "The verification code is wrong. Try again.", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(getContext(), "Other Error.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Call this when the user clicks a "Send Sign-in Link" button
    public void sendSignInLink(String email) {
        //this.userEmail = email; // Save for later verification

        ActionCodeSettings actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        .setAndroidPackageName(
                                getContext().getPackageName(),
                                true, /* installIfNotAvailable */
                                null /* minimumVersion */)
                        .setHandleCodeInApp(true) // This is crucial for handling in-app
                        .setUrl("https://memocircle-ac0c1.firebaseapp.com") // **YOUR AUTHORIZED DOMAIN HERE**
                        .setDynamicLinkDomain("your-dynamic-link-domain.page.link") // Optional, for custom dynamic links
                        .build();

        mAuth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseEA", "Email sent.");
                        Toast.makeText(getContext(),
                                "Sign-in link sent to " + email,
                                Toast.LENGTH_LONG).show();

                    } else {
                        Log.e("FirebaseEA", "Error sending sign-in link", task.getException());
                        Toast.makeText(getContext(),
                                "Failed to send sign-in link: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Call this in onCreate and onNewIntent to handle the incoming link
    private void handleSignInLink(String link) {
        if (mAuth.isSignInWithEmailLink(link)) {


            if (emailToVerify != null) {
                mAuth.signInWithEmailLink(emailToVerify, link)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("FirebaseEA", "Successfully signed in with email link!");
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Navigate to your main app screen
                                //startActivity(new Intent(ProfileFragment.this, MainActivity.class));
                            } else {
                                Log.e("FirebaseEA", "Error signing in with email link", task.getException());
                            }
                        });
            } else {
                // Email not found in preferences. Ask user to re-enter their email.
                //oast.makeText(this, "Please re-enter your email to complete sign-in.", Toast.LENGTH_LONG).show();
                // You might want to show a dialog here to ask for email
            }
        }
    }

    //TODO: send a dialog that notifies the user about the SMS that is about to be sent.
    //TODO: notify the user that for first-time sign-up, he need to update his info.
}