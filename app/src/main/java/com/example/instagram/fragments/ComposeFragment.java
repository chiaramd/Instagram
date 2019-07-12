package com.example.instagram.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagram.BitmapScaler;
import com.example.instagram.R;
import com.example.instagram.model.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.glide.transformations.CropSquareTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ComposeFragment extends Fragment {

    @BindView(R.id.etDescription) EditText etDescriptionInput;
    @BindView(R.id.btnCreate) Button btnCreate;
    @BindView(R.id.ivPreview) ImageView ivPreview;
    @BindView(R.id.pbLoading) ProgressBar pbLoading;
    @BindView(R.id.view_finder) TextureView textureView;
    @BindView(R.id.ivCapture) ImageView ivCaptureImage;
    @BindView(R.id.ivBlackBar) ImageView ivBlackBar;
    @BindView(R.id.ivThumbnail) ImageView ivThumbnail;
    private Unbinder unbinder;

    private String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private String[] GALLERY_REQ_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    private Activity activity;
    private Context context;
    private final String TAG = "ComposeFragment";
    // Arbitrary number to track permissions request for camera
    private final static int REQUEST_CODE_PERMISSIONS = 10;
    private final static int GALLERY_REQUEST_CODE_PERMISSIONS = 20;
    private final static int SELECT_GALLERY_PHOTO_CODE = 1046;
    private String photoFileName = "photo.jpg";
    private File photoFile;
    private ParseFile parseFile;

    // onCreateView method is called when Fragment should create its View object hierarchy, either dynamically or via XML layout inflation
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // setup handles to view objects here
        unbinder = ButterKnife.bind(this, view);

        activity = getActivity();
        context = getContext();
        setCameraView(true);

        if (cameraPermissionsGranted()) {
            textureView.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(activity, REQUEST_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        // every time the texture view changes, recompute layout
        textureView.addOnLayoutChangeListener((view1, i, i1, i2, i3, i4, i5, i6, i7) -> updateTransform());

        if (galleryPermissionsGranted()) {
            setGalleryThumbnail();
        } else {
            ActivityCompat.requestPermissions(activity, GALLERY_REQ_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_GALLERY_PHOTO_CODE) {
            if (data != null) {
                setCameraView(false);
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setPreview(selectedImage);

            } else {
                Log.d(TAG, "No data");
                Toast.makeText(getContext(), "No image selected. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.btnCreate)
    void post() {
        final String description = etDescriptionInput.getText().toString();
        final ParseUser user = ParseUser.getCurrentUser();
        createPost(description, parseFile, user);
    }

    @OnClick(R.id.ivPreview)
    void takeNewPhoto() {
        setCameraView(true);
    }

    @OnClick(R.id.ivThumbnail)
    void chooseGalleryPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, SELECT_GALLERY_PHOTO_CODE);
        }
    }

    private void setPreview(Bitmap bitmap) {

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        Glide.with(this)
                .load(bitmap)
                .apply(bitmapTransform(new CropSquareTransformation()))
                .apply(new RequestOptions().override(width, width))
                .into(ivPreview);
        Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(bitmap, 300);
        // Write the smaller bitmap back to disk
        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File resizedFile = getPhotoFileUri(photoFileName + "_resized");
        try {
            resizedFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(resizedFile);
            // Write the bytes of the bitmap to file
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error resizing image");
            e.printStackTrace();
        }
//        ivPreview.setImageBitmap(resizedBitmap);
        parseFile = new ParseFile(resizedFile);
    }

    private void createPost(String description, ParseFile imageFile, ParseUser user) {
        pbLoading.setVisibility(ProgressBar.VISIBLE);
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(imageFile);
        newPost.setUser(user);
        newPost.saveInBackground(e -> {
            pbLoading.setVisibility(ProgressBar.INVISIBLE);
            if (e == null) {
                Log.d(TAG, "Post created successfully");
                startTimelineFragment();
            } else {
                Log.e(TAG, "Post not created");
                e.printStackTrace();
            }
        });
    }

    private void startTimelineFragment() {
        Fragment fragment = new TimelineFragment();
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "FRAGMENT_TAG").commit();
    }

    // Returns the file for a photo stored on disk given the filename
    private File getPhotoFileUri(String filename) {
        // Access package-specific directories without requesting external read/write runtime permissions
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create storage directory if it doesn't exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }
        // Return file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + filename);
        return file;
    }

    private Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);

        // Rotate bitmap
        int rotationAngle = getRotationAngle(photoFilePath);
        Matrix matrix = new Matrix();
        try {
            matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        return rotatedBitmap;
    }

    private int getRotationAngle(String photoFilePath) {
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert exif != null;
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        return rotationAngle;
    }

    private void setGalleryThumbnail() {
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Glide.with(context)
                    .load(image)
                    .into(ivThumbnail);
            Log.d(TAG, "Thumbnail loaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methods for CameraX implementation here
    private void startCamera() {
        // Create configuration object for viewfinder use case
        Rational aspectRatio = new Rational(1,1);
        Size size = new Size(400,400);
        PreviewConfig previewConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(size).build();

        //Build the viewfinder use case
        Preview preview = new Preview(previewConfig);

        // Update surface texture each time output is updated
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup parent = (ViewGroup) textureView.getParent();
            parent.removeView(textureView);
            parent.addView(textureView, 4);
            textureView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        // Configuration for image capture use case
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setTargetAspectRatio(aspectRatio).setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY).build();

        ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);

        ivCaptureImage.setOnClickListener(view -> {
            photoFile = getPhotoFileUri(photoFileName);
            imageCapture.takePicture(photoFile, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    Log.d(TAG, "Image taken");
                    Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());
                    setPreview(takenImage);
                    setCameraView(false);
                }
                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    Log.e(TAG, "Image not taken");
                    assert cause != null;
                    cause.printStackTrace();
                }
            });
        });

        CameraX.bindToLifecycle(this, preview, imageCapture);
    }

    // CameraX viewfinder transformations
    private void updateTransform() {
        Matrix matrix = new Matrix();
        float centerX = textureView.getWidth() / 2f;
        float centerY = textureView.getHeight() / 2f;
        matrix.postRotate(0, centerX, centerY);
        textureView.setTransform(matrix);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (cameraPermissionsGranted()) {
                textureView.post(this::startCamera);
                startCamera();
            } else {
                Log.d(TAG, "Camera permissions not granted");
                Toast.makeText(getContext(), "Camera permissions not granted by user", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == GALLERY_REQUEST_CODE_PERMISSIONS) {
            if (galleryPermissionsGranted()) {
                setGalleryThumbnail();
            } else {
                Log.d(TAG, "Gallery permissions not granted");
                Toast.makeText(getContext(), "Gallery permissions not granted by user", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean cameraPermissionsGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean galleryPermissionsGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void setCameraView(boolean camera) {
        if (camera) {
            textureView.setVisibility(View.VISIBLE);
            ivBlackBar.setVisibility(View.VISIBLE);
            ivCaptureImage.setVisibility(View.VISIBLE);
            ivThumbnail.setVisibility(View.VISIBLE);
            etDescriptionInput.setVisibility(View.GONE);
            ivPreview.setVisibility(View.GONE);
            btnCreate.setVisibility(View.GONE);
        } else {
            textureView.setVisibility(View.GONE);
            ivBlackBar.setVisibility(View.GONE);
            ivCaptureImage.setVisibility(View.GONE);
            ivThumbnail.setVisibility(View.GONE);
            etDescriptionInput.setVisibility(View.VISIBLE);
            ivPreview.setVisibility(View.VISIBLE);
            btnCreate.setVisibility(View.VISIBLE);
        }
    }
}