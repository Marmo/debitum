package org.ebur.debitum.viewModel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;

import java.util.concurrent.ExecutionException;

public class EditPersonViewModel extends AndroidViewModel {

    private final PersonRepository repository;
    @NonNull private Person editedPerson;
    private String originalName;
    private final MutableLiveData<Boolean> contactLinkingEnabled;

    public EditPersonViewModel(Application application) {
        super(application);
        repository = new PersonRepository(application);
        editedPerson = new Person(-1);
        contactLinkingEnabled = new MutableLiveData<>(false);
    }

    public boolean isNewPerson() { return editedPerson.idPerson == -1; }

    @NonNull public Person getEditedPerson() { return editedPerson; }
    // this should only be called once when the edited person is not yet changed
    public void setEditedPerson(Person editedPerson) {
        this.editedPerson = editedPerson;
        this.originalName = editedPerson.name;
    }

    public String getOriginalName() {
        return  originalName;
    }

    public void setLinkedContactUri(@Nullable Uri uri) {
        editedPerson.linkedContactUri = uri;
    }

    @NonNull public LiveData<Boolean> isContactLinkingEnabled() {
        return contactLinkingEnabled;
    }
    public void setContactLinkingEnabled(boolean enabled) {
        contactLinkingEnabled.setValue(enabled);
    }

    public boolean personExists(String name) throws ExecutionException, InterruptedException { return repository.exists(name); }
    public void writePersonToDb() {
        if (isNewPerson()) addPerson();
        else updatePerson();
    }
    private void addPerson() {
        repository.insert(new Person(
                editedPerson.name,
                editedPerson.note,
                editedPerson.linkedContactUri
        ));
    }
    private void updatePerson() { repository.update(editedPerson); }
}
