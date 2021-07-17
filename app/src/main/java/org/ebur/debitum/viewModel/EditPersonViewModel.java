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
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.material.textfield.TextInputLayout;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.util.ContactsHelper;
import org.ebur.debitum.util.Utilities;

import java.util.concurrent.ExecutionException;

public class EditPersonViewModel extends AndroidViewModel {

    private final PersonRepository repository;
    @NonNull private final MutableLiveData<Integer> id;
    @NonNull private final MutableLiveData<String> name;
    @NonNull private final MutableLiveData<String> note;
    @NonNull private final MutableLiveData<Uri> contactUri;
    @NonNull private final MutableLiveData<CharSequence> nameError;
    private String originalName;

    public EditPersonViewModel(Application application) {
        super(application);
        repository = new PersonRepository(application);
        id = new MutableLiveData<>(null);
        name = new MutableLiveData<>(null);
        note = new MutableLiveData<>(null);
        contactUri = new MutableLiveData<>(null);
        nameError = new MutableLiveData<>(null);
        originalName = null;
    }

    @NonNull public LiveData<Integer> getID() {
        return id;
    }
    public void setId(@Nullable Integer id) {
        this.id.setValue(id);
    }
    @NonNull public LiveData<Boolean> isNewPerson() {
        return Transformations.map(id, id -> id == null);
    }

    @NonNull public MutableLiveData<String> getName() {
        return name;
    }
    public void setName(@Nullable String name) {
        this.name.setValue(name);
        // this should only be called once when the edited person is not yet changed
        if (originalName == null) {
            this.originalName = name;
        }
        // refresh avatar
        setContactUri(contactUri.getValue());
        // reset error message
        if (name != null && !name.isEmpty()) {
            setNameError(null);
        }
    }

    @NonNull public MutableLiveData<String> getNote() {
        return note;
    }
    public void setNote(@Nullable String note) {
        this.note.setValue(note);
    }

    public void setContactUri(@Nullable Uri uri) {
        contactUri.setValue(uri);
    }

    @NonNull public LiveData<String> getContactName() {
        return Transformations.map(contactUri, uri -> {
            return uri == null ? null : ContactsHelper.getContactName(uri, getApplication());
        });
    }

    @NonNull public LiveData<Drawable> getContactDrawable() {
        return Transformations.map(contactUri, uri -> {
            // this will yield either the photo (if uri != null and a photo is there) or a
            // generated color based on the person's color index
            @ColorInt int secondaryColorRGB = Utilities.getAttributeColor(getApplication(), R.attr.colorSecondary);
            return ContactsHelper.makeAvatarDrawable(
                    ContactsHelper.getContactImage(uri, getApplication()),
                    new Person(name.getValue()).getColor(secondaryColorRGB),
                    getApplication()
            );
        });
    }

    @NonNull public LiveData<CharSequence> getAvatarLetter() {
        // null if we have a photo, else name's first char in upper case
        return Transformations.map(name, name -> {
            if (getContactDrawable().getValue() instanceof RoundedBitmapDrawable
                    || name == null || name.isEmpty()) {
                return null;
            } else {
                return String.valueOf(name.charAt(0)).toUpperCase();
            }
        });
    }

    @NonNull public LiveData<CharSequence> getContactHint() {
        return Transformations.map(contactUri, uri -> {
           @StringRes int hint =
                   uri == null ? R.string.edit_person_hint_no_linked_contact
                   : R.string.edit_person_hint_linked_contact;
           return getApplication().getResources().getString(hint);
        });
    }

    @NonNull public LiveData<CharSequence> getNameError() {
        return nameError;
    }
    public void setNameError(@Nullable CharSequence text) {
        nameError.setValue(text);
    }

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

    public boolean personExists(String name) throws ExecutionException, InterruptedException { return repository.exists(name); }
    public void writePersonToDb() {
        if (isNewPerson().getValue()) addPerson();
        else updatePerson();
    }

    private Person assemblePerson() {
        Person person = new Person(
                name.getValue(),
                note.getValue(),
                contactUri.getValue()
        );
        if (!isNewPerson().getValue()) {
            person.idPerson = id.getValue();
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
        String name = this.name.getValue();
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
