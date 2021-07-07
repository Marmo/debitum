package org.ebur.debitum;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;

import java.util.HashMap;

public abstract class ContactsHelper {

    private static class Contact {
        String name;
        Drawable photo;

        Contact(String name, Drawable photo) {
            this.name = name;
            this.photo = photo;
        }
    }

    private HashMap<Integer, Contact> contactCache;

    @Nullable
    public static Drawable getContactImage(Uri uri) {
        return null;
    }

    public static String getContactName(Uri uri, ContentResolver resolver) {
        String name = "Ehm .. dunno";
        int index;
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            name = cursor.getString(index);
        }
        cursor.close();
        return name;
    }
}
