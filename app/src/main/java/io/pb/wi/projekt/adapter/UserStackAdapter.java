package io.pb.wi.projekt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.pb.wi.projekt.R;
import io.pb.wi.projekt.User;

public class UserStackAdapter extends RecyclerView.Adapter<UserStackAdapter.ViewHolder> {

    private List<User> users;

    public UserStackAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.name.setText(user.getName());
        holder.location.setText(user.getLocation());
        Picasso.get().load(user.getProfileUrl()).into(holder.image);

        holder.image.setOnClickListener(v -> {
            // Przejdź do następnego zdjęcia lub wróć do poprzedniego
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                // Przykład: Przejdź do następnego zdjęcia
                int nextPosition = currentPosition + 1;
                if (nextPosition < users.size()) {
                    User nextUser = users.get(nextPosition);
                    Picasso.get().load(nextUser.getProfileUrl()).into(holder.image);
                    holder.name.setText(nextUser.getName());
                    holder.location.setText(nextUser.getLocation());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addUser(User user) {
        users.add(user);
        notifyDataSetChanged();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name, location;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            name = itemView.findViewById(R.id.item_name);
            location = itemView.findViewById(R.id.item_city);
        }
    }
}