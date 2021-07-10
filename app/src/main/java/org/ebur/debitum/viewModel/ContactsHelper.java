package org.ebur.debitum.viewModel;

import android.app.Application;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;

public class ContactsHelper extends AndroidViewModel {

    private final String TAG = "ContactsHelper";

    private final MutableLiveData<HashMap<Uri, Contact>> contactCache;

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
        contactCache = new MutableLiveData<>(new HashMap<>());
    }

    public LiveData<HashMap<Uri, Contact>> getContacts() {
        return contactCache;
    }

    private void cacheContactInfo(@NonNull Uri uri, @NonNull Contact contact) {
        HashMap<Uri, Contact> cache = contactCache.getValue();
        if (cache != null) {
            cache.put(uri, contact);
            contactCache.setValue(cache);
        }
    }

    private void clearContactCache() {
        HashMap<Uri, Contact> cache = contactCache.getValue();
        if (cache != null) {
            cache.clear();
        } else {
            cache = new HashMap<>();
        }
        contactCache.setValue(cache);
    }
    
    public void refreshContactsCache(List<Person> persons) {
        clearContactCache();
        String name;
        Bitmap photo;
        for (Person person : persons) {
            if(person.linkedContactUri != null) {
                name = getContactName(person.linkedContactUri);
                if(name == null) {
                    Log.e(TAG, "Something went wrong getting contact name for "+person.name+" (URI "+person.linkedContactUri+")");
                    break; //
                }
                photo = getContactImage(person.linkedContactUri);
                cacheContactInfo(person.linkedContactUri, new Contact(name, photo));
            }
        }
    }

    private boolean isCached(@Nullable Uri uri) {
        HashMap<Uri, Contact> cache = contactCache.getValue();
        if (uri == null || cache == null) {
            return false;
        } else {
            return cache.containsKey(uri);
        }
    }

    @Nullable
    private Bitmap getContactImageFromCache(@NonNull Uri uri) {
        HashMap<Uri, Contact> cache = contactCache.getValue();
        if (cache != null) {
            Contact contact = cache.get(uri);
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
        HashMap<Uri, Contact> cache = contactCache.getValue();
        if (cache != null) {
            Contact contact = cache.get(uri);
            return contact != null ? contact.name : null;
        } else {
            return null;
        }
    }

    @Nullable
    private String getContactNameFromContentProvider(@NonNull Uri uri) {
        String name;
        int index;
        Cursor cursor = getApplication().getContentResolver()
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

    @Nullable
    public String getContactName(@Nullable Uri uri) {
        if (uri == null) return null;
        if (isCached(uri)) {
            return getContactNameFromCache(uri);
        } else {
            // cache contact and return its name
            String name = getContactNameFromContentProvider(uri);
            if (name != null) {
                cacheContactInfo(
                        uri,
                        new Contact(
                                name,
                                getContactImageFromContentProvider(uri)
                        )
                );
            }
            return name;
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
