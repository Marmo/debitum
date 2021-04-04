package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;

import java.util.List;

public class EditPersonViewModel extends AndroidViewModel {

    private final PersonRepository repository;
    private final LiveData<List<Person>> persons;
    private boolean newPerson;

    public EditPersonViewModel(Application application) {
        super(application);
        repository = new PersonRepository(application);
        persons = repository.getAllPersons();
    }

    public LiveData<List<Person>> getPersons() { return persons; }

    public void addPerson(String name) {repository.insert(new Person(name));}
    public boolean personExists(String name) { return repository.exists(name); }
}
