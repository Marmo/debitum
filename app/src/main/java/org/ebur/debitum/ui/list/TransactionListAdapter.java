package org.ebur.debitum.ui.list;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.ebur.debitum.database.TransactionWithPerson;

public class TransactionListAdapter
        extends ListAdapter<TransactionWithPerson, TransactionListViewHolder>
        implements AbstractBaseListFragment.Adapter {

    private SelectionTracker<Long> selectionTracker = null;

    public TransactionListAdapter(@NonNull DiffUtil.ItemCallback<TransactionWithPerson> diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public TransactionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TransactionListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionListViewHolder holder, int position) {
        TransactionWithPerson current = getItem(position);
        holder.bind(current, selectionTracker.isSelected(getItemId(position)));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).transaction.idTransaction;
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) { this.selectionTracker = selectionTracker; }

    static class TransactionDiff extends DiffUtil.ItemCallback<TransactionWithPerson> {

        @Override
        public boolean areItemsTheSame(@NonNull TransactionWithPerson oldItem, @NonNull TransactionWithPerson newItem) {
            return oldItem.transaction.idTransaction == newItem.transaction.idTransaction;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionWithPerson oldItem, @NonNull TransactionWithPerson newItem) {
            return oldItem.equals(newItem);
        }
    }
}
