package org.ebur.debitum.ui.edit_transaction;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;

import java.io.File;

class EditTransactionImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final ImageView imgView;
    private final View checkmarkView;
    private final Drawable placeholderDrawable;
    private final ActivityResultLauncher<String> addImageLauncher;
    private final DeleteImageCallback deleteCallback;
    @Nullable private File imageFile;

    private ActionMode actionMode;

    private EditTransactionImageViewHolder(View itemView, ActivityResultLauncher<String> addImageLauncher, DeleteImageCallback deleteCallback) {
        super(itemView);
        imgView = itemView.findViewById(R.id.image);
        checkmarkView = itemView.findViewById(R.id.image_selected_checkmark);

        this.addImageLauncher = addImageLauncher;
        this.deleteCallback = deleteCallback;

        Context context = itemView.getContext();
        placeholderDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_add_photo_64);
    }

    public void bind(@Nullable File imageFile) {
        this.imageFile = imageFile;
        checkmarkView.setVisibility(View.INVISIBLE);
        if (imageFile != null) {
            imgView.setImageURI(Uri.fromFile(imageFile));
            itemView.setOnClickListener(view -> {
                showImage();
            });
            itemView.setOnLongClickListener(view -> {
                if(itemView.isSelected()) {
                    actionMode.finish();
                    return true;
                } else {
                    if (actionMode != null) {
                        return false;
                    }
                    actionMode = itemView.startActionMode(actionModeCallback);
                    return true;
                }
            });
        } else {
            imgView.setImageDrawable(placeholderDrawable);
            itemView.setOnClickListener(view -> {
                addImage();
            });
            itemView.setOnLongClickListener(view -> {
                // show add image tooltip as toast
                String msg = itemView.getContext().getString(R.string.edit_transaction_image_add_title);
                Toast.makeText(itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    private void showImage() {
        if (imageFile != null) {
            Context context = imgView.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // using the file:// uri directly would cause a android.os.FileUriExposedException
            Uri contentUri = FileProvider.getUriForFile(context, "org.ebur.debitum.fileprovider", imageFile);
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

    private void deleteImage() {
         if (imageFile != null) deleteCallback.onDelete(imageFile);
    }

    static EditTransactionImageViewHolder create(ViewGroup parent,
                                                 ActivityResultLauncher<String> addImageLauncher,
                                                 DeleteImageCallback deleteCallback) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_list, parent, false);
        return new EditTransactionImageViewHolder(view, addImageLauncher, deleteCallback);
    }

    // without having an onClickListener the view will not get the pressed state when clicked
    // and thus won't reflect the backgroundTint change from list_item_bg_selector.xml
    @Override
    public void onClick(View v) {
    }



    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_edit_transaction_image, menu);
            itemView.setSelected(true);
            checkmarkView.setVisibility(View.VISIBLE);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(R.string.edit_transaction_actionmode_title);
            return true; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.miDeleteImage) {
                deleteImage();
                mode.finish(); // Action picked, so close the CAB
                return true;
            } else {
                return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            itemView.setSelected(false);
            checkmarkView.setVisibility(View.INVISIBLE);
        }
    };

    // this must be passed by the fragment using the adapter/viewHolder to handle image deletion
    public interface DeleteImageCallback {
        void onDelete(@NonNull File imagefile);
    }

}

