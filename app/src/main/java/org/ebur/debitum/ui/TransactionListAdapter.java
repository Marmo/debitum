package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.database.TransactionWithPerson;

public class TransactionListAdapter extends ListAdapter<TransactionWithPerson, RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private SelectionTracker<Long> selectionTracker = null;

    public TransactionListAdapter(@NonNull DiffUtil.ItemCallback<TransactionWithPerson> diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return HeaderViewHolder.create(parent);
            case TYPE_ITEM:
                return TransactionListViewHolder.create(parent);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.bind(100);
        }
        else if(holder instanceof TransactionListViewHolder) {
            TransactionListViewHolder itemHolder = (TransactionListViewHolder) holder;
            TransactionWithPerson current = getItem(position);
            itemHolder.bind(current,
                    selectionTracker.isSelected(getItemId(position))
            );
        }
    }

    @Override
    public long getItemId(int position) { return getItem(position).transaction.idTransaction; }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;
        else
            return TYPE_ITEM;
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) { this.selectionTracker = selectionTracker; }

    static class TransactionDiff extends DiffUtil.ItemCallback<TransactionWithPerson> {

        @Override
        public boolean areItemsTheSame(@NonNull TransactionWithPerson oldItem, @NonNull TransactionWithPerson newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionWithPerson oldItem, @NonNull TransactionWithPerson newItem) {
            return oldItem.equals(newItem);
        }
    }
}
