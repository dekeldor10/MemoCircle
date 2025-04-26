package com.dordekel.memocircle;

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    //variables for google sign-in (old):
    private static final String TAG = "GoogleSignIn";
    private static final String CLIENT_ID = "656786178012-a0cef1oek69fknl4dn9ig9lejh725ooh.apps.googleusercontent.com";

    //new variables for authentication with firebase:
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthCredential mPhoneAuthCredential;
    private CredentialManager credentialManager;
    private SignInClient signInClient;
    static FirebaseUser firebaseUser; //not originally static, make sure it doesn't cause problems.
    FirebaseDatabase database;
    DatabaseReference usersDatabaseReference;
    String phoneNumber;
    String mVerificationId;
    String userVerificationCode;
    boolean isSignInButton = false;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //declare all the views objects:
        TextView userName = view.findViewById(R.id.userName);
        TextView userEmail = view.findViewById(R.id.userEmail);
        TextView isSignedInView = view.findViewById(R.id.isSignedIn);
        TextView textViewUserId = view.findViewById(R.id.textViewUserId);
        EditText editTextPhone = view.findViewById(R.id.editTextPhone);
        Button confirmPhoneNumberButton = view.findViewById(R.id.confirmPhoneNumberButton);
        Button signOutButton = view.findViewById(R.id.signOutButton);
        Button updateInfoButton = view.findViewById(R.id.updateInfoButton);

        //instance the mAuth as an instance of FirebaseAuth:
        mAuth = FirebaseAuth.getInstance();
        //instance the CredentialManager:
        credentialManager = CredentialManager.create(requireContext());
        //instance the FirebaseDatabase:
        database = FirebaseDatabase.getInstance("https://memocircle-ac0c1-default-rtdb.europe-west1.firebasedatabase.app/"); //the correct URL of the database.
        //instance the DatabaseReference for the users:
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

            //notify the user:
            //Toast.makeText(getContext(), "Name and e-mail updated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        emailAndNameDialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {
            // TODO: notify the user that this is important in another dialog. i dont have time for this now.
            //dismiss the dialog:
            dialog.dismiss();
        }).create();


        //check if the user is already signed-in:
        if(mAuth.getCurrentUser() != null){
            //the user is signed in:
            isSignedInView.setText("the user is signed in");
            //get the user:
            firebaseUser = mAuth.getCurrentUser();
            //update the views:
            try{
                userName.setText(firebaseUser.getDisplayName());
                userEmail.setText(firebaseUser.getEmail());
                textViewUserId.setText(firebaseUser.getUid());
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
            isSignedInView.setText("the user is not signed in");
        }


        //set the onClickListeners:
        //for the confirm phone number and sign-in with phone number button:
        confirmPhoneNumberButton.setOnClickListener(v -> {
            if(!isSignInButton){ //if the button is to send the SMS, not to sign-in.
                //retrieve the phone number from the EditText:
                phoneNumber = editTextPhone.getText().toString(); //the FirebaseAuth requires a phone number with a country code.
                //TODO: enable Israel in the firebase webpage, and make it work also when you type 05x-xxx-xxxx.

                //some settings for the authentication with firebase via phone number:
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(90L, TimeUnit.SECONDS)
                        .setCallbacks(mPhoneCallbacks)
                        .build();

                mAuth.setLanguageCode("he");
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
                isSignedInView.setText("the user is signed in");
                try{
                    userName.setText(firebaseUser.getDisplayName());
                    userEmail.setText(firebaseUser.getEmail());
                    textViewUserId.setText(firebaseUser.getUid());
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
            //show the update email and name dialog:
            emailAndNameDialogBuilder.show();




            //start a Google sign-in request:
                        /*
                        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(CLIENT_ID)
                                .build();

                        //create the request with Credential:
                        GetCredentialRequest request = new GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build();

                         */


            //signInWithGoogleId(request);

            /*
            //start the sign-in process:
            credentialManager.getCredentialAsync(
                    request,
                    requireActivity(), // This must be a FragmentActivity!
                    ContextCompat.getMainExecutor(requireContext())
            ).thenAccept(result -> {
                Credential credential = result.getCredential();

                if (credential instanceof PublicKeyCredential) {
                    // This is WebAuthn
                } else if (credential instanceof GoogleIdTokenCredential) {
                    String idToken = ((GoogleIdTokenCredential) credential).getIdToken();

                    // Sign in with Firebase
                    authorizeWithFirebaseGoogle(idToken);
                }
            }).exceptionally(e -> {
                Log.e("Credential", "Error: " + e);
                return null;
            });

             */



            //start the sign-in process with a CredentialManager and it's callbacks:
            //unfortunately, the getCredential method has some Kotlin-specific requirements and code, making it unavailable to use here..
            //I'll bypass that with Java code, using Task from Google Play services API - which represents an asynchronous operation..

            //in green: the code i shared with StackOverflow.
            /**
            if(isAdded()){
                getGoogleCredentialTask(requireContext())
                        .addOnSuccessListener(response -> {
                            Credential credential = response.getCredential();
                            signInWithGoogleId(credential);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Google sign-in failed" + e);
                            Toast.makeText(getContext(), "Google sign-in failed: " + e, Toast.LENGTH_SHORT).show();
                        });
            }

             **/



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
            editTextPhone.setText("");
            editTextPhone.setHint("Your phone number for SMS authentication");
            confirmPhoneNumberButton.setText("send SMS");
            isSignInButton = false;
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
                Toast.makeText(getContext(), "Code has been sent. lease check your SMS app.", Toast.LENGTH_SHORT).show();
                //save the verificationID and the forceResendingToken for later use.
                mVerificationId = verificationId;
                mResendToken = forceResendingToken;

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


    /**
    //the method to initiate google sign-in with the given GoogleID credential:
    private void signInWithGoogleId(Credential credential){
        //make sure the given credential is a GoogleID credential: (might need to update the Java JDK)
        if(credential instanceof CustomCredential && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)){
            //create GoogleID token from the credential:
            CustomCredential customCredential = (CustomCredential) credential;
            Bundle bundle = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(bundle);

            //authorize with firebase: with a method.
            authorizeWithFirebaseGoogle(googleIdTokenCredential.getIdToken());

        } else{
            Toast.makeText(getContext(), "Credential type is not of GoogleID!", Toast.LENGTH_SHORT).show();
        }

    }
     **/

    /**
    //the method handling and returning the appropriate Task for the googleID sign-in process:
    private Task<GetCredentialResponse> getGoogleCredentialTask(Context context) {
        TaskCompletionSource<GetCredentialResponse> taskCompletionSource = new TaskCompletionSource<>();

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Executor executor = context != null ? ContextCompat.getMainExecutor(context) : Runnable::run;

        credentialManager.getCredential(requireActivity(), request)
                .addOnSuccessListener(executor, taskCompletionSource::setResult)
                .addOnFailureListener(executor, taskCompletionSource::setException);


        return taskCompletionSource.getTask();
    }
     **/


    //the method to sign the user in with the given phone credential (final step):
    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential){
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                handleSignInResult(task);
            }
        });
    }
    /**
    //the method to authorize with firebase with a GoogleID token (final step):
    private void authorizeWithFirebaseGoogle(String idToken){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                handleSignInResult(task);
            }
        });
    }
     **/


    /** this one too:**/
    //unified result handle method for both sign-in methods:
    private void handleSignInResult(Task<AuthResult> task){
        if(task.isSuccessful()){
            //great! the user has been approved and the sign in was successful.
            Toast.makeText(getContext(), "Signed-in successfully! P", Toast.LENGTH_SHORT).show();
            //insert the user:
            firebaseUser = task.getResult().getUser();
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

    //TODO: send a dialog that notifies the user about the SMS that is about to be sent.
    //TODO: notify the user that for first-time sign-up, he need to update his info.
}