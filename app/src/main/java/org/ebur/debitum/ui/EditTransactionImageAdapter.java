package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import java.io.File;

public class EditTransactionImageAdapter
        extends ListAdapter<File, EditTransactionImageViewHolder> {

    private final ActivityResultLauncher<String> addImageLauncher;
    private final EditTransactionImageViewHolder.DeleteImageCallback deleteCallback;

    public EditTransactionImageAdapter(@NonNull DiffUtil.ItemCallback<File> diffCallback,
                                       @NonNull ActivityResultLauncher<String> addImageLauncher,
                                       @NonNull EditTransactionImageViewHolder.DeleteImageCallback deleteCallback) {
        super(diffCallback);
        this.addImageLauncher = addImageLauncher;
        this.deleteCallback = deleteCallback;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public EditTransactionImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return EditTransactionImageViewHolder.create(parent, addImageLauncher, deleteCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull EditTransactionImageViewHolder holder, int position) {
        File current = getItem(position);
        holder.bind(current);
    }

    /*@Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }*/

    static class Diff extends DiffUtil.ItemCallback<File> {

        @Override
        public boolean areItemsTheSame(@NonNull File oldItem, @NonNull File newItem) {
            return oldItem.compareTo(newItem) == 0;
        }

        @Override
        public boolean areContentsTheSame(@NonNull File oldItem, @NonNull File newItem) {
            return oldItem.compareTo(newItem) == 0;
        }
    }
}
