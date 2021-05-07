package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {

    private final TransactionRepository txnRepository;

    private final LiveData<List<TransactionWithPerson>> moneyTransactions;
    private final LiveData<List<TransactionWithPerson>> itemTransactions;

    public TransactionListViewModel (Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        moneyTransactions = txnRepository.getAllMoneyTransactions();
        itemTransactions = txnRepository.getAllItemTransactions();
    }

    public LiveData<List<TransactionWithPerson>> getMoneyTransactions() {
        return moneyTransactions;
    }
    public LiveData<List<TransactionWithPerson>> getItemTransactions() {
        return itemTransactions;
    }

    public void insert(Transaction transaction) {
        txnRepository.insert(transaction);
    }

    public void delete(Transaction transaction) {
        txnRepository.delete(transaction);
    }

}