package org.ebur.debitum.viewModel;

import android.app.Application;
import android.view.Menu;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.R;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.TransactionRepository;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransactionListViewModel extends AndroidViewModel {
    // TODO unify EditTransactionViewModel and EditPersonViewModel into BaseViewModel and derive the specialised View Models from this

    private final TransactionRepository txnRepository;

    private final LiveData<List<TransactionWithPerson>> transactions;
    private final LiveData<List<PersonWithTransactions>> personsWithTransactions;
    private final MutableLiveData<ArrayList<Integer>> toolbarMenuItems;

    public TransactionListViewModel (Application application) {
        super(application);
        txnRepository = new TransactionRepository(application);
        transactions = txnRepository.getAllTransactions();
        personsWithTransactions = txnRepository.getAllPersonsWithTransactions();

        toolbarMenuItems = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<TransactionWithPerson>> getTransactions() { return transactions; }
    public LiveData<List<PersonWithTransactions>> getPersonsWithTransactions() { return personsWithTransactions; }
    public LiveData<ArrayList<Integer>> getToolbarMenuItems() { return toolbarMenuItems; }

    public void insert(Transaction transaction) { txnRepository.insert(transaction); }

    public void hideAllToolbarMenuItems() { toolbarMenuItems.setValue(new ArrayList<>()); }
    public void showToolbarMenuItem(int menuItem) { setToolbarMenuItemVisibility(menuItem, true); }
    public void hideToolbarMenuItem(int menuItem) { setToolbarMenuItemVisibility(menuItem, false); }
    private void setToolbarMenuItemVisibility(int menuItem, boolean visible) {
        ArrayList<Integer> items = toolbarMenuItems.getValue();
        if(items != null) {
            if (visible) items.add(menuItem);
            else items.remove((Integer) menuItem);
            toolbarMenuItems.setValue(items);
        }
    }
}