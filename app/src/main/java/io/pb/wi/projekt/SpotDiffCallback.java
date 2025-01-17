package io.pb.wi.projekt;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class SpotDiffCallback extends DiffUtil.Callback {

    private final List<Spot> oldList;
    private final List<Spot> newList;

    public SpotDiffCallback(List<Spot> oldList, List<Spot> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldPosition, int newPosition) {
        return oldList.get(oldPosition).getId() == newList.get(newPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldPosition, int newPosition) {
        return oldList.get(oldPosition).equals(newList.get(newPosition));
    }
}
