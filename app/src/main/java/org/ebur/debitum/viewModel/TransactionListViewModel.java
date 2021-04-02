package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {

    private final TransactionRepository repository;

    private final LiveData<List<TransactionWithPerson>> transactions;
    private final LiveData<List<Person>> persons;
    private final LiveData<List<PersonWithTransactions>> personsWithTransactions;

    public TransactionListViewModel (Application application) {
        super(application);
        repository = new TransactionRepository(application);
        transactions = repository.getAllTransactions();
        persons = repository.getAllPersons();
        personsWithTransactions = repository.getAllPersonsWithTransactions();
    }

    public LiveData<List<TransactionWithPerson>> getTransactions() { return transactions; }
    public LiveData<List<Person>> getPersons() { return persons; }
    public LiveData<List<PersonWithTransactions>> getPersonsWithTransactions() { return personsWithTransactions; }

    public void insert(Transaction transaction) { repository.insert(transaction); }
    public void insert(Person person) { repository.insert(person); }
}