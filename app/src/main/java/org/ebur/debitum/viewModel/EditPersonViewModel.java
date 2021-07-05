package org.ebur.debitum.viewModel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;

import java.util.concurrent.ExecutionException;

public class EditPersonViewModel extends AndroidViewModel {

    private final PersonRepository repository;
    private Person editedPerson;

    public EditPersonViewModel(Application application) {
        super(application);
        repository = new PersonRepository(application);
    }

    public boolean isNewPerson() { return editedPerson.idPerson == -1; }

    public Person getEditedPerson() { return editedPerson; }
    public void setEditedPerson(Person editedPerson) { this.editedPerson = editedPerson; }

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

    public void setLinkedContactUri(@Nullable Uri uri) {
        editedPerson.linkedContactUri = uri;
    }
}
