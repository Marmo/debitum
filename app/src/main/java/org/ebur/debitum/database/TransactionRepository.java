package org.ebur.debitum.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TransactionRepository {

    private TransactionDao transactionDao;

    private LiveData<List<TransactionWithPerson>> allTransactions;
    private LiveData<List<PersonWithTransactions>> allPersonsWithTransactions;

    // Note that in order to unit test the Repository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);

        transactionDao = db.transactionDao();

        allTransactions = transactionDao.getAllTransactions();
        allPersonsWithTransactions = transactionDao.getAllPersonsWithTransactions();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<TransactionWithPerson>> getAllTransactions() {
        return allTransactions;
    }
    public LiveData<List<PersonWithTransactions>> getAllPersonsWithTransactions() { return allPersonsWithTransactions; }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Transaction transaction) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            transactionDao.insert(transaction);
        });
    }
}

