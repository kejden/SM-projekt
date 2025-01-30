package io.pb.wi.projekt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import io.pb.wi.projekt.R;
import io.pb.wi.projekt.User;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchesViewHolder> {

    private List<User> matchesList;

    public MatchesAdapter(List<User> matchesList) {
        this.matchesList = matchesList;
    }

    @NonNull
    @Override
    public MatchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolder holder, int position) {
        User user = matchesList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.ageTextView.setText(String.valueOf(user.getAge()));
        holder.locationTextView.setText(user.getLocation());

        if (user.getProfileUrls() != null && !user.getProfileUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileUrls().get(0))
                    .into(holder.profileImageView);
        }
    }

    @Override
    public int getItemCount() {
        return matchesList.size();
    }

    static class MatchesViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView, ageTextView, locationTextView;

        public MatchesViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image);
            nameTextView = itemView.findViewById(R.id.name);
            ageTextView = itemView.findViewById(R.id.age);
            locationTextView = itemView.findViewById(R.id.location);
        }
    }
}