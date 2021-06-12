package org.ebur.debitum.ui;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.ItemReturnedFilterViewModel;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// like TransactionListFragment but shows only non-monetary items
public class ItemTransactionListFragment extends TransactionListFragment {

    private ItemReturnedFilterViewModel returnedFilterViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        returnedFilterViewModel = new ViewModelProvider(this).get(ItemReturnedFilterViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void subscribeToViewModel() {
        viewModel.getItemTransactions().observe(getViewLifecycleOwner(), this::updateAdapter);
    }

    private void updateAdapter(List<TransactionWithPerson> transactions) {
        Person filterPerson = personFilterViewModel.getFilterPerson();
        List<TransactionWithPerson> listForAdapter = filter(transactions, filterPerson);
        listForAdapter = filter(listForAdapter, returnedFilterViewModel.getFilterMode());
        updateTotalHeader(TransactionWithPerson.getNumberOfItems(listForAdapter));
        adapter.submitList(listForAdapter);
        if(transactions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setupTotalHeader(@NonNull View root) {
        TextView descView = root.findViewById(R.id.header_description);
        descView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void updateTotalHeader(int total) {
        TextView totalView = requireView().findViewById(R.id.header_total);
        totalView.setText(String.format(Locale.getDefault(), "%d", total));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_transaction_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miFilterReturned) {
            returnedFilterViewModel.setFilterMode(ItemReturnedFilterViewModel.FILTER_RETURNED);
            updateAdapter(viewModel.getItemTransactions().getValue());
            TextView descView = requireView().findViewById(R.id.header_description);
            descView.setText(R.string.header_desc_items_returned);
            return true;
        } else if (id==R.id.miFilterUnreturned) {
            returnedFilterViewModel.setFilterMode(ItemReturnedFilterViewModel.FILTER_UNRETURNED);
            updateAdapter(viewModel.getItemTransactions().getValue());
            TextView descView = requireView().findViewById(R.id.header_description);
            descView.setText(R.string.header_desc_items_unreturned);
            return true;
        } else if (id==R.id.miFilterAll) {
            returnedFilterViewModel.setFilterMode(ItemReturnedFilterViewModel.FILTER_ALL);
            updateAdapter(viewModel.getItemTransactions().getValue());
            TextView descView = requireView().findViewById(R.id.header_description);
            descView.setText(R.string.header_desc_items_all);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean isActionModeReturnEnabled() {
        return true;
    }

    @Override
    protected void onActionModeReturned(int selectedId) {
        // get Transaction from viewModel/repository
        Transaction txn;
        try {
            txn = viewModel.getTransactionFromDatabase(selectedId);
        } catch (ExecutionException |InterruptedException e) {
            // TODO notify with toast
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Log.e(TAG, errorMessage);
            return;
        }
        // set returned
        txn.setReturned();
        // update via viewModel/repository
        viewModel.update(txn);
    }

    protected List<TransactionWithPerson> filter(List<TransactionWithPerson> transactions, int filterMode) {
        if (transactions == null) return null;
        else return transactions.stream()
                .filter(twp -> !twp.transaction.isReturned() && (filterMode & ItemReturnedFilterViewModel.FILTER_UNRETURNED) > 0
                        || twp.transaction.isReturned() && (filterMode & ItemReturnedFilterViewModel.FILTER_RETURNED) > 0)
                .collect(Collectors.toList());
    }
}
