package org.ebur.debitum.ui;


import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.TransactionWithPerson;

import java.util.List;

// like TransactionListFragment but shows only non-monetary items
public class ItemTransactionListFragment extends TransactionListFragment {

    @Override
    protected void subscribeToViewModel() {
        viewModel.getItemTransactions().observe(getViewLifecycleOwner(), (transactions) -> {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            List<TransactionWithPerson> listForAdapter = filter(transactions, filterPerson);
            listForAdapter.add(0, buildTotalHeader(TransactionWithPerson.getNumberOfItems(listForAdapter)));
            adapter.submitList(listForAdapter);
        });
    }

    @Override
    protected TransactionWithPerson buildTotalHeader(int total) {
        TransactionWithPerson header = super.buildTotalHeader(total);
        header.transaction.isMonetary = false; // used to indicate correct number formatting in HeaderViewHolder
        return header;
    }
}
