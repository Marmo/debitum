package org.ebur.debitum.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.R;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

public class ContactsHelper {

    private final String TAG = "ContactsHelper";

    private final static HashMap<Uri, Contact> contactCache = new HashMap<>();
    private final static MutableLiveData<Boolean> contactLinkingEnabled = new MutableLiveData<>(false);

    public static class Contact {
        @NonNull String name;
        @Nullable Bitmap photo;

        Contact(@NonNull String name, @Nullable Bitmap photo) {
            this.name = name;
            this.photo = photo;
        }
    }

    private static void cacheContactInfo(@NonNull Uri uri, @NonNull Contact contact) {
        contactCache.put(uri, contact);
    }

    private static boolean isCached(@Nullable Uri uri) {
        if (uri == null) {
            return false;
        } else {
            return contactCache.containsKey(uri);
        }
    }

    @Nullable
    private static Bitmap getContactImageFromCache(@NonNull Uri uri) {
        Contact contact = contactCache.get(uri);
        return contact != null ? contact.photo : null;
    }

    @Nullable
    private static Bitmap getContactImageFromContentProvider(@NonNull Uri uri, Context context) {
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
    public static Bitmap getContactImage(@Nullable Uri uri, Context context) {
        if (uri == null) return null;
        if (isCached(uri)) {
            return getContactImageFromCache(uri);
        } else {
            // cache contact and return its photo
            Bitmap photo = getContactImageFromContentProvider(uri, context);
            cacheContactInfo(
                    uri,
                    new Contact(
                            getContactNameFromContentProvider(uri, context),
                            photo
                    )
            );
            return photo;
        }
    }

    @Nullable
    private static String getContactNameFromCache(@NonNull Uri uri) {
        Contact contact = contactCache.get(uri);
        return contact != null ? contact.name : null;
    }

    @NonNull
    private static String getContactNameFromContentProvider(@NonNull Uri uri, Context context) {
        @NonNull String name;
        int index;
        try (Cursor cursor = context.getContentResolver()
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
    public static String getContactName(@Nullable Uri uri, Context context) {
        if (uri == null) return null;
        if (isCached(uri)) {
            return getContactNameFromCache(uri);
        } else {
            // cache contact and return its name
            String name = getContactNameFromContentProvider(uri, context);
            cacheContactInfo(
                    uri,
                    new Contact(
                            name,
                            getContactImageFromContentProvider(uri, context)
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
    public static Drawable makeAvatarDrawable(@Nullable Bitmap photo, @ColorInt int color, Context context) {
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


    @NonNull public static LiveData<Boolean> isContactLinkingEnabled() {
        return contactLinkingEnabled;
    }

    public static void setContactLinkingEnabled(boolean enabled) {
        contactLinkingEnabled.setValue(enabled);
    }

    /** Checks if the permission to read contacts is granted and asks for it if not. Depending on
     * the result the ContactHelper's contactLinkingEnabled member is set to true (granted) or
     * false (denied).
     *
     * @param requestPermissionLauncher ActivityResultLauncher<String> that defines what to do when
     *                                  the permission is granted or denied. Typical implementation:<br/>
     *                                  <code>ActivityResultLauncher<String> requestPermissionLauncher =
     *                 registerForActivityResult(new ActivityResultContracts.RequestPermission(),
     *                         isGranted -> contactsHelper.setContactLinkingEnabled(isGranted));</code>
     */
    public static void checkReadContactsPermission(ActivityResultLauncher<String> requestPermissionLauncher, Context context) {
        // check and ask for permission
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            setContactLinkingEnabled(true);
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }
}
