package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PersonSumListViewModel extends AndroidViewModel {

    private final TransactionRepository txnRepository;
    private final PersonRepository personRepository;

    public static final int ORDER_ASC = 0;
    public static final int ORDER_DESC = 1;
    public static final int ORDER_NAME = 0<<1;
    public static final int ORDER_DATE = 1<<1;
    public static final int ORDER_AMOUNT = 2<<1;
    public static final int ORDER_MASK_DIR = 0b001;
    public static final int ORDER_MASK_BY = 0b110;

    private final LiveData<List<PersonWithTransactions>> personsWithTransactions;
    private final MutableLiveData<Integer> order;

    public PersonSumListViewModel(Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        personRepository = new PersonRepository(application);
        personsWithTransactions = txnRepository.getAllPersonsWithTransactions();
        order = new MutableLiveData<>(ORDER_DATE & ORDER_DESC);
    }

    public LiveData<List<PersonWithTransactions>> getPersonsWithTransactions() { return personsWithTransactions; }

    public Person getPersonById(int idPerson) throws ExecutionException, InterruptedException {
        return personRepository.getPersonById(idPerson);
    }

    public void insert(Transaction transaction) { txnRepository.insert(transaction); }

    public void delete(Person person) {
        personRepository.delete(person);
    }

    // TODO another possibility would be to pass the order-by SQL part to txnRepository.getAllPersonsWithTransactions()
    //  and re-call this method in setOrder()
    public void setOrder(int by, boolean asc) {
        int direction = asc?ORDER_ASC:ORDER_DESC;
        if (by == ORDER_NAME || by == ORDER_DATE || by == ORDER_AMOUNT) {
            order.setValue(by | direction);
        }
        else throw new IllegalArgumentException("Unknown by value: " + by);
    }

    @NonNull
    public LiveData<Integer> getOrder() {
        return order;
    }

    public boolean isOrderAscending() {
        return (order.getValue() & ORDER_MASK_DIR) == ORDER_ASC;
    }

    public int getOrderBy() {
        return (order.getValue() & ORDER_MASK_BY);
    }
}