package com.dordekel.memocircle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;

public class NoteActivity extends AppCompatActivity {

    ImageView testingImageView;
    byte[] resultByteArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //declare the buttons
        Button returnButton = findViewById(R.id.returnButton);
        Button saveNoteButton = findViewById(R.id.saveNoteButton);
        ImageButton addImageButton = findViewById(R.id.addImageButton);
        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText textContentEditText = findViewById(R.id.textContentEditText);
        testingImageView = findViewById(R.id.testingImageView);

        //initiate the database and DAO
        AppDatabase db = AppDatabase.getInstance(this);
        NoteDao noteDao = db.noteDao();



        //set the onClickListeners
        returnButton.setOnClickListener(v -> {
            //return to MainActivity
            Intent returnIntent = new Intent(NoteActivity.this, MainActivity.class);
            startActivity(returnIntent);
            //delete this activity and the stack behind it, for optimal user experience.
            finishAffinity();
        });

        saveNoteButton.setOnClickListener(v -> {
            //get the texts from the editTexts:
            String noteTitle = titleEditText.getText().toString();
            String textContent = textContentEditText.getText().toString();

            //create the note with the resultByteArr. if the used did not take a picture, it will be null, which is fine.
            Note note = new Note(noteTitle, textContent, resultByteArr);

            //save the note to the database:
            //when using Room, the note has to be created by a Thread.
            new Thread(() -> {
                noteDao.insertNote(note);
                //display note in logCat (for debugging purposes):
                System.out.println(noteDao.getAllNotes()[0].getTextContent());
            }).start();

            Toast.makeText(NoteActivity.this, "Note Saved!", Toast.LENGTH_SHORT).show();

        });

        addImageButton.setOnClickListener(view -> {
            /*
            //first, check if the permission needed for taking a picture is granted:
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //if not, request it:
                ActivityCompat.requestPermissions(NoteActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }

            //launch the camera app (activity for result):
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            activityResultLauncher.launch(intent);

             *///newer solution, works:
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                // If not, request permissions
                ActivityCompat.requestPermissions(NoteActivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES}, 1);
            } else {
                // Launch the camera if permissions are granted
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                activityResultLauncher.launch(intent);
            }
        });

        //For the finished product, i'll use a textWatcher and a Handler for saving or updating the note when the user
        //stops writing. for now, i'll just use a button, due to lack of time.
        /*
        titleEditText.addTextChangedListener(textWatcherTitle);
        textContentEditText.addTextChangedListener(textWatcherTextContent);

         */

    }


    //defining the ActivityForResult launcher:
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                //do stuff with the result:
                //first, make sure it's OK and there is data.
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    //use Bundle because the data received in this result comes in the Bundle format.
                    //Bundle resultBundle = result.getData().getExtras();
                    //change to Bitmap in order to show the image via ImageView:
                    /*
                    byte[] byteArr = BundletoByteArray(resultBundle);
                    System.out.println(byteArr.length);
                    Bitmap resultBitmap = byteArrayToBitmap(byteArr);
                    if(resultBitmap == null){
                        System.out.println("resultBitmap is null");
                    }

                     */
                    Bitmap resultBitmap = (Bitmap) result.getData().getExtras().get("data");
                    resultByteArr = BitmaptoByteArray(resultBitmap);


                    //Bitmap resultBitmap = (Bitmap) resultBundle.get("data");
                    testingImageView.setImageBitmap(resultBitmap);
                }
            });

    //bundle to byte[] converter (I store the image in byte[] format. it is way better):
    /*
    public byte[] BundletoByteArray(Bundle bundle){
        //use Parcel as a medium.
        Parcel parcel = Parcel.obtain();
        bundle.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

     */

    //byte[] to Bitmap (for showing the image in an ImageView):
    public Bitmap byteArrayToBitmap(byte[] byteArr){
        return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
    }

    //Bitmap to byte[]:
    public byte[] BitmaptoByteArray(Bitmap bitmap){
        //Create a ByteArrayOutputStream to write the Bitmap data
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //Compress the Bitmap into the ByteArrayOutputStream (PNG format)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //Convert the stream to a byte array and return it
        return stream.toByteArray();
    }










    /*
    //The textWatcher fot the title EditText
    TextWatcher textWatcherTitle = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        //I will be using this method for creating / updating the note when the user stops writing. Just like in Google Keep.
        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    //The textWatcher for the textContent EditText
    TextWatcher textWatcherTextContent = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        //again, like the title, I will be using this method for creating / updating the note when the user stops writing.
        @Override
        public void afterTextChanged(Editable editable) {
            Toast.makeText(NoteActivity.this, "done editing textContent", Toast.LENGTH_SHORT).show();
        }
    };

     */

}