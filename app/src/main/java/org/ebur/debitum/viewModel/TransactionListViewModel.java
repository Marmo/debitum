package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonRepository;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.HashMap;
import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {
    // TODO unify EditTransactionViewModel and EditPersonViewModel into BaseViewModel and derive the specialised View Models from this

    private final TransactionRepository txnRepository;

    private final LiveData<List<TransactionWithPerson>> transactions;
    private final LiveData<List<PersonWithTransactions>> personsWithTransactions;

    private final MutableLiveData<HashMap<Integer, Boolean>> toolbarMenuItems;

    public TransactionListViewModel (Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        transactions = txnRepository.getAllTransactions();
        personsWithTransactions = txnRepository.getAllPersonsWithTransactions();

        HashMap<Integer, Boolean> actions = new HashMap<>();
        actions.put(R.id.miAddPerson, false);
        actions.put(R.id.miDeleteTransaction, false);
        actions.put(R.id.miEditTransaction, false);
        toolbarMenuItems = new MutableLiveData<HashMap<Integer, Boolean>>(actions);
    }

    public LiveData<List<TransactionWithPerson>> getTransactions() { return transactions; }
    public LiveData<List<PersonWithTransactions>> getPersonsWithTransactions() { return personsWithTransactions; }
    public LiveData<HashMap<Integer, Boolean>> getToolbarMenuItems() {return toolbarMenuItems; }

    public void insert(Transaction transaction) { txnRepository.insert(transaction); }

    public void showToolbarMenuItem(int menuItem) { setToolbarMenuItemVisibility(menuItem, true); }
    public void hideToolbarMenuItem(int menuItem) { setToolbarMenuItemVisibility(menuItem, false); }
    private void setToolbarMenuItemVisibility(int toolbarAction, boolean visible) {
        HashMap<Integer, Boolean> actions = toolbarMenuItems.getValue();
        actions.put(toolbarAction, visible);
        toolbarMenuItems.setValue(actions);
    }
}