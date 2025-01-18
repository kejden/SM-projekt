package io.pb.wi.projekt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.pb.wi.projekt.R;
import io.pb.wi.projekt.User;

public class UserStackAdapter extends RecyclerView.Adapter<UserStackAdapter.ViewHolder> {

    private List<User> users;

    public UserStackAdapter(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.name.setText(user.getName() + ", " + user.getAge());
        holder.location.setText(user.getLocation());
        Glide.with(holder.image.getContext())
                .load(user.getProfileUrl())
                .into(holder.image);
        holder.itemView.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "You clicked on " + user.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
    }

    public List<User> getUsers() {
        return users;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView location;
        ImageView image;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            location = view.findViewById(R.id.item_city);
            image = view.findViewById(R.id.item_image);
        }
    }
}