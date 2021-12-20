package org.ebur.debitum.database;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TransactionRepository {

    private final TransactionDao transactionDao;

    private final LiveData<List<TransactionWithPerson>> allMoneyTransactions;
    private final LiveData<List<TransactionWithPerson>> allItemTransactions;
    private final LiveData<List<PersonWithTransactions>> allPersonsWithTransactions;

    // Note that in order to unit test the Repository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);

        transactionDao = db.transactionDao();

        allMoneyTransactions = transactionDao.getAllTransactions(true);
        allItemTransactions = transactionDao.getAllTransactions(false);
        allPersonsWithTransactions = transactionDao.getAllPersonsWithTransactions();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<TransactionWithPerson>> getAllMoneyTransactions() {
        return allMoneyTransactions;
    }

    public LiveData<List<TransactionWithPerson>> getAllItemTransactions() {
        return allItemTransactions;
    }

    public LiveData<List<PersonWithTransactions>> getAllPersonsWithTransactions() { return allPersonsWithTransactions; }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    @Nullable
    public Long insert(Transaction transaction) {
        Future<Long> future = AppDatabase.databaseTaskExecutor.submit( () -> transactionDao.insert(transaction));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update(Transaction transaction) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            transactionDao.update(transaction);
        });
    }

    public void delete(Transaction transaction) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            transactionDao.delete(transaction);
        });
    }

    public TransactionWithPerson getTransaction(int idTransaction) throws ExecutionException, InterruptedException {
        Future<TransactionWithPerson> future = AppDatabase.databaseTaskExecutor.submit( () -> transactionDao.getTransaction(idTransaction));
        return future.get();
    }

    public void changeTransactionDecimals(int shift) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            transactionDao.changeDecimals(shift);
        });
    }
}

