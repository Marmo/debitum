package org.ebur.debitum.ui.list;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class ListItemDetailsLookup extends ItemDetailsLookup<Long> {
    private final RecyclerView recyclerView;

    ListItemDetailsLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof TransactionListViewHolder) {
                return ((TransactionListViewHolder) holder).getItemDetails();
            }
            else if (holder instanceof PersonSumListViewHolder) {
                return ((PersonSumListViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}
