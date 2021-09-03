package org.ebur.debitum.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

class EditTransactionImageViewHolder extends RecyclerView.ViewHolder {
    private final TextView imgNameView;
    private final ImageView imgView;
    private final ImageView deleteBtnView;
    private final String placeholderTitle;
    private final Drawable placeholderDrawable;

    private EditTransactionImageViewHolder(View itemView) {
        super(itemView);
        imgNameView = itemView.findViewById(R.id.image_title);
        imgView = itemView.findViewById(R.id.image);
        deleteBtnView = itemView.findViewById(R.id.button_delete);

        Context context = itemView.getContext();
        placeholderTitle = context.getString(R.string.edit_transaction_placeholder_title);
        placeholderDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_add_photo_64);
    }

    public void bind(@Nullable Uri imageUri) {

        if (imageUri != null) {
            imgView.setImageBitmap(BitmapFactory.decodeFile(imageUri.getPath()));
            imgNameView.setText(imageUri.getLastPathSegment());
            deleteBtnView.setVisibility(View.VISIBLE);
            deleteBtnView.setOnClickListener(view -> {
                deleteImage(imageUri);
            });
            imgView.setOnClickListener(view -> {
                showImage(imageUri);
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

    @Nullable
    private File getImageFile(@Nullable Uri uri) {
        if (uri != null && uri.getScheme().equals("file")) {
            try {
                URI fileUri = new URI(uri.toString());
                return new File(fileUri);
            } catch (URISyntaxException ignored) {
                return null;
            }
        } else {
            return null;
        }
    }

    private void showImage(Uri uri) {
        if (uri != null) {
            Context context = imgView.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // TODO make Toast telling that no app to open image could be found
            }
        }
    }

    private void addImage() {
        // TODO, possibly pass this OnClickListener to bind()
        // start file picker
        // add image uri to viewModel
    }

    private void deleteImage(@Nullable Uri uri) {
        @Nullable File file = getImageFile(uri);
        if (file != null) {
            // TODO ask for confirmation
            boolean deleted = file.delete();
            // TODO update RecyclerView/Adapter's list
        }
    }

    static EditTransactionImageViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_list, parent, false);
        return new EditTransactionImageViewHolder(view);
    }

    // anonymous implementation of androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
    //     https://proandroiddev.com/a-guide-to-recyclerview-selection-3ed9f2381504?gi=ee4affe1b9d3
    //     https://developer.android.com/reference/androidx/recyclerview/selection/package-summary
    ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<Long>() {
            @Override
            public int getPosition() { return getAdapterPosition(); }

            @Override
            public Long getSelectionKey() { return getItemId(); }
        };
    }
}

