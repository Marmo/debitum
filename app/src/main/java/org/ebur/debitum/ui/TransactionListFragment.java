package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

// TODO make list items selectable to delete them (via ActionBar-Button)
// TODO make list items selactable to edit them (fab or ActionBar-Button)

public class TransactionListFragment extends Fragment {

    // fragment initialization parameters
    public static final String ARG_FILTER_BY = "filterBy";

    private TransactionListViewModel viewModel;
    private SelectionTracker<Long> selectionTracker = null;

    private int nRowsSelected = 0;
    @Nullable
    private Person filterBy;

    public static TransactionListFragment newInstance() {
        return newInstance(null);
    }
    public static TransactionListFragment newInstance(@Nullable Person filterBy) {
        TransactionListFragment fragment = new TransactionListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER_BY, filterBy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);

        // setup adapter
        final TransactionListAdapter adapter = new TransactionListAdapter(new TransactionListAdapter.TransactionDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // set Person filter
        filterBy = requireArguments().getParcelable(ARG_FILTER_BY);

        // build selectionTracker
        this.selectionTracker = new SelectionTracker.Builder<Long>(
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


        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionListViewModel.class);
        viewModel.getTransactions().observe(getViewLifecycleOwner(), (transactions) -> {
            adapter.submitList(filter(transactions, filterBy));
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // show edit/delete transaction buttons based on number of selected items
        invalidateMenuIfNeeded(selectionTracker.getSelection().size());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_list, menu);

        // only show edit person menu item when filtered by person
        if(requireArguments().getParcelable(ARG_FILTER_BY) == null) {
            menu.findItem(R.id.miEditPerson).setVisible(false);
        }

        // only show edit transaction menu item if exactly one transaction is selected
        if(nRowsSelected != 1) menu.findItem(R.id.miEditTransaction).setVisible(false);

        // only show delete transaction menu item if one or more items are selected
        if(nRowsSelected < 1) menu.findItem(R.id.miDeleteTransaction).setVisible(false);
    }

    private void invalidateMenuIfNeeded(int nRowsSelectedNew) {
        // rebuild options menu if relevant change in selected item number occured
        if ( nRowsSelectedNew != nRowsSelected
                && (nRowsSelectedNew <= 1 || nRowsSelected <= 1)) {
            requireActivity().invalidateOptionsMenu();
        }
        nRowsSelected = nRowsSelectedNew;
    }


    private List<TransactionWithPerson> filter(List<TransactionWithPerson> transactions, @Nullable Person filterBy) {
        if (filterBy == null) return transactions;
        // http://javatricks.de/tricks/liste-filtern
        else return transactions.stream()
                .filter(twp -> twp.person.equals(filterBy))
                .collect(Collectors.toList());
    }
}