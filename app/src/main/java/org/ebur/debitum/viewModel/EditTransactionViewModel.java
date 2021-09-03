package org.ebur.debitum.viewModel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditTransactionViewModel extends AndroidViewModel {

    private final PersonRepository personRepository;
    private final TransactionRepository transactionRepository;
    private TransactionWithPerson transaction;
    // maintained separately from transaction for new transactions (transaction = null)
    private int transactionType;
    private Date timestamp;
    private Date returnTimestamp;
    private MutableLiveData<List<Uri>> imageUris;


    public EditTransactionViewModel(Application application) {
        super(application);
        personRepository = new PersonRepository(application);
        transactionRepository = new TransactionRepository(application);
        imageUris = new MutableLiveData<>();
    }

    public List<Person> getPersons() throws ExecutionException, InterruptedException { return  personRepository.getAllPersonsNonLive(); }

    public void setTransaction(TransactionWithPerson twp) {
        this.transaction = twp;
        if (twp != null) {
            this.transactionType = twp.transaction.isMonetary ? Transaction.TYPE_MONEY : Transaction.TYPE_ITEM;
        }
    }
    public TransactionWithPerson getTransaction() {
        return transaction;
    }

    public void setTransactionType(int type) {
        this. transactionType = type;
    }
    public boolean isMoneyTransaction() {
        return transactionType == Transaction.TYPE_MONEY;
    }
    public boolean isItemTransaction() {
        return transactionType == Transaction.TYPE_ITEM;
    }

    public int getPersonId(String name) throws ExecutionException, InterruptedException { return personRepository.getPersonId(name); }

    public boolean isNewTransaction() { return transaction == null; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Date getTimestamp() { return this.timestamp; }
    public void setReturnTimestamp(Date timestamp) { this.returnTimestamp = timestamp; }
    public Date getReturnTimestamp() { return this.returnTimestamp; }

    public TransactionWithPerson getTransactionFromDatabase(int idTransaction) throws ExecutionException, InterruptedException { return transactionRepository.getTransaction(idTransaction); }
    public void insert(Transaction transaction) { transactionRepository.insert(transaction); }
    public void update(Transaction transaction) { transactionRepository.update(transaction); }
    public void delete(Transaction transaction) { transactionRepository.delete(transaction); }

    public LiveData<List<Uri>> getImageUris() {return imageUris;}
    public void removeImage(@NonNull Uri uri) {
        List<Uri> uris = imageUris.getValue();
        if (uris != null) {
            uris.remove(uri);
            imageUris.setValue(uris);
        }
    }
    public void addImageUri(@NonNull Uri uri) {
        List<Uri> uris = imageUris.getValue();
        if (uris == null) uris = new ArrayList<>();
        uris.add(uri);
        imageUris.setValue(uris);
    }

}