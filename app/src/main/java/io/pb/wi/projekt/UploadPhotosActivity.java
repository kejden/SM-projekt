package io.pb.wi.projekt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UploadPhotosActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView[] imageViews = new ImageView[4];
    private List<Uri> imageUris = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference userDb;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photos);

        mAuth = FirebaseAuth.getInstance();
        userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        storageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        imageViews[0] = findViewById(R.id.image_view_1);
        imageViews[1] = findViewById(R.id.image_view_2);
        imageViews[2] = findViewById(R.id.image_view_3);
        imageViews[3] = findViewById(R.id.image_view_4);

        findViewById(R.id.select_button).setOnClickListener(v -> selectImages());
        findViewById(R.id.done_button).setOnClickListener(v -> uploadImages());
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUris.clear();

            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), 4);
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }

            updateImageViews();
        }
    }

    private void updateImageViews() {
        for (int i = 0; i < imageViews.length; i++) {
            if (i < imageUris.size()) {
                imageViews[i].setImageURI(imageUris.get(i));
            } else {
                imageViews[i].setImageResource(R.drawable.placeholder);
            }
        }
    }

    private void uploadImages() {
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Wybierz przynajmniej jedno zdjęcie", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Przesyłanie zdjęć...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<com.google.android.gms.tasks.Task<Uri>> uploadTasks = new ArrayList<>();

        for (int i = 0; i < imageUris.size(); i++) {
            int index = i;
            StorageReference fileRef = storageRef.child(mAuth.getCurrentUser().getUid() + "_" + UUID.randomUUID().toString() + ".jpg");
            com.google.android.gms.tasks.Task<Uri> uploadTask = fileRef.putFile(imageUris.get(index))
                    .continueWithTask(task -> fileRef.getDownloadUrl());
            uploadTasks.add(uploadTask);
        }

        Tasks.whenAllSuccess(uploadTasks).addOnCompleteListener(task -> {
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                List<Uri> downloadUris = new ArrayList<>();
                for (Object obj : task.getResult()) {
                    if (obj instanceof Uri) {
                        downloadUris.add((Uri) obj);
                    }
                }

                for (int i = 0; i < 4; i++) {
                    userDb.child("profileImageUrl" + i)
                            .setValue(i < downloadUris.size() ? downloadUris.get(i).toString() : "default");
                }

                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Błąd przesyłania: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}