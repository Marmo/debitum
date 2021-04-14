package org.ebur.debitum.viewModel;

import android.app.Application;
import android.view.Menu;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.R;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {
    // TODO unify EditTransactionViewModel and EditPersonViewModel into BaseViewModel and derive the specialised View Models from this

    private final TransactionRepository txnRepository;

    private final LiveData<List<TransactionWithPerson>> transactions;
    private final LiveData<List<PersonWithTransactions>> personsWithTransactions;

    public TransactionListViewModel (Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        transactions = txnRepository.getAllTransactions();
        personsWithTransactions = txnRepository.getAllPersonsWithTransactions();
    }

    public LiveData<List<TransactionWithPerson>> getTransactions() { return transactions; }
    public LiveData<List<PersonWithTransactions>> getPersonsWithTransactions() { return personsWithTransactions; }

    public void insert(Transaction transaction) { txnRepository.insert(transaction); }


}