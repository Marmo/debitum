package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;

public class PersonSumListAdapter extends ListAdapter<PersonWithTransactions, PersonSumListViewHolder> {

    private SelectionTracker<Long> selectionTracker = null;

    public PersonSumListAdapter(@NonNull DiffUtil.ItemCallback<PersonWithTransactions> diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public PersonSumListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PersonSumListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(PersonSumListViewHolder holder, int position) {
        PersonWithTransactions current = getItem(position);
        holder.bind(current,
                selectionTracker.isSelected(getItemId(position))
        );
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).person.idPerson;
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
