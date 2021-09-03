package org.ebur.debitum.ui;

import android.net.Uri;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class EditTransactionImageAdapter
        extends ListAdapter<Uri, EditTransactionImageViewHolder> {

    public EditTransactionImageAdapter(@NonNull DiffUtil.ItemCallback<Uri> diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public EditTransactionImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return EditTransactionImageViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull EditTransactionImageViewHolder holder, int position) {
        Uri current = getItem(position);
        holder.bind(current);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    static class Diff extends DiffUtil.ItemCallback<Uri> {

        @Override
        public boolean areItemsTheSame(@NonNull Uri oldItem, @NonNull Uri newItem) {
            return oldItem.compareTo(newItem) == 0;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Uri oldItem, @NonNull Uri newItem) {
            return oldItem.compareTo(newItem) == 0;
        }
    }
}
