package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.ebur.debitum.database.TransactionWithPerson;

public class TransactionListAdapter extends ListAdapter<TransactionWithPerson, TransactionListViewHolder> {

    public TransactionListAdapter(@NonNull DiffUtil.ItemCallback<TransactionWithPerson> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public TransactionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TransactionListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(TransactionListViewHolder holder, int position) {
        TransactionWithPerson current = getItem(position);
        holder.bind(current.person.name, current.transaction.description, current.transaction.getAmount(), current.transaction.timestamp);
    }

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
