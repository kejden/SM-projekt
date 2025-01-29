package io.pb.wi.projekt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "GalleryActivity";
    private static final int MAX_IMAGES = 4;

    private GridLayout imagesGrid;
    private List<Uri> imageUris = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private StorageReference storageRef;
    private DatabaseReference userDb;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initializeFirebase();
        setupUI();
        loadExistingImages();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        userDb = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
    }

    private void setupUI() {
        imagesGrid = findViewById(R.id.images_grid);
        MaterialButton addPhotosButton = findViewById(R.id.add_photos_button);
        addPhotosButton.setOnClickListener(v -> pickImages());
    }

    private void loadExistingImages() {
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageUrls.clear();
                for (int i = 0; i < MAX_IMAGES; i++) {
                    String url = snapshot.child("profileImageUrl" + i).getValue(String.class);
                    if (url != null && !url.equals("default")) {
                        imageUrls.add(url);
                    }
                }
                populateGrid();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void populateGrid() {
        imagesGrid.removeAllViews();
        imagesGrid.setRowCount(2);
        imagesGrid.requestLayout();


        // Handle empty state
        if (imageUrls.isEmpty() && imageUris.isEmpty()) {
            View emptyView = getLayoutInflater().inflate(R.layout.empty_gallery_state, imagesGrid, false);
            imagesGrid.addView(emptyView);
            return;
        }

        // Display existing images from URLs
        for (String url : imageUrls) {
            addImageToGrid(url, true);
        }

        // Display newly selected images from URIs
        for (Uri uri : imageUris) {
            addImageToGrid(uri.toString(), false);
        }
    }

    private void addImageToGrid(String imageUrl, boolean isExisting) {
        View imageViewLayout = getLayoutInflater().inflate(R.layout.item_image, imagesGrid, false);
        ImageView imageView = imageViewLayout.findViewById(R.id.image_view);
        ImageButton deleteBtn = imageViewLayout.findViewById(R.id.delete_btn);

        try {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }

        setupDeleteButton(deleteBtn, imageViewLayout, imageUrl, isExisting);
        imagesGrid.addView(imageViewLayout);
    }

    private void setupDeleteButton(ImageButton deleteBtn, View imageViewLayout, String imageUrl, boolean isExisting) {
        deleteBtn.setOnClickListener(v -> {
            if (isExisting) {
                animateAndDelete(imageViewLayout, imageUrl);
            } else {
                removeLocalImage(imageUrl);
            }
        });
    }

    private void animateAndDelete(View view, String imageUrl) {
        ViewGroup parent = (ViewGroup) view.getParent();
        parent.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            deleteImageFromStorage(imageUrl);
            parent.removeView(view);
        }, 300);
    }

    private void removeLocalImage(String imageUrl) {
        imageUris.removeIf(uri -> uri.toString().equals(imageUrl));
        populateGrid();
    }

    private void deleteImageFromStorage(String imageUrl) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        photoRef.delete().addOnSuccessListener(aVoid -> updateDatabaseAfterDeletion(imageUrl))
                .addOnFailureListener(e -> Log.e(TAG, "Delete failed: " + e.getMessage()));
    }

    private void updateDatabaseAfterDeletion(String deletedUrl) {
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> remainingUrls = new ArrayList<>();

                // Collect remaining URLs
                for (int i = 0; i < MAX_IMAGES; i++) {
                    String url = snapshot.child("profileImageUrl" + i).getValue(String.class);
                    if (url != null && !url.equals(deletedUrl) && !url.equals("default")) {
                        remainingUrls.add(url);
                    }
                }

                // Reset all image slots
                for (int i = 0; i < MAX_IMAGES; i++) {
                    String value = (i < remainingUrls.size()) ? remainingUrls.get(i) : "default";
                    userDb.child("profileImageUrl" + i).setValue(value);
                }

                new Handler().postDelayed(() -> loadExistingImages(), 500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            handleSelectedImages(data);
        }
    }

    private void handleSelectedImages(Intent data) {
        imageUris.clear();

        try {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && imageUris.size() < MAX_IMAGES; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }

            uploadImages();
        } catch (Exception e) {
            Log.e(TAG, "Error handling images: " + e.getMessage());
            Toast.makeText(this, "Błąd przy wyborze zdjęć", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImages() {
        for (int i = 0; i < imageUris.size(); i++) {
            int uploadIndex = imageUrls.size() + i;
            if (uploadIndex >= MAX_IMAGES) break;

            Uri uri = imageUris.get(i);
            StorageReference fileRef = storageRef.child(mAuth.getCurrentUser().getUid() + "_" + UUID.randomUUID().toString() + ".jpg");

            fileRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot ->
                            fileRef.getDownloadUrl().addOnSuccessListener(downloadUrl ->
                                    userDb.child("profileImageUrl" + uploadIndex).setValue(downloadUrl.toString())
                            )
                    )
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Upload failed: " + e.getMessage())
                    );
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Picasso.get().cancelTag(TAG);
    }
}