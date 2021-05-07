package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;

import java.util.concurrent.ExecutionException;

public class EditPersonViewModel extends AndroidViewModel {

    private final PersonRepository repository;
    //private final LiveData<List<Person>> persons;
    private Person editedPerson;

    public EditPersonViewModel(Application application) {
        super(application);
        repository = new PersonRepository(application);
        //persons = repository.getAllPersons();
    }

    public boolean isNewPerson() { return editedPerson == null; }

    public Person getEditedPerson() { return editedPerson; }
    public void setEditedPerson(Person editedPerson) { this.editedPerson = editedPerson; }

    public void addPerson(String name) {repository.insert(new Person(name));}
    public boolean personExists(String name) throws ExecutionException, InterruptedException { return repository.exists(name); }

    public void update(Person person) { repository.update(person); }
    public void delete(Person person) { repository.delete(person); }
}
