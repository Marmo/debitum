package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.database.PersonWithTransactions;

public class PersonSumListAdapter extends ListAdapter<PersonWithTransactions, RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private SelectionTracker<Long> selectionTracker = null;

    public PersonSumListAdapter(@NonNull DiffUtil.ItemCallback<PersonWithTransactions> diffCallback) {
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
                return PersonSumListViewHolder.create(parent);
            default:
                throw new ClassCastException("Unknown viewType");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            PersonWithTransactions header = getItem(position);
            headerHolder.bind(header.getSum(), true);
        }
        else if(holder instanceof PersonSumListViewHolder) {
            PersonSumListViewHolder itemHolder = (PersonSumListViewHolder) holder;
            PersonWithTransactions current = getItem(position);
            itemHolder.bind(current,
                    selectionTracker.isSelected(getItemId(position))
            );
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).person.idPerson;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;
        else
            return TYPE_ITEM;
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) { this.selectionTracker = selectionTracker; }

    static class PersonSumDiff extends DiffUtil.ItemCallback<PersonWithTransactions> {

        @Override
        public boolean areItemsTheSame(@NonNull PersonWithTransactions oldItem, @NonNull PersonWithTransactions newItem) {
            return oldItem.person.idPerson == newItem.person.idPerson;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PersonWithTransactions oldItem, @NonNull PersonWithTransactions newItem) {
            return oldItem.equals(newItem);
        }
    }

}
