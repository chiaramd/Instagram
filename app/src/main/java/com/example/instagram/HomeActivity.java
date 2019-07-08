package com.example.instagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.instagram.model.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {
    @BindView(R.id.etDescription) EditText etDescriptionInput;
    @BindView(R.id.btnCreate) Button btnCreate;
    @BindView(R.id.btnRefresh) Button btnRefresh;
    @BindView(R.id.ivPreview) ImageView ivPreview;

    private final String TAG = "HomeActivity";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
//    private static final String imagePath = "instaChiara/dc0b49714b107d7f780d7ba5d8e4fec5_video-vector-icon-png_267481.jpg";
//http://chiaramd-fbu-instagram.herokuapp.com/parse/files/instaChiara/dc0b49714b107d7f780d7ba5d8e4fec5_video-vector-icon-png_267481.jpg
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

//        loadTopPosts();
    }

    @OnClick(R.id.btnCreate)
    public void getPostInfo() {
        Log.d(TAG, "Creating post...");
//        final String description = etDescriptionInput.getText().toString();
//        final ParseUser user = ParseUser.getCurrentUser();
        launchCamera();
//        final File file = new File(imagePath);
//        final ParseFile parseFile = new ParseFile(file);

//        createPost(description, parseFile, user);
    }
//should this be public??
    private void launchCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri("pho to.jpg");

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(HomeActivity.this, "com.codepath.fileprovider", photoFile);
        i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // the app will crash if no app can handle the intent
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the file for a photo stored on disk given the filename
    public File getPhotoFileUri(String filename) {
        // access package-specific directories without requesting external read/write runtime permissions
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // create storage directory if it doesn't exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }

        // return file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + filename);
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Image taken!");
                // now we have the camera photo on disk
//                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());
                // resize bitmap
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, 300);
                // write the smaller bitmap back to disk
                // configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // create new file for resized bitmap
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    // write the bytes of the bitmap to file
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // load resized image into preview
                ivPreview.setImageBitmap(resizedBitmap);
                // TODO - resize bitmap
                // TODO- load taken image into a preview with .setImageBitmap(takenImage);
                final String description = etDescriptionInput.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();
                final ParseFile parseFile = new ParseFile(resizedFile);
                createPost(description, parseFile, user);

            } else {
                Log.d(TAG, "Image was not taken");
            }
        }
    }

    @OnClick(R.id.btnRefresh)
    public void refresh() {
        Log.d(TAG, "Refreshing posts...");
        loadTopPosts();
    }

    private void createPost(String description, ParseFile imageFile, ParseUser user) {
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(imageFile);
        newPost.setUser(user);

        newPost.saveInBackground(e -> {
            if (e == null) {
                Log.d(TAG, "Create post success!");
            } else {
                e.printStackTrace();
            }
        });
    }

    private void loadTopPosts() {
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop().withUser();

        postsQuery.findInBackground((objects, e) -> {
            if (e == null) {
                for (int i = 0; i < objects.size(); ++i) {
                    Log.d(TAG, "Post[" + i + "] = " + objects.get(i).getDescription() + "\nusername = " + objects.get(i).getUser().getUsername());
                }
            } else {
                e.printStackTrace();
            }
        });
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }
}

//accesing files:
//// getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
//File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
//
//// wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
//bmpUri = FileProvider.getUriForFile(MyActivity.this, "com.codepath.fileprovider", file);
