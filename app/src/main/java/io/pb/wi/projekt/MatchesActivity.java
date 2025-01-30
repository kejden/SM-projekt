package io.pb.wi.projekt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.pb.wi.projekt.adapter.MatchesAdapter;
import java.util.ArrayList;
import java.util.List;

public class MatchesActivity extends AppCompatActivity {

    private RecyclerView matchesRecyclerView;
    private MatchesAdapter matchesAdapter;
    private List<User> matchesList = new ArrayList<>();
    private DatabaseReference usersDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        matchesRecyclerView = findViewById(R.id.matches_recycler_view);
        matchesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        matchesAdapter = new MatchesAdapter(matchesList);
        matchesRecyclerView.setAdapter(matchesAdapter);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        loadMatches(currentUserId);
    }

    private void loadMatches(String currentUserId) {
        usersDb.child(currentUserId).child("connections").child("matches").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot match : dataSnapshot.getChildren()) {
                        String matchId = match.getKey();
                        usersDb.child(matchId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String user_id = dataSnapshot.getKey();
                                    String name = dataSnapshot.child("name").getValue(String.class);
                                    int age = dataSnapshot.child("age").getValue(Integer.class);
                                    String location = dataSnapshot.child("location").getValue(String.class);
                                    List<String> profileUrls = new ArrayList<>();
                                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                        if (childSnapshot.getKey().startsWith("profileImageUrl")) {
                                            String url = childSnapshot.getValue(String.class);
                                            if (url != null && !url.equals("default")) {
                                                profileUrls.add(url);
                                            }
                                        }
                                    }
                                    User user = new User(name, age, location, profileUrls, user_id);
                                    matchesList.add(user);
                                    matchesAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}