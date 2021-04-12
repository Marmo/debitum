package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.Date;

import static android.app.Activity.RESULT_OK;

// TODO make list items selectable to delete them (via ActionBar-Button)
// TODO make list items selactable to edit them (fab or ActionBar-Button)

public class TransactionListFragment extends Fragment {

    // fragment initialization parameters
    private static final String ARG_FILTER_BY_NAME = "filterBy_Name";
    private static final String ARG_FILTER_BY_ID = "filterBy_Id";

    private TransactionListViewModel viewModel;
    private SelectionTracker<Long> selectionTracker = null;

    public static TransactionListFragment newInstance() {
        return newInstance(null);
    }
    public static TransactionListFragment newInstance(Person filterBy) {
        TransactionListFragment fragment = new TransactionListFragment();
        Bundle args = new Bundle();
        if (filterBy != null){
            args.putString(ARG_FILTER_BY_NAME, filterBy.name);
            args.putInt(ARG_FILTER_BY_ID, filterBy.idPerson);
        } else { // no filter applied
            args.putString(ARG_FILTER_BY_NAME, "");
            args.putInt(ARG_FILTER_BY_ID, -1);
        }
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
                setEditDeleteMenuItemVisibility(selectionTracker.getSelection().size());
            }
        });
        adapter.setSelectionTracker(this.selectionTracker);


        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionListViewModel.class);
        // Update the transactions in the [recyclerView] via [adapter].
        viewModel.getTransactions().observe(getViewLifecycleOwner(), adapter::submitList);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.hideToolbarMenuItem(R.id.miAddPerson);
        // show edit/delete transaction buttons based on number of selected items
        setEditDeleteMenuItemVisibility(selectionTracker.getSelection().size());
        if(getArguments().getInt(ARG_FILTER_BY_ID)>=0) { // filtered by person
            viewModel.showToolbarMenuItem(R.id.miEditPerson);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.hideToolbarMenuItem(R.id.miDeleteTransaction);
        viewModel.hideToolbarMenuItem(R.id.miEditTransaction);
        viewModel.hideToolbarMenuItem(R.id.miEditPerson);
    }

    private void setEditDeleteMenuItemVisibility(int nSelectedRows) {
        if (nSelectedRows == 0) {
            // remove delete and edit from MainActivity's toolbar
            viewModel.hideToolbarMenuItem(R.id.miDeleteTransaction);
            viewModel.hideToolbarMenuItem(R.id.miEditTransaction);
        }
        else if (nSelectedRows == 1) {
            // add delete and edit to MainActivity's toolbar
            viewModel.showToolbarMenuItem(R.id.miDeleteTransaction);
            viewModel.showToolbarMenuItem(R.id.miEditTransaction);
        }
        else if (nSelectedRows>1) {
            // add delete and remove edit from MainActivity's toolbar
            viewModel.showToolbarMenuItem(R.id.miDeleteTransaction);
            viewModel.hideToolbarMenuItem(R.id.miEditTransaction);
        }
    }
}