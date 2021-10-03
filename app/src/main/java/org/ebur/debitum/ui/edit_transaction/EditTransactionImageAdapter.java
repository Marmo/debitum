package org.ebur.debitum.ui.edit_transaction;

import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

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
        //setHasStableIds(true);
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
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull File oldItem, @NonNull File newItem) {
            return compareFiles(oldItem, newItem);
        }

        // from https://stackoverflow.com/questions/38527245/how-to-compare-two-java-io-file-programmatically
        public boolean compareFiles(@NonNull File file1, @NonNull File file2) {
            if(file1.length() != file2.length()) {
                return false;
            }
            byte[] buffer1 = new byte[1024];
            byte[] buffer2 = new byte[1024];
            try {
                FileInputStream fileInputStream1 = new FileInputStream(file1);
                FileInputStream fileInputStream2 = new FileInputStream(file2);
                while (fileInputStream1.read(buffer1) != -1) {
                    if (fileInputStream2.read(buffer2) != -1 && !Arrays.equals(buffer1, buffer2))
                        return false;
                }
                return true;
            } catch (Exception ignore) {
                return false;
            }
        }
    }
}
