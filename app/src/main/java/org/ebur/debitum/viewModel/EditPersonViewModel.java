package org.ebur.debitum.viewModel;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.material.textfield.TextInputLayout;

import org.ebur.debitum.BR;
import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.util.ContactsHelper;
import org.ebur.debitum.util.Utilities;

import java.util.concurrent.ExecutionException;

public class EditPersonViewModel extends AndroidViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private final PersonRepository repository;
    @Nullable private Integer id;
    @Nullable private String name;
    @Nullable private String note;
    @Nullable private Uri contactUri;
    @Nullable private CharSequence nameError;
    @Nullable private String originalName;

    public EditPersonViewModel(Application application) {
        super(application);
        repository = new PersonRepository(application);
        id = null;
        name = null;
        note = null;
        contactUri = null;
        nameError = null;
        originalName = null;
    }

    @Nullable public Integer getID() {
        return id;
    }
    public void setId(@Nullable Integer id) {
        if (!Utilities.equal(this.id, id)) {
            this.id = id;
        }
    }
    public boolean isNewPerson() {
        return id == null;
    }

    @Bindable
    @Nullable public String getName() {
        return name;
    }
    public void setName(@Nullable String name) {
        if (!Utilities.equal(this.name, name)) {
            this.name = name;
            notifyPropertyChanged(BR.name);
            notifyPropertyChanged(BR.avatarLetter);

            if (originalName == null) {
                this.originalName = name;
            }
            // refresh avatar
            setContactUri(contactUri);

            // reset error message
            if (name != null && !name.isEmpty()) {
                setNameError(null);
                notifyPropertyChanged(BR.nameError);
            }
        }
    }

    @Bindable
    @Nullable public String getNote() {
        return note;
    }
    public void setNote(@Nullable String note) {
        if (!Utilities.equal(this.note, note)) {
            this.note = note;
            notifyPropertyChanged(BR.note);
        }
    }

    public void setContactUri(@Nullable Uri uri) {
        if (Utilities.equal(contactUri, uri)) {
            this.contactUri = uri;
            notifyPropertyChanged(BR.contactName);
            notifyPropertyChanged(BR.contactDrawable);
            notifyPropertyChanged(BR.avatarLetter);
            notifyPropertyChanged(BR.contactHint);
        }
    }

    @Bindable
    @Nullable public String getContactName() {
        return contactUri == null ? null : ContactsHelper.getContactName(contactUri, getApplication());
    }

    @Bindable
    @NonNull public Drawable getContactDrawable() {
        // this will yield either the photo (if uri != null and a photo is there) or a
        // generated color based on the person's color index
        @ColorInt int secondaryColorRGB = Utilities.getAttributeColor(getApplication(), R.attr.colorSecondary);
        return ContactsHelper.makeAvatarDrawable(
                ContactsHelper.getContactImage(contactUri, getApplication()),
                new Person(name).getColor(secondaryColorRGB),
                getApplication()
        );
    }

    @Bindable
    @Nullable public CharSequence getAvatarLetter() {
        // null if we have a photo, else name's first char in upper case
        if (getContactDrawable() instanceof RoundedBitmapDrawable
                || name == null || name.isEmpty()) {
            return null;
        } else {
            return String.valueOf(name.charAt(0)).toUpperCase();
        }
    }

    @Bindable
    @NonNull public CharSequence getContactHint() {
       @StringRes int hint =
               contactUri == null ? R.string.edit_person_hint_no_linked_contact
               : R.string.edit_person_hint_linked_contact;
       return getApplication().getResources().getString(hint);
    }

    @Bindable
    @Nullable public CharSequence getNameError() {
        return nameError;
    }
    public void setNameError(@Nullable CharSequence text) {
        if(!Utilities.equal(nameError, text)) {
            nameError = text;
        }
    }

    @Nullable
    public String getOriginalName() {
        return  originalName;
    }

    public void setEditedPerson(@Nullable Person person) {
        boolean noone = person == null;
        setId(noone?null:person.idPerson);
        this.originalName = noone?null:person.name;
        setName(noone?null:person.name);
        setNote(noone?null:person.note);
        setContactUri(noone?null:person.linkedContactUri);
    }

    public boolean personExists(String name) throws ExecutionException, InterruptedException {
        return repository.exists(name);
    }
    public void writePersonToDb() {
        if (isNewPerson()) addPerson();
        else updatePerson();
    }

    private Person assemblePerson() {
        Person person = new Person(
                name,
                note,
                contactUri
        );
        if (!isNewPerson()) {
            person.idPerson = id;
        }
        return person;
    }
    private void addPerson() {
        repository.insert(assemblePerson());
    }
    private void updatePerson() {
        repository.update(assemblePerson());
    }

    public void savePerson() throws ExecutionException, InterruptedException{
        Resources res = getApplication().getResources();
        // check if nameView has contents and get name
        if(name == null || TextUtils.isEmpty(name)) {
            setNameError(res.getString(R.string.error_message_enter_name));
            return;
        }
        // check if name was changed and if it was, check if this name already exists
        if(!name.equals(originalName)
                && personExists(name)) {
            setNameError(res.getString(R.string.error_message_name_exists, name));
        } else {
            writePersonToDb();
        }
    }

    @BindingAdapter("app:textIfEmpty")
    public static void setTextIfEmpty(@NonNull TextView view, @Nullable CharSequence text) {
        // set view's text to text if it is null or ""
        if (text != null
                && !text.toString().isEmpty()
                && (view.getText() == null || view.getText().toString().isEmpty())) {
            view.setText(text);
        }
    }

    @BindingAdapter("app:errorText")
    public static void setErrorText(@NonNull TextInputLayout textInputLayout, @Nullable CharSequence errorText) {
        textInputLayout.setError(errorText);
    }

    @Override
    public void addOnPropertyChangedCallback(
            @NonNull Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(
            @NonNull Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * Notifies observers that all properties of this instance have changed.
     */
    void notifyChange() {
        callbacks.notifyCallbacks(this, 0, null);
    }

    /**
     * Notifies observers that a specific property has changed. The getter for the
     * property that changes should be marked with the @Bindable annotation to
     * generate a field in the BR class to be used as the fieldId parameter.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

    // TODO use this in the layout
    /*@BindingAdapter(value = {"app:name", "app:contactUri"}, requireAll = true)
    public static void setImageDrawable(@NonNull ImageView imageView, @Nullable String name, @Nullable Uri uri) {
        // this will yield either the photo (if uri != null and a photo is there) or a
        // generated color based on the person's color index
        @ColorInt int secondaryColorRGB = Utilities.getAttributeColor(, R.attr.colorSecondary);
        Drawable drawable = ContactsHelper.makeAvatarDrawable(
                ContactsHelper.getContactImage(uri, getApplication()),
                new Person(name).getColor(secondaryColorRGB),
                getApplication()
        );
    }*/
}
