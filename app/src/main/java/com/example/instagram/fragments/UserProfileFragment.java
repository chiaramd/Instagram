package com.example.instagram.fragments;

import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.BitmapScaler;
import com.example.instagram.EndlessRecyclerViewScrollListener;
import com.example.instagram.MainActivity;
import com.example.instagram.PostGridAdapter;
import com.example.instagram.R;
import com.example.instagram.model.Post;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class UserProfileFragment extends Fragment {
    @BindView(R.id.btnLogout) Button btnLogout;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.tvUsername) TextView tvUsername;
    @BindView(R.id.rvPosts) RecyclerView rvPosts;
    private Unbinder unbinder;

    private final String TAG = "UserProfileFragment";
    private final static int PICK_PHOTO_CODE = 1046;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private String photoFileName = "photo.jpg";
    private boolean profileImageExists;

    private File photoFile;
    private ParseUser user;

    ArrayList<Post> posts;
    PostGridAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private GridLayoutManager gridLayoutManager;

    private boolean scrollable = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        user = ParseUser.getCurrentUser();

        ParseFile profilePic = user.getParseFile("profileImage");
        if (profilePic != null) {
            Glide.with(this)
                    .load(profilePic.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
            profileImageExists = true;
        } else {
            Log.d(TAG, "No profile image");
            Glide.with(this)
                    .load(ContextCompat.getDrawable(getActivity(), R.drawable.layer))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);
            profileImageExists = false;
        }

        tvUsername.setText(user.getUsername());

        posts = new ArrayList<>();
        adapter = new PostGridAdapter(posts);
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rvPosts.setLayoutManager(gridLayoutManager);
        rvPosts.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadMorePosts();
            }
        };
        rvPosts.addOnScrollListener(scrollListener);
        populateTimeline();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btnLogout)
    void logout() {
        Log.d(TAG, "Logging user out...");
        ParseUser.logOut();
        Intent i = new Intent(getActivity(), MainActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.ivProfileImage)
    void addProfile() {
        //TODO - check if profile image exists?
        AlertDialog materialAlertDialog = new MaterialAlertDialogBuilder(getContext())
                .setTitle("Add a profile picture")
                .setNegativeButton("Take a picture", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        launchCamera();
                    }
                })
                .setPositiveButton("Choose an image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pickPhoto();
                    }
                })
                .show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Image taken!");


                Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());
                setProfileImage(takenImage);
            } else {
                Log.d(TAG, "Image was not taken");
            }
        } else
        if (requestCode == PICK_PHOTO_CODE) {
            Log.d(TAG, "Photo picked");
            if (data != null) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setProfileImage(selectedImage);

            } else {
                Log.d(TAG, "No data");
            }
        }
    }

    private void setProfileImage(Bitmap bitmap) {
        Log.d(TAG, "Setting profile image...");
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(bitmap, 300);
        // write the smaller bitmap back to disk
        // configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File resizedFile = getPhotoFileUri(photoFileName + "_resized");
        try {
            resizedFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(resizedFile);
            // write the bytes of the bitmap to file
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error resizing image");
            e.printStackTrace();
        }

        // load resized image into preview
        /*ivPreview.setImageBitmap(resizedBitmap);
        parseFile = new ParseFile(resizedFile);*/
        ParseFile parseFile = new ParseFile(resizedFile);
        user.put("profileImage", parseFile);
        user.saveInBackground();
        profileImageExists = true;
        Glide.with(this)
                .load(resizedBitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfileImage);
    }

    // Returns the file for a photo stored on disk given the filename
    private File getPhotoFileUri(String filename) {
        // access package-specific directories without requesting external read/write runtime permissions
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // create storage directory if it doesn't exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }
        // return file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + filename);
        return file;
    }

    private void pickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    private void launchCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri("pho to.jpg");

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // the app will crash if no app can handle the intent
        if (i.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private Bitmap rotateBitmapOrientation(String photoFilePath) {
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

    private void populateTimeline() {
        Log.d(TAG, "Populating timeline...");
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getUserPosts().getTop().withUser();

        postsQuery.findInBackground((objects, e) -> {
            if (e == null) {
                adapter.clear();
                adapter.addAll(objects);
            } else {
                e.printStackTrace();
            }
        });
    }

    private void loadMorePosts() {
        if (scrollable) {
            scrollable = false;
            Log.d(TAG, "Loading more posts...");
            final Post.Query postsQuery = new Post.Query();
            postsQuery.getUserPosts().getNext(posts.size()).withUser();


            postsQuery.findInBackground((objects, e) -> {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        posts.add(objects.get(i));
                        adapter.notifyItemInserted(posts.size() - 1);
                    }
                } else {
                    e.printStackTrace();
                }
                scrollable = true;
            });
        }
    }
}
