package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddTransactionViewModel extends AndroidViewModel {

    private final TransactionRepository repository;
    private final LiveData<List<Person>> persons;
    private Date timestamp;
    private String name = "";

    public AddTransactionViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
        persons = repository.getAllPersons();
    }

    public LiveData<List<Person>> getPersons() { return persons; }

    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }
    public int getPersonId() {
        for (Person person : persons.getValue()) {
            if(person.name.equals(this.name)) return person.idPerson;
        }
        return -1;
    }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Date getTimestamp() { return this.timestamp; }
}