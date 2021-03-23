package org.ebur.debitum.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TransactionRepository {

    private TransactionDAO transactionDao;
    private LiveData<List<Transaction>> allTransactions;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public TransactionRepository(Application application) {
        TransactionDatabase db = TransactionDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        allTransactions = transactionDao.getAllTransactions();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Transaction transaction) {
        TransactionDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.insert(transaction);
        });
    }
}

