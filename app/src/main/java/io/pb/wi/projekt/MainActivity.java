package io.pb.wi.projekt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;

import com.google.android.material.navigation.NavigationView;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.List;

import io.pb.wi.projekt.adapter.CardStackAdapter;

public class MainActivity extends AppCompatActivity implements CardStackListener {

    private DrawerLayout drawerLayout;
    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        cardStackView = findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(this, this);
        adapter = new CardStackAdapter(createSpots());

        setupNavigation();
        setupCardStackView();
        setupButton();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
        Log.d("CardStackView", "onCardDragging: d = " + direction.name() + ", r = " + ratio);
    }

    @Override
    public void onCardSwiped(Direction direction) {
        Log.d("CardStackView", "onCardSwiped: p = " + manager.getTopPosition() + ", d = " + direction);
        if (manager.getTopPosition() == adapter.getItemCount() - 5) {
            paginate();
        }
    }

    @Override
    public void onCardRewound() {
        Log.d("CardStackView", "onCardRewound: " + manager.getTopPosition());
    }

    @Override
    public void onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: " + manager.getTopPosition());
    }

    @Override
    public void onCardAppeared(View view, int position) {
        TextView textView = view.findViewById(R.id.item_name);
        Log.d("CardStackView", "onCardAppeared: (" + position + ") " + textView.getText());
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        TextView textView = view.findViewById(R.id.item_name);
        Log.d("CardStackView", "onCardDisappeared: (" + position + ") " + textView.getText());
    }

    private void setupNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.reload) {
                reload();
            } else if (itemId == R.id.add_spot_to_first) {
                addFirst(1);
            } else if (itemId == R.id.add_spot_to_last) {
                addLast(1);
            } else if (itemId == R.id.remove_spot_from_first) {
                removeFirst(1);
            } else if (itemId == R.id.remove_spot_from_last) {
                removeLast(1);
            } else if (itemId == R.id.replace_first_spot) {
                replace();
            } else if (itemId == R.id.swap_first_for_last) {
                swap();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupCardStackView() {
        initialize();
    }

    private void setupButton() {
        View skip = findViewById(R.id.skip_button);
        skip.setOnClickListener(view -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            manager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });

        View rewind = findViewById(R.id.rewind_button);
        rewind.setOnClickListener(view -> {
            RewindAnimationSetting setting = new RewindAnimationSetting.Builder()
                    .setDirection(Direction.Bottom)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new DecelerateInterpolator())
                    .build();
            manager.setRewindAnimationSetting(setting);
            cardStackView.rewind();
        });

        View like = findViewById(R.id.like_button);
        like.setOnClickListener(view -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            manager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });
    }

    private void initialize() {
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(true);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
        if (cardStackView.getItemAnimator() instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) cardStackView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
    }

    private void paginate() {
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = new ArrayList<>(old);
        newSpots.addAll(createSpots());
        SpotDiffCallback callback = new SpotDiffCallback(old, newSpots);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newSpots);
        result.dispatchUpdatesTo(adapter);
    }

    private void reload() {
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = createSpots();
        SpotDiffCallback callback = new SpotDiffCallback(old, newSpots);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newSpots);
        result.dispatchUpdatesTo(adapter);
    }

    private void addFirst(int size) {
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = new ArrayList<>(old);
        for (int i = 0; i < size; i++) {
            newSpots.add(manager.getTopPosition(), createSpot());
        }
        SpotDiffCallback callback = new SpotDiffCallback(old, newSpots);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newSpots);
        result.dispatchUpdatesTo(adapter);
    }

    private void addLast(int size) {
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = new ArrayList<>(old);
        for (int i = 0; i < size; i++) {
            newSpots.add(createSpot());
        }
        SpotDiffCallback callback = new SpotDiffCallback(old, newSpots);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newSpots);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeFirst(int size) {
        if (adapter.getSpots().isEmpty()) {
            return;
        }
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = new ArrayList<>(old);
        for (int i = 0; i < size; i++) {
            newSpots.remove(manager.getTopPosition());
        }
        SpotDiffCallback callback = new SpotDiffCallback(old, newSpots);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newSpots);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeLast(int size) {
        if (adapter.getSpots().isEmpty()) {
            return;
        }
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = new ArrayList<>(old);
        for (int i = 0; i < size; i++) {
            newSpots.remove(newSpots.size() - 1);
        }
        SpotDiffCallback callback = new SpotDiffCallback(old, newSpots);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newSpots);
        result.dispatchUpdatesTo(adapter);
    }

    private void replace() {
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = new ArrayList<>(old);
        newSpots.remove(manager.getTopPosition());
        newSpots.add(manager.getTopPosition(), createSpot());
        adapter.setSpots(newSpots);
        adapter.notifyItemChanged(manager.getTopPosition());
    }

    private void swap() {
        List<Spot> old = adapter.getSpots();
        List<Spot> newSpots = new ArrayList<>(old);
        Spot first = newSpots.remove(manager.getTopPosition());
        Spot last = newSpots.remove(newSpots.size() - 1);
        newSpots.add(manager.getTopPosition(), last);
        newSpots.add(first);
        SpotDiffCallback callback = new SpotDiffCallback(old, newSpots);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newSpots);
        result.dispatchUpdatesTo(adapter);
    }

    private Spot createSpot() {
        return new Spot("Yasaka Shrine", "Kyoto", "https://source.unsplash.com/Xq1ntWruZQI/600x800");
    }

    private List<Spot> createSpots() {
        List<Spot> spots = new ArrayList<>();
        spots.add(new Spot("Yasaka Shrine", "Kyoto", "https://summoning.ru/images/notes/kyoto/79.jpg"));
        spots.add(new Spot("Fushimi Inari Shrine", "Kyoto", "https://source.unsplash.com/NYyCqdBOKwc/600x800"));
        spots.add(new Spot("Bamboo Forest", "Kyoto", "https://source.unsplash.com/buF62ewDLcQ/600x800"));
        spots.add(new Spot("Brooklyn Bridge", "New York", "https://source.unsplash.com/THozNzxEP3g/600x800"));
        spots.add(new Spot("Empire State Building", "New York", "https://source.unsplash.com/USrZRcRS2Lw/600x800"));
        spots.add(new Spot("The Statue of Liberty", "New York", "https://source.unsplash.com/PeFk7fzxTdk/600x800"));
        spots.add(new Spot("Louvre Museum", "Paris", "https://source.unsplash.com/LrMWHKqilUw/600x800"));
        spots.add(new Spot("Eiffel Tower", "Paris", "https://source.unsplash.com/HN-5Z6AmxrM/600x800"));
        spots.add(new Spot("Big Ben", "London", "https://source.unsplash.com/CdVAUADdqEc/600x800"));
        spots.add(new Spot("Great Wall of China", "China", "https://source.unsplash.com/AWh9C-QjhE4/600x800"));
        return spots;
    }
}
