package com.dordekel.memocircle;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class NoteActivity extends AppCompatActivity {

    ImageView testingImageView;
    String resultImgUriString;
    public Uri imageUri;
    int noteID;
    boolean isNewNote;
    Note existingNote;


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
        Button deleteButton = findViewById(R.id.deleteButton);
        ImageButton addImageButton = findViewById(R.id.addImageButton);
        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText textContentEditText = findViewById(R.id.textContentEditText);
        testingImageView = findViewById(R.id.testingImageView);

        //by default, the DeleteButton should not be visible (you cant delete a new note, can you?)
        //the ImageView should be too - if there isn't an image it's just taking space.
        deleteButton.setVisibility(View.GONE);
        testingImageView.setVisibility(View.GONE);

        //initiate the database and DAO
        AppDatabase db = AppDatabase.getInstance(this);
        NoteDao noteDao = db.noteDao();

        //variables and things to do when this isn't a new note:
        //set local isNewNote to the one provided by the noteIntent (will be used in this several times):
        isNewNote = getIntent().getBooleanExtra("isNewNote", false);

        //if this isn't a new note, get the noteID from the intent:
        if(!isNewNote){
            noteID = getIntent().getIntExtra("noteID", 1);

            //make the deleteButton visible:
            deleteButton.setVisibility(View.VISIBLE);

            //set all the EditTexts and the ImageView to the existing note's values:
            existingNote = noteDao.getNoteByNoteID(noteID);
            titleEditText.setText(existingNote.getTitle());
            textContentEditText.setText(existingNote.getTextContent());

            if(existingNote.getImgUriString() != null){
                try{
                    testingImageView.setImageBitmap(uriToBitmap(Uri.parse(existingNote.getImgUriString())));
                    testingImageView.setVisibility(View.VISIBLE);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            //update the text on the saveNoteButton to "update note":
            saveNoteButton.setText("Update Note");
        }

        //Check if the all the permission needed are granted (using a foreach loop):
        for(String permission : new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{permission}, 1);
            }
        }

        //set the onClickListeners
        returnButton.setOnClickListener(v -> {
            //return to MainActivity
            Intent returnIntent = new Intent(NoteActivity.this, MainActivity.class);
            startActivity(returnIntent);
            //delete this activity and the stack behind it, for optimal user experience.
            finishAffinity();
        });

        deleteButton.setOnClickListener(v -> {
            //delete the note from the database (reminder - this button only shows up if this is an existing note):
            noteDao.deleteNote(existingNote);
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


            //check is this is a new note or an existing one:
            if(isNewNote){
                //this is a NEW note. create it.
                //create the note with the resultImgUtiString. if the used did not take a picture, it will be null (which is fine).
                Note note = new Note(noteTitle, textContent, resultImgUriString);

                //save the note to the database:
                //when using Room, the note has to be created by a Thread.
                new Thread(() -> {
                    noteDao.insertNote(note);
                    //display note in logCat (for debugging purposes):
                    System.out.println(noteDao.getAllNotes()[0].getTextContent());
                }).start();

                Toast.makeText(NoteActivity.this, " New note Saved!", Toast.LENGTH_SHORT).show();
            }else{
                //again, when using Room it's recommended to use a Thread
                new Thread(() -> {
                    //this is an EXISTING note. update it.
                    existingNote.setTitle(noteTitle);
                    existingNote.setTextContent(textContent);
                    existingNote.setImgUriString(resultImgUriString);
                    noteDao.updateNote(existingNote);
                }).start();

                Toast.makeText(NoteActivity.this, " Note Updated!", Toast.LENGTH_SHORT).show();
            }


        });

        addImageButton.setOnClickListener(view -> {
            //here, a dialog will show up asking the user if he wants to use the gallery or take a picture.

            AlertDialog.Builder cameraOrGalleryDialogBuilder = new AlertDialog.Builder(NoteActivity.this);
            cameraOrGalleryDialogBuilder.setTitle("Add Image");
            cameraOrGalleryDialogBuilder.setMessage("Do you want to take a picture or choose from the gallery?");
            cameraOrGalleryDialogBuilder.setPositiveButton("Camera", (dialog, which) -> {
                openCamera();
            });
            cameraOrGalleryDialogBuilder.setNegativeButton("Gallery", (dialog, which) -> {
                openGallery();
            });
            cameraOrGalleryDialogBuilder.setNeutralButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });
            cameraOrGalleryDialogBuilder.show();
        });
    }


    //defining the ActivityForResult launcher & showing the uri via the ImageView:
    //This is the ActivityForResult for the image capture and handling the image Uri after taking the picture:
    ActivityResultLauncher<Intent> takePictureActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            //do stuff with the result:
            //first, make sure it's OK and there is data.
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                //if the user chose to get a picture from the gallery, the imageUri is empty.
                //fix it simply by getting the Uri from the result of the activityForResult:
                if(imageUri == null){
                    imageUri = result.getData().getData();
                }

                //from here, it is the same no matter the method the user chose to pick the image.
                //use URI as google suggests (bonus: gives higher-definition pictures).
                try {
                    testingImageView.setImageBitmap(uriToBitmap(imageUri));
                    resultImgUriString = imageUri.toString();
                    testingImageView.setVisibility(View.VISIBLE);
                    Toast.makeText(this, resultImgUriString, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    );


    //ActivityForResult with the Photo Picker API, as recommended by Google when the user wants to use the gallery:
    ActivityResultLauncher<PickVisualMediaRequest> pickMediaActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.PickVisualMedia(), result -> {
            //check if the result is fine and usable:
            if(result != null){
                imageUri = result;

                //grant the permission to permanently access the Uri:
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    testingImageView.setImageBitmap(uriToBitmap(imageUri));
                    resultImgUriString = imageUri.toString();
                    testingImageView.setVisibility(View.VISIBLE);
                    Toast.makeText(this, resultImgUriString, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "not", Toast.LENGTH_SHORT).show();
                }
            }
        }
    );

    //This is the ActivityForResult for asking the user for the permission to access the Uri (mandatory for security reasons - Scoped Storage):
    ActivityResultLauncher<IntentSenderRequest> requestUriAccessActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            //make sure the result is OK:
            if(result.getResultCode() != RESULT_OK){
                //the user did not give permission :(
                Toast.makeText(NoteActivity.this, "Uri access permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    );



    //Launchers:
    //If the user wants to add from the gallery:

    public void openGallery(){
        //this API doesn't use an intent, but a specific PickVisualMediaRequest.
        pickMediaActivityResultLauncher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }
    //If the user wants to take a picture via camera:
    public void openCamera() {
        //create the specific Uri for the image the user ia about to take:
        try {
            imageUri = createImageUri();
        } catch (Exception e) {
            Toast.makeText(NoteActivity.this, "Error creating image Uri in realtime", Toast.LENGTH_SHORT).show();
            return;
        }

        //request the Uri access:
        requestUriAccess(imageUri);

        //launch the camera itself:
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Tell the intent to save the picture to the specifically-generated Uri:
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //launch the whole operation:
        takePictureActivityResultLauncher.launch(cameraIntent);

    }



    //Image uri methods (cannot be done in a separate class: some of the functions are non-static):

    //request uri access from user (mandatory for version later than android 10 (api29) - Scoped Storage for security reasons):
    public void requestUriAccess(Uri uri){
        List<Uri> uris = Collections.singletonList(uri);
        PendingIntent writeRequestPendingIntent = MediaStore.createWriteRequest(getContentResolver(), uris);

        IntentSenderRequest requestUriAccess = new IntentSenderRequest.Builder(writeRequestPendingIntent.getIntentSender()).build();

        requestUriAccessActivityResultLauncher.launch(requestUriAccess);
    }

    //create the file Uri for the image the user will capture:
    public Uri createImageUri(){
        Uri uri;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "MemoCircleImg_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if(uri == null){
            Toast.makeText(NoteActivity.this, "Error creating image Uri", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(NoteActivity.this, "Uri created successfully", Toast.LENGTH_SHORT).show();
        }

        return uri;
    }



    //conversions:

    //Uri to Bitmap (with correct orientation):
    public Bitmap uriToBitmap(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap resultBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        //get the file path itself from the uri: (can't use uri.getPath(), as this method is deprecated in api 29 and above)
        String filePath = getPathFromUri(uri);

        //the image comes with EXIF data, which contains the orientation of the picture.
        //BitmapFactory does not access this data. so, I have to get the picture orientation myself, and fix it correctly.
        if(filePath != null){
            int rotation = getImageRotation(filePath);
            if(rotation != 0){
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                resultBitmap = Bitmap.createBitmap(resultBitmap, 0, 0, resultBitmap.getWidth(), resultBitmap.getHeight(), matrix, true);
            }

        }
        return resultBitmap;
    }

    private String getPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        String filePath = null;

        assert cursor != null;
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        if(columnIndex != -1){
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private int getImageRotation(String filePath) throws IOException {
        ExifInterface exifInterface = new ExifInterface(filePath);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
    //use textWatcher for saving the note automatically. TODO: add a textWatcher in the future.

}