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

    private final PersonRepository personRepository;
    private final TransactionRepository transactionRepository;
    //private final LiveData<List<Person>> persons;
    private int idTransaction = -1;
    private Date timestamp;
    private String name = "";
    private boolean newTransaction;

    public EditTransactionViewModel(Application application) {
        super(application);
        personRepository = new PersonRepository(application);
        transactionRepository = new TransactionRepository(application);
        //persons = personRepository.getAllPersons();
    }

    //public LiveData<List<Person>> getPersons() { return persons; }
    public List<Person> getPersons() throws ExecutionException, InterruptedException { return  personRepository.getAllPersonsNonLive(); }

    public void setIdTransaction(int id) { this.idTransaction = id; }
    public int getIdTransaction() { return this.idTransaction; }

    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }
    public int getSelectedPersonId() throws ExecutionException, InterruptedException { return personRepository.getPersonId(this.name); }

    public boolean isNewTransaction() { return newTransaction; }
    public void setNewTransaction(boolean newTransaction) { this.newTransaction = newTransaction; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Date getTimestamp() { return this.timestamp; }

    public TransactionWithPerson getTransaction(int idTransaction) throws ExecutionException, InterruptedException { return transactionRepository.getTransaction(idTransaction); }
    public void insert(Transaction transaction) { transactionRepository.insert(transaction); }
    public void update(Transaction transaction) { transactionRepository.update(transaction); }
    public int delete(Transaction transaction) throws ExecutionException, InterruptedException { return transactionRepository.deleteAndCount(transaction); }
}