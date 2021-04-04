package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {

    private final TransactionRepository txnRepository;
    private final PersonRepository persRepository;

    private final LiveData<List<TransactionWithPerson>> transactions;
    private final LiveData<List<Person>> persons;
    private final LiveData<List<PersonWithTransactions>> personsWithTransactions;

    public TransactionListViewModel (Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        persRepository = new PersonRepository(application);
        transactions = txnRepository.getAllTransactions();
        persons = persRepository.getAllPersons();
        personsWithTransactions = txnRepository.getAllPersonsWithTransactions();
    }

    public LiveData<List<TransactionWithPerson>> getTransactions() { return transactions; }
    public LiveData<List<Person>> getPersons() { return persons; }
    public LiveData<List<PersonWithTransactions>> getPersonsWithTransactions() { return personsWithTransactions; }

    public void insert(Transaction transaction) { txnRepository.insert(transaction); }
    //public void insert(Person person) { persRepository.insert(person); }
}