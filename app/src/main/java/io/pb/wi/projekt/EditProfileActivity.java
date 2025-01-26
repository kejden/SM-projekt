package io.pb.wi.projekt;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText, locationEditText;
    private DatabaseReference userDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEditText = findViewById(R.id.name);
        ageEditText = findViewById(R.id.age);
        locationEditText = findViewById(R.id.location);

        userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString();
        String age = ageEditText.getText().toString();
        String location = locationEditText.getText().toString();

        userDb.child("name").setValue(name);
        userDb.child("age").setValue(age);
        userDb.child("location").setValue(location);

        finish();
    }
}