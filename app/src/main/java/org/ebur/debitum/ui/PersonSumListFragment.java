package org.ebur.debitum.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.PersonSumListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// TODO add Activity to show all transactions of one person that is launched when clicking on one row
// TODO in PersonTransactionListActivity add ActionBar options to edit/delete person
public class PersonSumListFragment extends Fragment {

    private static final String TAG = "PersonSumListFragment";

    private PersonSumListViewModel viewModel;
    private RecyclerView recyclerView;
    private PersonSumListAdapter adapter;
    private SelectionTracker<Long> selectionTracker = null;

    private int nRowsSelected = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(PersonSumListViewModel.class);

        View root = inflater.inflate(R.layout.fragment_person_sum_list, container, false);

        recyclerView = root.findViewById(R.id.person_sum_list_recyclerview);
        adapter = new PersonSumListAdapter(new PersonSumListAdapter.PersonSumDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        buildSelectionTracker();

        // observe ViewModel's LiveData
        viewModel.getPersonsWithTransactions().observe(getViewLifecycleOwner(), pwtList -> {
            // we need to make a copy so that the header is not added to the original list (again and again and again ...)
            List<PersonWithTransactions> listForAdapter = new ArrayList<>(pwtList);
            listForAdapter.add(0, buildTotalHeader(
                    PersonWithTransactions.getSum(listForAdapter),
                    PersonWithTransactions.getNumberOfItems(listForAdapter)
            ));
            adapter.submitList(listForAdapter);
        });

        setHasOptionsMenu(true);

        return root;
    }

    // create a PersonWithTransactions containing the header information for the RecyclerView
    private PersonWithTransactions buildTotalHeader(int totalMoney, int totalItems) {
        Transaction headerMoneyTxn = new Transaction(Integer.MIN_VALUE);
        Transaction headerItemTxn = new Transaction(Integer.MIN_VALUE+1);
        headerMoneyTxn.amount = totalMoney;
        headerItemTxn.amount = totalItems;
        headerMoneyTxn.isMonetary = true;
        headerItemTxn.isMonetary = false;
        return new PersonWithTransactions(new Person("PONDER STIBBONS"), headerMoneyTxn, headerItemTxn);
    }

    private void buildSelectionTracker() {
        selectionTracker = new SelectionTracker.Builder<>(
                "personSumListSelection",
                recyclerView,
                new ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {
                    @Override
                    public Long getKey(int position) {
                        return adapter.getItemId(position);
                    }

                    @Override
                    public int getPosition(@NonNull Long key) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForItemId(key);
                        return viewHolder == null ? RecyclerView.NO_POSITION : viewHolder.getLayoutPosition();
                    }
                },
                new ListItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build();

        // change visible menu items depending on item selection
        this.selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                invalidateMenuIfNeeded(selectionTracker.getSelection().size());
            }
        });
        adapter.setSelectionTracker(this.selectionTracker);
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
        inflater.inflate(R.menu.menu_person_sum_list, menu);

        // only show add person menu item when nothing is selected
        if(nRowsSelected > 0) menu.findItem(R.id.miAddPerson).setVisible(false);

        // only show edit transaction menu item if exactly one transaction is selected
        if(nRowsSelected != 1) menu.findItem(R.id.miEditPerson).setVisible(false);

        // only show delete transaction menu item if one or more items are selected
        if(nRowsSelected == 0) menu.findItem(R.id.miDeletePerson).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miAddPerson) {
            onAddPersonAction();
            return true;
        } else if(id==R.id.miEditPerson) {
            onEditPersonAction();
            return true;
        } else if(id==R.id.miDeletePerson) {
            onDeletePersonAction();
            return true;
        }
        return true;
    }

    public void onEditPersonAction() {
        // we can assume, that only one row is selected, as the menu item is hidden else
        if (selectionTracker.getSelection().size() == 1) {
            // get selected idPerson (note: PersonSumListAdapter.getItemId returns the
            // item's person id as the unique row id, so we can use that here
            int selectedId = selectionTracker.getSelection().iterator().next().intValue();
            Person selectedPerson;
            try {
                selectedPerson = viewModel.getPersonById(selectedId);
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, String.format("person with id %d could not be found in the database", selectedId));
                return;
            }

            // clear selection, as nothing shall be selected upon returning from EditPersonFragment
            selectionTracker.clearSelection();

            // navigate to EditTransactionFragment
            Bundle args = new Bundle();
            args.putParcelable(EditPersonFragment.ARG_EDITED_PERSON, selectedPerson);
            NavHostFragment.findNavController(this).navigate(R.id.action_editPerson, args);
        }
    }

    public void onDeletePersonAction() {
        // make copy of selection so we have a constant list of selected items, even during
        // iteratively deleting items
        MutableSelection<Long> selection = new MutableSelection<>();
        selectionTracker.copySelection(selection);

        int deleteCount = selection.size();

        // ask for confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setPositiveButton(R.string.delete_dialog_confirm, (dialog, id) -> {
            for (Long idPerson : selection) {
                // Room uses the primary key (idPerson) to find the row to be deleted, so an
                // empty Person with the correct id will suffice
                viewModel.delete(new Person(idPerson.intValue()));
            }
            selectionTracker.clearSelection();
            Snackbar.make(requireView(),
                    getResources().getQuantityString(R.plurals.person_sum_list_snackbar_deleted, deleteCount, deleteCount),
                    Snackbar.LENGTH_LONG)
                    .show();
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> dialog.cancel());
        builder.setMessage(R.string.person_sum_list_dialog_delete_text)
                .setTitle(getResources().getQuantityString(R.plurals.person_sum_list_dialog_delete_title, deleteCount, deleteCount));
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    public void onAddPersonAction() {
        Bundle args = new Bundle();
        args.putParcelable(EditPersonFragment.ARG_EDITED_PERSON, null);
        NavHostFragment.findNavController(this).navigate(R.id.action_editPerson, args);
    }

    private void invalidateMenuIfNeeded(int nRowsSelectedNew) {
        // rebuild options menu if relevant change in selected item number occured
        if ( nRowsSelectedNew != nRowsSelected
                && (nRowsSelectedNew <= 1 || nRowsSelected <= 1)) {
            requireActivity().invalidateOptionsMenu();
        }
        nRowsSelected = nRowsSelectedNew;
    }
}