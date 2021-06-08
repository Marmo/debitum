package org.ebur.debitum.ui;


import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.Selection;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

// like TransactionListFragment but shows only non-monetary items
public class ItemTransactionListFragment extends TransactionListFragment {

    @Override
    protected void subscribeToViewModel() {
        viewModel.getItemTransactions().observe(getViewLifecycleOwner(), transactions -> {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            List<TransactionWithPerson> listForAdapter = filter(transactions, filterPerson);
            updateTotalHeader(TransactionWithPerson.getNumberOfItems(listForAdapter));
            adapter.submitList(listForAdapter);
            if(transactions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });
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
}
