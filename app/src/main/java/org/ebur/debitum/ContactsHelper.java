package org.ebur.debitum;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Objects;

public class ContactsHelper {

    private final Context context;

    private static class Contact {
        @NonNull String name;
        @Nullable Bitmap photo;

        Contact(@NonNull String name, @Nullable Bitmap photo) {
            this.name = name;
            this.photo = photo;
        }
    }

    private static HashMap<Uri, Contact> contactCache;

    public ContactsHelper(Context context) {
        this.context = context;
    }

    private void cacheContactInfo(@NonNull Uri uri) {
        contactCache.put(uri, new Contact(
                Objects.requireNonNull(getContactName(uri)),
                getContactImage(uri)));
    }

    @Nullable
    public Bitmap getContactImage(@Nullable Uri uri) {
        if (uri == null) return null;

        Uri photoUri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        String[] photoProjection = new String[] {ContactsContract.Contacts.Photo.PHOTO};
        Bitmap photo;
        Cursor cursor = context.getContentResolver()
                .query(photoUri, photoProjection, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    photo = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                    int size = context.getResources().getInteger(R.integer.avatar_size);
                    return Bitmap.createScaledBitmap(photo, size, size, true);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Nullable
    public String getContactName(@Nullable  Uri uri) {
        if (uri == null) return null;
        String name;
        int index;
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                name = cursor.getString(index);
                return name;
            }
        } finally {
            cursor.close();
        }
        return "";
    }

    /**
     * Creates an avatar Drawable based on the baseAvatarDrawable that contains the bitmap if present
     * or a color based on the person's color index else
     * @param photo contact's photo
     * @param color color int to use when there is no photo (i.e. photo==null)
     */
    @NonNull
    public Drawable makeAvatarDrawable(@Nullable Bitmap photo, @ColorInt int color) {
        if (photo == null) {
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, null);
            assert drawable != null;
            // min API 29: drawable.mutate().setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
            drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            return drawable;
        } else {
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), photo);
            drawable.setCircular(true);
            return drawable;
        }
    }
}
