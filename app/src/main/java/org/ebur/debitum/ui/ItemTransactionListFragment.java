package org.ebur.debitum.ui;


import org.ebur.debitum.database.Person;

// like TransactionListFragment but shows only non-monetary items
public class ItemTransactionListFragment extends TransactionListFragment {

    @Override
    protected void observeTransactionsLiveData() {
        viewModel.getItemTransactions().observe(getViewLifecycleOwner(), (transactions) -> {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            adapter.submitList(filter(transactions, filterPerson));
        });
    }
}
