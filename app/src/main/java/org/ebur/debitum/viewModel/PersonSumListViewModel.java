package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonWithSum;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;

import java.util.List;

public class PersonSumListViewModel extends AndroidViewModel {

    private final TransactionRepository repository;

    private final LiveData<List<PersonWithSum>> personSums;
    //private final LiveData<List<Person>> persons;

    public PersonSumListViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
        personSums = repository.getAllPersonSums();
        //persons = repository.getAllPersons();
    }

    public LiveData<List<PersonWithSum>> getPersonSums() { return personSums; }
    //public LiveData<List<Person>> getPersons() { return persons; }

    public void insert(Transaction transaction) { repository.insert(transaction); }
    public void insert(Person person) { repository.insert(person); }
}