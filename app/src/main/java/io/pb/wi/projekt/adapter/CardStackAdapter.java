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
import io.pb.wi.projekt.Spot;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {

    private List<Spot> spots;

    public CardStackAdapter(List<Spot> spots) {
        this.spots = spots != null ? spots : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spot spot = spots.get(position);
        holder.name.setText(spot.getId() + ". " + spot.getName());
        holder.city.setText(spot.getCity());
        Glide.with(holder.image.getContext())
                .load(spot.getUrl())
                .into(holder.image);
        holder.itemView.setOnClickListener(v ->
                Toast.makeText(v.getContext(), spot.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    public void setSpots(List<Spot> spots) {
        this.spots = spots != null ? spots : new ArrayList<>();
    }

    public List<Spot> getSpots() {
        return spots;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView city;
        ImageView image;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            city = view.findViewById(R.id.item_city);
            image = view.findViewById(R.id.item_image);
        }
    }
}

