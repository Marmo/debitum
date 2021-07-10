package org.ebur.debitum.viewModel;

import android.app.Application;
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
import androidx.lifecycle.AndroidViewModel;

import org.ebur.debitum.R;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

public class ContactsHelper extends AndroidViewModel {

    private final String TAG = "ContactsHelper";

    private final HashMap<Uri, Contact> contactCache;

    public static class Contact {
        @NonNull String name;
        @Nullable Bitmap photo;

        Contact(@NonNull String name, @Nullable Bitmap photo) {
            this.name = name;
            this.photo = photo;
        }
    }

    public ContactsHelper(Application application) {
        super(application);
        contactCache = new HashMap<>();
    }

    private void cacheContactInfo(@NonNull Uri uri, @NonNull Contact contact) {
        contactCache.put(uri, contact);
    }

    private boolean isCached(@Nullable Uri uri) {
        if (uri == null || contactCache == null) {
            return false;
        } else {
            return contactCache.containsKey(uri);
        }
    }

    @Nullable
    private Bitmap getContactImageFromCache(@NonNull Uri uri) {
        if (contactCache != null) {
            Contact contact = contactCache.get(uri);
            return contact != null ? contact.photo : null;
        } else {
            return null;
        }
    }

    @Nullable
    private Bitmap getContactImageFromContentProvider(@NonNull Uri uri) {
        Uri photoUri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        String[] photoProjection = new String[] {ContactsContract.Contacts.Photo.PHOTO};
        Bitmap photo;
        Cursor cursor = getApplication().getContentResolver()
                .query(photoUri, photoProjection, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    photo = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                    int size = getApplication().getResources().getInteger(R.integer.avatar_size);
                    return Bitmap.createScaledBitmap(photo, size, size, true);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Nullable
    public Bitmap getContactImage(@Nullable Uri uri) {
        if (uri == null) return null;
        if (isCached(uri)) {
            return getContactImageFromCache(uri);
        } else {
            // cache contact and return its photo
            Bitmap photo = getContactImageFromContentProvider(uri);
            cacheContactInfo(
                    uri,
                    new Contact(
                            getContactNameFromContentProvider(uri),
                            photo
                    )
            );
            return photo;
        }
    }

    @Nullable
    private String getContactNameFromCache(@NonNull Uri uri) {
        if (contactCache != null) {
            Contact contact = contactCache.get(uri);
            return contact != null ? contact.name : null;
        } else {
            return null;
        }
    }

    @NonNull
    private String getContactNameFromContentProvider(@NonNull Uri uri) {
        @NonNull String name;
        int index;
        try (Cursor cursor = getApplication().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                name = cursor.getString(index);
                return name;
            }
        }
        return "";
    }

    @Nullable
    public String getContactName(@Nullable Uri uri) {
        if (uri == null) return null;
        if (isCached(uri)) {
            return getContactNameFromCache(uri);
        } else {
            // cache contact and return its name
            String name = getContactNameFromContentProvider(uri);
            cacheContactInfo(
                    uri,
                    new Contact(
                            name,
                            getContactImageFromContentProvider(uri)
                    )
            );
            return name.isEmpty() ? null : name;
        }
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
            Drawable drawable = ResourcesCompat.getDrawable(getApplication().getResources(), R.drawable.circle, null);
            assert drawable != null;
            // min API 29: drawable.mutate().setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
            drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            return drawable;
        } else {
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getApplication().getResources(), photo);
            drawable.setCircular(true);
            return drawable;
        }
    }
}
