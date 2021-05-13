package org.ebur.debitum.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialFadeThrough;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.PersonFilterViewModel;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class TransactionListFragment extends Fragment {

    public static final String ARG_FILTER_PERSON = "filterPerson";

    protected TransactionListViewModel viewModel;
    protected PersonFilterViewModel personFilterViewModel;
    private RecyclerView recyclerView;
    protected TransactionListAdapter adapter;
    private SelectionTracker<Long> selectionTracker = null;

    private Toolbar filterBar;

    private int nRowsSelected = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transitions
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new Hold());
        setSharedElementEnterTransition(new MaterialContainerTransform());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionListViewModel.class);
        personFilterViewModel = new ViewModelProvider(requireActivity()).get(PersonFilterViewModel.class);

        View root = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        setupFilterBar(root);

        setupTotalHeader(root);

        setupRecyclerView(root);

        buildSelectionTracker();
        subscribeToViewModel();
        setHasOptionsMenu(true);

        return root;
    }

    private void setupRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.recyclerview);
        adapter = new TransactionListAdapter(new TransactionListAdapter.TransactionDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
    }

    private void setupFilterBar(View root) {
        filterBar = root.findViewById(R.id.filter_bar);
        // set Person filter if in argument
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_FILTER_PERSON)) {
            personFilterViewModel.setFilterPerson(args.getParcelable(ARG_FILTER_PERSON));
        }

        filterBar.getMenu().findItem(R.id.miDismiss_filter).setOnMenuItemClickListener(item -> {
            dismissFilterBar();
            return true;
        });

        if(personFilterViewModel.getFilterPerson() != null) {
            filterBar.setTitle(personFilterViewModel.getFilterPerson().name);
            filterBar.setVisibility(View.VISIBLE);
        } else {
            filterBar.setVisibility(View.GONE);
        }
    }

    private void buildSelectionTracker() {
        selectionTracker = new SelectionTracker.Builder<>(
                "transactionListSelection",
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

    protected void subscribeToViewModel() {
        viewModel.getMoneyTransactions().observe(getViewLifecycleOwner(), (transactions) -> {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            List<TransactionWithPerson> listForAdapter = filter(transactions, filterPerson);
            updateTotalHeader(TransactionWithPerson.getSum(listForAdapter));
            adapter.submitList(listForAdapter);
        });
    }

    protected void setupTotalHeader(View root) {
        TextView descView = root.findViewById(R.id.header_description);
        descView.setVisibility(View.INVISIBLE);
    }
    protected void updateTotalHeader(int total) {
        TextView totalView = requireView().findViewById(R.id.header_total);
        totalView.setText(Transaction.formatMonetaryAmount(total));
        int totalColor = total>0 ? R.color.owe_green : R.color.lent_red;
        totalView.setTextColor(totalView.getResources().getColor(totalColor, null));
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

        // do not show edit person menu item when not filtered by person or when rows are selected
        if(personFilterViewModel.getFilterPerson() == null || nRowsSelected > 0)
            menu.findItem(R.id.miEditPerson).setVisible(false);

        // only show edit transaction menu item if exactly one transaction is selected
        if(nRowsSelected != 1)
            menu.findItem(R.id.miEditTransaction).setVisible(false);

        // only show delete transaction menu item if one or more items are selected
        if(nRowsSelected < 1)
            menu.findItem(R.id.miDeleteTransaction).setVisible(false);
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
            onDeleteTransactionAction();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onEditPersonAction() {
        Bundle args = new Bundle();
        args.putParcelable(EditPersonFragment.ARG_EDITED_PERSON, personFilterViewModel.getFilterPerson());
        NavHostFragment.findNavController(this).navigate(R.id.action_editPerson, args);
    }

    private void onEditTransactionAction() {
        // we can assume, that only one row is selected, as the menu item is hidden else
        if (selectionTracker.getSelection().size() == 1) {
            // get selected idTransaction (note: TransactionListAdapter.getItemId returns the
            // item's transaction id as the unique row id, so we can use that here
            int selectedId = selectionTracker.getSelection().iterator().next().intValue();

            // clear selection, as nothing shall be selected upon returning from EditTransactionFragment
            selectionTracker.clearSelection();

            // navigate to EditTransactionFragment
            Bundle args = new Bundle();
            args.putInt(EditTransactionFragment.ARG_ID_TRANSACTION, selectedId);
            NavHostFragment.findNavController(this).navigate(R.id.action_editTransaction, args);
        }
    }

    private void onDeleteTransactionAction() {
        // make copy of selection so we have a constant list of selected items, even during
        // iteratively deleting items
        MutableSelection<Long> selection = new MutableSelection<>();
        selectionTracker.copySelection(selection);

        int deleteCount = selection.size();

        // ask for confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setPositiveButton(R.string.delete_dialog_confirm, (dialog, id) -> {
            for (Long idTransaction : selection) {
                // Room uses the primary key (idTransaction) to find the row to be deleted, so an
                // empty Transaction with the correct id will suffice
                viewModel.delete(new Transaction(idTransaction.intValue()));
            }
            selectionTracker.clearSelection();
            Snackbar.make(requireView(),
                    getResources().getQuantityString(R.plurals.transaction_list_snackbar_deleted, deleteCount, deleteCount),
                    Snackbar.LENGTH_LONG)
                    .show();
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> dialog.cancel());
        builder.setMessage(R.string.transaction_list_dialog_delete_text)
                .setTitle(getResources().getQuantityString(R.plurals.transaction_list_dialog_delete_title, deleteCount, deleteCount));
        AlertDialog dialog = builder.create();

        dialog.show();
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
        // IMPORTANT: return a copy of the passed list, as we add a header item afterwards and do
        // not want it to be added to the viewModel's original list, too (instead just to the
        // adapter's copy of the list)
        if (filterPerson == null) return new ArrayList<>(transactions);
        // http://javatricks.de/tricks/liste-filtern
        else return transactions.stream()
                .filter(twp -> twp.person.equals(filterPerson))
                .collect(Collectors.toList());
    }

    // -------------
    // Person filter
    // -------------

    private void dismissFilterBar() {
        personFilterViewModel.setFilterPerson(null);
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());

        NavController nav = NavHostFragment.findNavController(this);
        if(pref.getBoolean(SettingsFragment.PREF_KEY_DISMISS_FILTER_BEHAVIOUR, false)) {
            nav.navigate(R.id.people_dest);
        } else {
            // replace curremt framgent with a new one of the same class
            // (then unfiltered, as the viewModel's filterPerson was nulled)
            NavDestination current = nav.getCurrentDestination();
            if (current != null)
                nav.navigate(current.getId());
        }

        filterBar.setVisibility(View.GONE);
    }
}