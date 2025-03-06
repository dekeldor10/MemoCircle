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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
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
    byte[] resultByteArr;
    public Uri imageUri;

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



            //launch the camera app (activity for result):
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            takePictureActivityResultLauncher.launch(intent);

             */
            //first, check if the all the permission needed are granted (using a foreach loop):
            for(String permission : new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}){
                if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{permission}, 1);
                }
            }
            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //takePictureActivityResultLauncher.launch(intent);

            openCamera();
        });

        //For the finished product, i'll use a textWatcher and a Handler for saving or updating the note when the user
        //stops writing. for now, i'll just use a button, due to lack of time.
        /*
        titleEditText.addTextChangedListener(textWatcherTitle);
        textContentEditText.addTextChangedListener(textWatcherTextContent);

         */

    }


    //defining the ActivityForResult launcher & showing the uri via the ImageView:
    //This is the ActivityForResult for the image capture and handling the image Uri after taking the picture:
    ActivityResultLauncher<Intent> takePictureActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            //do stuff with the result:
            //first, make sure it's OK and there is data.
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                //use URI for a higher-quality image.
                //does not work when trying to capture an image with WRITE_EXTERNAL_STORAGE! I'll use the recommended method for it.
                //Uri imageUri = result.getData().getData();
                try {
                    testingImageView.setImageBitmap(uriToBitmap(imageUri));
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
                if(result.getResultCode() == RESULT_OK){
                    //great, the user gave permission. Launch the camera:
                    //openCamera();
                } else{
                    //the user did not give permission.
                    Toast.makeText(NoteActivity.this, "Uri access permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
    );


    //Launchers:
    //If the user wants to add from the gallery:

    public void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //launch this intent.
        takePictureActivityResultLauncher.launch(galleryIntent);
    }
    //If the user wants to take a picture via camera:
    public void openCamera() {
        //create the specific Uri for the image the user ia about to take:
        //Uri imageUri;
        try {
            imageUri = createImageUri();
        } catch (Exception e) {
            //e.printStackTrace();
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


    //bundle to byte[] converter (I store the image in byte[] format. it is way better):
    /*
    public byte[] BundleToByteArray(Bundle bundle){
        //use Parcel as a medium.
        Parcel parcel = Parcel.obtain();
        bundle.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

     */





    //conversions:

    //byte[] to Bitmap (for showing the image in an ImageView):
    public Bitmap byteArrayToBitmap(byte[] byteArr){
        return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
    }

    //Bitmap to byte[]:
    public byte[] BitmapToByteArray(Bitmap bitmap){
        //Create a ByteArrayOutputStream to write the Bitmap data
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //Compress the Bitmap into the ByteArrayOutputStream (PNG format)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //Convert the stream to a byte array and return it
        return stream.toByteArray();
    }
    /*
    //Uri to bitmap (old method, i wrote a better one below):
    public Bitmap uriToBitmap(Uri uri){
        Bitmap bitmap;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(NoteActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
        return bitmap;

    }

    */

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