package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EditTransactionViewModel extends AndroidViewModel {

    public final static int TRANSACTION_TYPE_MONEY = 0;
    public final static int TRANSACTION_TYPE_ITEM = 1;

    private final PersonRepository personRepository;
    private final TransactionRepository transactionRepository;
    private TransactionWithPerson transaction;
    // maintained separately from transaction for new transactions (transaction = null)
    private int transactionType;
    private Date timestamp;


    public EditTransactionViewModel(Application application) {
        super(application);
        personRepository = new PersonRepository(application);
        transactionRepository = new TransactionRepository(application);
    }

    public List<Person> getPersons() throws ExecutionException, InterruptedException { return  personRepository.getAllPersonsNonLive(); }

    public void setTransaction(TransactionWithPerson twp) {
        this.transaction = twp;
        if (twp != null) {
            this.transactionType = twp.transaction.isMonetary ? TRANSACTION_TYPE_MONEY : TRANSACTION_TYPE_ITEM;
        }
    }
    public TransactionWithPerson getTransaction() {
        return transaction;
    }

    public void setTransactionType(int type) {
        this. transactionType = type;
    }
    public boolean isMoneyTransaction() {
        return transactionType == TRANSACTION_TYPE_MONEY;
    }
    public boolean isItemTransaction() {
        return transactionType == TRANSACTION_TYPE_ITEM;
    }

    public int getPersonId(String name) throws ExecutionException, InterruptedException { return personRepository.getPersonId(name); }

    public boolean isNewTransaction() { return transaction == null; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Date getTimestamp() { return this.timestamp; }

    public TransactionWithPerson getTransactionFromDatabase(int idTransaction) throws ExecutionException, InterruptedException { return transactionRepository.getTransaction(idTransaction); }
    public void insert(Transaction transaction) { transactionRepository.insert(transaction); }
    public void update(Transaction transaction) { transactionRepository.update(transaction); }
    public void delete(Transaction transaction) { transactionRepository.delete(transaction); }


}