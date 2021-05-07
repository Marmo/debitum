package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PersonSumListViewModel extends AndroidViewModel {

    private final TransactionRepository txnRepository;
    private final PersonRepository personRepository;

    private final LiveData<List<PersonWithTransactions>> personsWithTransactions;

    public PersonSumListViewModel(Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        personRepository = new PersonRepository(application);
        personsWithTransactions = txnRepository.getAllPersonsWithTransactions();
    }

    public LiveData<List<PersonWithTransactions>> getPersonsWithTransactions() { return personsWithTransactions; }

    public Person getPersonById(int idPerson) throws ExecutionException, InterruptedException {
        return personRepository.getPersonById(idPerson);
    }

    public void insert(Transaction transaction) { txnRepository.insert(transaction); }

    public void delete(Person person) {
        personRepository.delete(person);
    }
}