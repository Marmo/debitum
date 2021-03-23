package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.Transaction;

import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {

    private TransactionRepository repository;

    private final LiveData<List<Transaction>> transactions;

    public TransactionListViewModel (Application application) {
        super(application);
        repository = new TransactionRepository(application);
        transactions = repository.getAllTransactions();
    }

    public LiveData<List<Transaction>> getTransactions() { return transactions; }

    public void insert(Transaction transaction) { repository.insert(transaction);}

}