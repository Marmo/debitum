package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditTransactionViewModel extends AndroidViewModel {

    private final PersonRepository repository;
    private final LiveData<List<Person>> persons;
    private Date timestamp;
    private String name = "";
    private boolean newTransaction;

    public EditTransactionViewModel(Application application) {
        super(application);
        repository = new PersonRepository(application);
        persons = repository.getAllPersons();
    }

    public LiveData<List<Person>> getPersons() { return persons; }

    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }
    public int getSelectedPersonId() throws ExecutionException, InterruptedException { return repository.getPersonId(this.name);
    }

    public boolean isNewTransaction() { return newTransaction; }
    public void setNewTransaction(boolean newTransaction) { this.newTransaction = newTransaction; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Date getTimestamp() { return this.timestamp; }
}