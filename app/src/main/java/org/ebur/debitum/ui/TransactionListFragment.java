package org.ebur.debitum.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.PersonFilterViewModel;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.List;
import java.util.stream.Collectors;


public class TransactionListFragment extends Fragment {

    public static final String ARG_FILTER_PERSON = "filterPerson";

    protected TransactionListViewModel viewModel;
    protected PersonFilterViewModel personFilterViewModel;
    private NavController nav;
    protected TransactionListAdapter adapter;
    private SelectionTracker<Long> selectionTracker = null;

    private int nRowsSelected = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionListViewModel.class);
        personFilterViewModel = new ViewModelProvider(requireActivity()).get(PersonFilterViewModel.class);
        nav = NavHostFragment.findNavController(this);

        View root = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);

        // setup adapter
        adapter = new TransactionListAdapter(new TransactionListAdapter.TransactionDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // set Person filter if in argument
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_FILTER_PERSON)) {
            personFilterViewModel.setFilterPerson(args.getParcelable(ARG_FILTER_PERSON));
        }

        // build selectionTracker
        this.selectionTracker = new SelectionTracker.Builder<>(
                "transactionListSelection",
                recyclerView,
                new StableIdKeyProvider(recyclerView),
                new ListItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
        ).build();

        this.selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                invalidateMenuIfNeeded(selectionTracker.getSelection().size());
            }
        });
        adapter.setSelectionTracker(this.selectionTracker);

        observeTransactionsLiveData();

        setHasOptionsMenu(true);

        return root;
    }

    protected void observeTransactionsLiveData() {
        viewModel.getMoneyTransactions().observe(getViewLifecycleOwner(), (transactions) -> {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            adapter.submitList(filter(transactions, filterPerson));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // show edit/delete transaction buttons based on number of selected items
        invalidateMenuIfNeeded(selectionTracker.getSelection().size());
    }

    // ---------------------------
    // Toolbar Menu event handling
    // ---------------------------

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_list, menu);

        // do not show edit person menu item when not filtered by person
        if(personFilterViewModel.getFilterPerson() == null) {
            menu.findItem(R.id.miEditPerson).setVisible(false);
        }

        // only show edit transaction menu item if exactly one transaction is selected
        if(nRowsSelected != 1) menu.findItem(R.id.miEditTransaction).setVisible(false);

        // only show delete transaction menu item if one or more items are selected
        if(nRowsSelected < 1) menu.findItem(R.id.miDeleteTransaction).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miEditPerson) {
            onEditPersonAction();
            return true;
        } else if(id==R.id.miEditTransaction) {
            onEditTransactionAction();
            return true;
        } else if(id==R.id.miDeleteTransaction) {
            onDeleteTransaction();
            return true;
        } else {
            return NavigationUI.onNavDestinationSelected(item, nav)
                    || super.onOptionsItemSelected(item);
        }
    }

    public void onEditPersonAction() {
        Bundle args = new Bundle();
        args.putParcelable(EditPersonFragment.ARG_EDITED_PERSON, personFilterViewModel.getFilterPerson());
        nav.navigate(R.id.action_transactionList_to_editPerson, args);
    }

    private void onEditTransactionAction() {
        // we can assume, that only one row is selected, as the menu item is hidden else
        if (selectionTracker.getSelection().size() == 1) {
            // clear selection, as nothing shall be selected upon returning from EditTransactionFragment
            selectionTracker.clearSelection();

            // get selected idTransaction (note: TransactionListAdapter.getItemId returns the
            // item's transaction id as the unique row id, so we can use that here
            int selectedId = selectionTracker.getSelection().iterator().next().intValue();

            // start EditTransactionFragment
            Bundle args = new Bundle();
            args.putInt(EditTransactionFragment.ARG_ID_TRANSACTION, selectedId);
            nav.navigate(R.id.action_transactionList_to_editTransaction);
        }
    }

    private void onDeleteTransaction() {
        // TODO ask for confirmation OR: show snackbar afterwards with undo button
        // make copy of selection so we have a constant list of selected items
        MutableSelection<Long> selection = new MutableSelection<>();
        selectionTracker.copySelection(selection);

        for (Long idTransaction : selection) {
            // we just need a Transaction with the correct id for deletion, so we create a dummy one
            viewModel.delete(new Transaction(idTransaction.intValue()));
        }
        selectionTracker.clearSelection();
        // TODO: show snackbar with success message including number of deleted transactions
    }

    private void invalidateMenuIfNeeded(int nRowsSelectedNew) {
        // rebuild options menu if relevant change in selected item number occured
        if ( nRowsSelectedNew != nRowsSelected
                && (nRowsSelectedNew <= 1 || nRowsSelected <= 1)) {
            requireActivity().invalidateOptionsMenu();
        }
        nRowsSelected = nRowsSelectedNew;
    }

    protected List<TransactionWithPerson> filter(List<TransactionWithPerson> transactions, @Nullable Person filterPerson) {
        if (transactions == null) return null;
        if (filterPerson == null) return transactions;
        // http://javatricks.de/tricks/liste-filtern
        else return transactions.stream()
                .filter(twp -> twp.person.equals(filterPerson))
                .collect(Collectors.toList());
    }
}