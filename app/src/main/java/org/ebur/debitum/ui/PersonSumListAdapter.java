package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;

public class PersonSumListAdapter extends ListAdapter<PersonWithTransactions, PersonSumListViewHolder> {

    public PersonSumListAdapter(@NonNull DiffUtil.ItemCallback<PersonWithTransactions> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public PersonSumListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PersonSumListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(PersonSumListViewHolder holder, int position) {
        PersonWithTransactions current = getItem(position);
        holder.bind(current);
    }

    static class PersonSumDiff extends DiffUtil.ItemCallback<PersonWithTransactions> {

        @Override
        public boolean areItemsTheSame(@NonNull PersonWithTransactions oldItem, @NonNull PersonWithTransactions newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull PersonWithTransactions oldItem, @NonNull PersonWithTransactions newItem) {
            return oldItem.equals(newItem);
        }
    }

}
