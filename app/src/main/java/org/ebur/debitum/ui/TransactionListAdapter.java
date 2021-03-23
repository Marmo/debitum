package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.ebur.debitum.database.Transaction;

public class TransactionListAdapter extends ListAdapter<Transaction, TransactionListViewHolder> {

    public TransactionListAdapter(@NonNull DiffUtil.ItemCallback<Transaction> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public TransactionListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TransactionListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(TransactionListViewHolder holder, int position) {
        Transaction current = getItem(position);
        holder.bind(current.name, current.description, current.getAmount(), current.timestamp);
    }

    static class TransactionDiff extends DiffUtil.ItemCallback<Transaction> {

        @Override
        public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
            return oldItem.equals(newItem);
        }
    }

}
