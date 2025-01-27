package io.pb.wi.projekt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.pb.wi.projekt.ImageAdapter;
import io.pb.wi.projekt.R;
import io.pb.wi.projekt.User;


public class UserStackAdapter extends RecyclerView.Adapter<UserStackAdapter.ViewHolder> {
    private List<User> users;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 pager;
        LinearLayout dotsContainer;
        TextView name;
        TextView location;
        FrameLayout leftTouchArea;
        FrameLayout rightTouchArea;

        public ViewHolder(View view) {
            super(view);
            pager = view.findViewById(R.id.pager);
            dotsContainer = view.findViewById(R.id.dotsContainer);
            name = view.findViewById(R.id.item_name);
            location = view.findViewById(R.id.item_city);
            leftTouchArea = view.findViewById(R.id.left_touch_area);
            rightTouchArea = view.findViewById(R.id.right_touch_area);
        }
    }

    public UserStackAdapter(List<User> users) {
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);

        holder.pager.setAdapter(new ImageAdapter(user.getProfileUrls()));
        setupDots(holder.dotsContainer, user.getProfileUrls().size(), holder.pager);

        holder.leftTouchArea.setOnClickListener(v -> {
            int currentItem = holder.pager.getCurrentItem();
            if (currentItem > 0) {
                holder.pager.setCurrentItem(currentItem - 1, true);
            }
        });

        holder.rightTouchArea.setOnClickListener(v -> {
            int currentItem = holder.pager.getCurrentItem();
            if (currentItem < holder.pager.getAdapter().getItemCount() - 1) {
                holder.pager.setCurrentItem(currentItem + 1, true);
            }
        });

        holder.name.setText(user.getName());
        holder.location.setText(user.getLocation());
    }

    private void setupDots(LinearLayout container, int count, ViewPager2 pager) {
        ImageView[] dots = new ImageView[count];
        container.removeAllViews();

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(container.getContext());
            dots[i].setImageResource(R.drawable.dot_inactive);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            container.addView(dots[i], params);
        }

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < count; i++) {
                    dots[i].setImageResource(i == position ?
                            R.drawable.dot_active : R.drawable.dot_inactive);
                }
                super.onPageSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
}