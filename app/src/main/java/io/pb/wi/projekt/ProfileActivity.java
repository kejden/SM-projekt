package io.pb.wi.projekt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    static final int PICK_IMAGE_REQUEST = 1;
    private ImageView[] profileImageViews = new ImageView[4];
    private List<Uri> imageUris = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference userDb;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        storageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        profileImageViews[0] = findViewById(R.id.profile_image_1);
        profileImageViews[1] = findViewById(R.id.profile_image_2);
        profileImageViews[2] = findViewById(R.id.profile_image_3);
        profileImageViews[3] = findViewById(R.id.profile_image_4);

        Button editProfileButton = findViewById(R.id.edit_profile_button);
        editProfileButton.setOnClickListener(v -> editProfile());

        loadProfile();
    }

    private void loadProfile() {
        userDb.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    int age = dataSnapshot.child("age").getValue(Integer.class);
                    String location = dataSnapshot.child("location").getValue(String.class);

                    EditText nameEditText = findViewById(R.id.name);
                    EditText ageEditText = findViewById(R.id.age);
                    EditText locationEditText = findViewById(R.id.location);

                    nameEditText.setText(name);
                    ageEditText.setText(String.valueOf(age));
                    locationEditText.setText(location);

                    for (int i = 0; i < 4; i++) {
                        String imageUrl = dataSnapshot.child("profileImageUrl" + i).getValue(String.class);
                        if (imageUrl != null && !imageUrl.equals("default")) {
                            Picasso.get().load(imageUrl).into(profileImageViews[i]);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
            }
        });
    }

    private void editProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    public void addImage(View view) {
        startActivity(new Intent(this, GalleryActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageUris.add(imageUri);
            updateProfileImages();
        }
    }

    private void updateProfileImages() {
        for (int i = 0; i < imageUris.size(); i++) {
            profileImageViews[i].setImageURI(imageUris.get(i));
            uploadImageToFirebase(i, imageUris.get(i));
        }
    }

    private void uploadImageToFirebase(int index, Uri imageUri) {
        StorageReference fileRef = storageRef.child(mAuth.getCurrentUser().getUid() + "_" + index + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();
            userDb.child("profileImageUrl" + index).setValue(downloadUrl);
        }));
    }
}