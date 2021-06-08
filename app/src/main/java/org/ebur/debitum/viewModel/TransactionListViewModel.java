package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public Transaction getTransactionFromDatabase(int idTransaction) throws ExecutionException, InterruptedException { return txnRepository.getTransaction(idTransaction).transaction; }

    public void insert(Transaction transaction) {
        txnRepository.insert(transaction);
    }
    public void update(Transaction transaction) { txnRepository.update(transaction); }
    public void delete(Transaction transaction) {
        txnRepository.delete(transaction);
    }

}