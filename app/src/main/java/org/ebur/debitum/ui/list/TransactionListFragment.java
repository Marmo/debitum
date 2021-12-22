package org.ebur.debitum.ui.list;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialContainerTransform;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.ui.EditPersonFragment;
import org.ebur.debitum.ui.SettingsFragment;
import org.ebur.debitum.ui.edit_transaction.EditTransactionFragment;
import org.ebur.debitum.util.Utilities;
import org.ebur.debitum.viewModel.PersonFilterViewModel;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TransactionListFragment
        extends AbstractBaseListFragment <
            TransactionListViewModel,
            TransactionListAdapter,
            TransactionListViewHolder,
            TransactionWithPerson> {

    private final static String TAG = "TransactionListFragment";

    public static final String ARG_FILTER_PERSON = "filterPerson";
    protected PersonFilterViewModel personFilterViewModel;
    private Toolbar filterBar;

    @Override
    @LayoutRes
    int getLayout() {
        return R.layout.fragment_transaction_list;
    }
    @Override
    Class<TransactionListViewModel> getViewModelClass() {
        return TransactionListViewModel.class;
    }
    @Override
    TransactionListAdapter getAdapter() {
        return new TransactionListAdapter(new TransactionListAdapter.TransactionDiff());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialContainerTransform sharedElementTransition = new MaterialContainerTransform();
        sharedElementTransition.setDuration(300);
        sharedElementTransition.setDrawingViewId(R.id.nav_host_fragment);
        //sharedElementTransition.setScrimColor(Color.TRANSPARENT);
        sharedElementTransition.setFadeMode(MaterialContainerTransform.FADE_MODE_THROUGH);
        setSharedElementEnterTransition(sharedElementTransition);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        personFilterViewModel = new ViewModelProvider(requireActivity()).get(PersonFilterViewModel.class);
        View root = super.onCreateView(inflater, container, savedInstanceState); assert root != null;
        setupFilterBar(root);
        return root;
    }

    private void setupFilterBar(@NonNull View root) {
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

        filterBar.getMenu().findItem(R.id.miEditPerson).setOnMenuItemClickListener(item -> {
            editPerson(personFilterViewModel.getFilterPerson());
            return true;
        });

        if(personFilterViewModel.getFilterPerson() != null) {
            filterBar.setTitle(personFilterViewModel.getFilterPerson().name);
            filterBar.setSubtitle(personFilterViewModel.getFilterPerson().note);
            filterBar.setVisibility(View.VISIBLE);
        } else {
            filterBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setupRecyclerView(@NonNull View root) {
        super.setupRecyclerView(root);
        // needed to scroll down when a transaction is created (else it will be hidden behind
        // the totals header (#2)
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.scrollToPosition(positionStart);
            }
        });
    }

    @Override
    protected void addRecyclerViewDecorations() {
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void subscribeToViewModel() {
        viewModel.getMoneyTransactions().observe(getViewLifecycleOwner(), transactions -> {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            List<TransactionWithPerson> listForAdapter = filter(transactions, filterPerson);
            updateTotalHeader(TransactionWithPerson.getSum(listForAdapter));
            adapter.submitList(listForAdapter);

            // show or hide empty-screen
            boolean empty = transactions.isEmpty();
            recyclerView.setVisibility(empty?View.GONE:View.VISIBLE);
            emptyView.setVisibility(empty?View.VISIBLE:View.GONE);
        });
    }

    // ---------------------------
    // Toolbar Menu event handling
    // ---------------------------

    public void editPerson(Person person) {
        Bundle args = new Bundle();
        args.putParcelable(EditPersonFragment.ARG_EDITED_PERSON, person);
        NavHostFragment.findNavController(this).navigate(R.id.action_global_editPerson, args);
    }

    // -----------
    // Action Mode
    // -----------

    @Override
    protected boolean prepareActionMode(ActionMode mode, Menu menu) {
        super.prepareActionMode(mode, menu);
        int nRowsSelected = selectionTracker.getSelection().size();
        // only show returned shortcut when exactly one item is selected
        menu.findItem(R.id.miReturned).setVisible(nRowsSelected == 1);

        // show sum in subtitle if more than one transaction is selected
        if(nRowsSelected > 1) {
            int sum = 0;
            try {
                for (long id : selectionTracker.getSelection()) {
                    sum += viewModel.getTransactionFromDatabase((int) id).amount;
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
            mode.setSubtitle(getResources().getString(
                    R.string.actionmode_sum,
                    Transaction.formatMonetaryAmount(sum, Utilities.getNrOfDecimals(requireContext()))
            ));
        } else {
            mode.setSubtitle(null);
        }
        return true;
    }

    @Override
    protected void onActionModeEdit(int idTransaction) {
        editTransaction(idTransaction);
    }
    private void editTransaction(int idTransaction) {
        Bundle args = new Bundle();
        args.putInt(EditTransactionFragment.ARG_ID_TRANSACTION, idTransaction);
        NavHostFragment.findNavController(this).navigate(R.id.action_global_editTransaction, args);
    }

    @Override
    protected void onActionModeDelete(Selection<Long> selection) {
        deleteTransactions(selection);
    }
    private void deleteTransactions(Selection<Long> selection) {
        int deleteCount = selection.size();

        // ask for confirmation
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setPositiveButton(R.string.delete_dialog_confirm, (dialog, id) -> {
            for (Long idTransaction : selection) {
                // Room uses the primary key (idTransaction) to find the row to be deleted, so an
                // empty Transaction with the correct id will suffice
                viewModel.delete(new Transaction(idTransaction.intValue()));
            }
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

    @Override
    protected void onActionModeReturned(int selectedId) {
        try {
            Transaction txn = viewModel.getTransactionFromDatabase(selectedId);
            Bundle args = new Bundle();
            args.putInt(EditTransactionFragment.ARG_PRESET_AMOUNT, -txn.amount);
            args.putString(
                    EditTransactionFragment.ARG_PRESET_DESCRIPTION,
                    getString(R.string.debt_settlement_money_description,
                            txn.description,
                            Utilities.formatDate(txn.timestamp, requireContext())
                    )
            );
            args.putString(EditTransactionFragment.ARG_PRESET_NAME, viewModel.getPersonFromDatabase(txn.idPerson).name);
            NavHostFragment.findNavController(this).navigate(R.id.action_global_editTransaction, args);
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // -------------
    // Person filter
    // -------------

    protected List<TransactionWithPerson> filter(List<TransactionWithPerson> transactions, @Nullable Person filterPerson) {
        if (transactions == null) return null;
        if (filterPerson == null) return new ArrayList<>(transactions);
            // http://javatricks.de/tricks/liste-filtern
        else return transactions.stream()
                .filter(twp -> twp.person.equals(filterPerson))
                .collect(Collectors.toList());
    }

    private void dismissFilterBar() {
        personFilterViewModel.setFilterPerson(null);
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());

        NavController nav = NavHostFragment.findNavController(this);
        if(pref.getBoolean(SettingsFragment.PREF_KEY_DISMISS_FILTER_BEHAVIOUR, false)) {
            nav.navigate(R.id.people_dest);
        } else {
            // replace current fragment with a new one of the same class
            // (then unfiltered, as the viewModel's filterPerson was nulled)
            NavDestination current = nav.getCurrentDestination();
            if (current != null)
                nav.navigate(current.getId());
        }

        filterBar.setVisibility(View.GONE);
    }

    //----------------------------------------------------------
    // generate presets for MainActivity::onAddTransactionAction
    //----------------------------------------------------------

    @Override
    @Nullable
    public Bundle getPresetsFromSelection() {
        if (selectionTracker.getSelection().size() != 1) {
            return null;
        } else {
            Bundle presets = new Bundle();
            int selectedId = selectionTracker.getSelection().iterator().next().intValue();
            selectionTracker.clearSelection(); // nothing should be selected when returning from EditTransactionFragment
            Transaction transaction;
            Person person;
            try {
                transaction = viewModel.getTransactionFromDatabase(selectedId);
                person = viewModel.getPersonFromDatabase(transaction.idPerson);
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, String.format("transaction with id %d (or its person) could not be found in the database", selectedId));
                return null;
            }
            presets.putString(EditTransactionFragment.ARG_PRESET_NAME, person.name);
            presets.putInt(EditTransactionFragment.ARG_PRESET_AMOUNT, transaction.amount);
            presets.putInt(EditTransactionFragment.ARG_PRESET_TYPE,
                    transaction.isMonetary?Transaction.TYPE_MONEY:Transaction.TYPE_ITEM);
            presets.putString(EditTransactionFragment.ARG_PRESET_DESCRIPTION, transaction.description);
            presets.putLong(EditTransactionFragment.ARG_PRESET_DATE, transaction.timestamp.getTime());
            if (!transaction.isMonetary) {
                presets.putLong(EditTransactionFragment.ARG_PRESET_RETURNDATE, transaction.timestampReturned.getTime());
            }
            return presets;
        }
    }
}