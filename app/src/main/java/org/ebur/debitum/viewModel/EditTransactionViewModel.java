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
    private String selectedName = "";

    public EditTransactionViewModel(Application application) {
        super(application);
        personRepository = new PersonRepository(application);
        transactionRepository = new TransactionRepository(application);
    }

    public List<Person> getPersons() throws ExecutionException, InterruptedException { return  personRepository.getAllPersonsNonLive(); }

    public void setIdTransaction(int id) { this.idTransaction = id; }
    public int getIdTransaction() { return this.idTransaction; }

    public void setSelectedName(String selectedName) { this.selectedName = selectedName; }
    public String getSelectedName() { return this.selectedName; }
    public int getSelectedPersonId() throws ExecutionException, InterruptedException { return personRepository.getPersonId(this.selectedName); }

    public boolean isNewTransaction() { return idTransaction == -1; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Date getTimestamp() { return this.timestamp; }

    public TransactionWithPerson getTransaction(int idTransaction) throws ExecutionException, InterruptedException { return transactionRepository.getTransaction(idTransaction); }
    public void insert(Transaction transaction) { transactionRepository.insert(transaction); }
    public void update(Transaction transaction) { transactionRepository.update(transaction); }
    public void delete(Transaction transaction) { transactionRepository.delete(transaction); }
}