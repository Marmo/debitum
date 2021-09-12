package org.ebur.debitum.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;

import java.io.File;

class EditTransactionImageViewHolder extends RecyclerView.ViewHolder {
    private final TextView imgNameView;
    private final ImageView imgView;
    private final ImageView deleteBtnView;
    private final String placeholderTitle;
    private final Drawable placeholderDrawable;
    private final ActivityResultLauncher<String> addImageLauncher;
    private final DeleteImageCallback deleteCallback;

    private EditTransactionImageViewHolder(View itemView, ActivityResultLauncher<String> addImageLauncher, DeleteImageCallback deleteCallback) {
        super(itemView);
        imgNameView = itemView.findViewById(R.id.image_title);
        imgView = itemView.findViewById(R.id.image);
        deleteBtnView = itemView.findViewById(R.id.button_delete);

        this.addImageLauncher = addImageLauncher;
        this.deleteCallback = deleteCallback;

        Context context = itemView.getContext();
        placeholderTitle = context.getString(R.string.edit_transaction_image_add_title);
        placeholderDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_add_photo_64);
    }

    public void bind(@Nullable File imageFile) {

        if (imageFile != null) {
            imgView.setImageURI(Uri.fromFile(imageFile));
            imgNameView.setText(imageFile.getName());
            deleteBtnView.setVisibility(View.VISIBLE);
            deleteBtnView.setOnClickListener(view -> {
                deleteImage(imageFile);
            });
            imgView.setOnClickListener(view -> {
                showImage(imageFile);
            });
        } else {
            imgView.setImageDrawable(placeholderDrawable);
            imgNameView.setText(placeholderTitle);
            imgView.setOnClickListener(view -> {
                addImage();
            });
            deleteBtnView.setVisibility(View.GONE);
        }
    }

    private void showImage(@Nullable File file) {
        if (file != null) {
            Context context = imgView.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // using the file:// uri directly would cause a android.os.FileUriExposedException
            Uri contentUri = FileProvider.getUriForFile(context, "org.ebur.debitum.fileprovider", file);
            intent.setData(contentUri);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(itemView.getContext(),
                        itemView.getResources().getString(R.string.edit_transaction_image_error_app_not_found),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void addImage() {
        // start file picker
        String mimetype = "image/*";
        addImageLauncher.launch(mimetype);
    }

    private void deleteImage(@NonNull File file) {
         deleteCallback.onDelete(file);
    }

    static EditTransactionImageViewHolder create(ViewGroup parent,
                                                 ActivityResultLauncher<String> addImageLauncher,
                                                 DeleteImageCallback deleteCallback) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_list, parent, false);
        return new EditTransactionImageViewHolder(view, addImageLauncher, deleteCallback);
    }

    // this must be passed by the fragment using the adapter/viewHolder to handle image deletion
    public interface DeleteImageCallback {
        void onDelete(@NonNull File imagefile);
    }
}

