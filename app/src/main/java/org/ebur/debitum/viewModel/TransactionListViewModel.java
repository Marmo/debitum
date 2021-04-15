package org.ebur.debitum.viewModel;

import android.app.Application;
import android.view.Menu;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TransactionListViewModel extends AndroidViewModel {

    private final TransactionRepository txnRepository;
    private final PersonRepository personRepository;

    private final LiveData<List<TransactionWithPerson>> transactions;
    private Person filterPerson;

    public TransactionListViewModel (Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        personRepository = new PersonRepository(application);
        transactions = txnRepository.getAllTransactions();
    }

    public LiveData<List<TransactionWithPerson>> getTransactions() { return transactions; }
    public void setFilterPerson(int idPerson) throws ExecutionException, InterruptedException {
        filterPerson = personRepository.getPersonById(idPerson);
    }
    public Person getFilterPerson() { return filterPerson; }

    public void insert(Transaction transaction) { txnRepository.insert(transaction); }


}