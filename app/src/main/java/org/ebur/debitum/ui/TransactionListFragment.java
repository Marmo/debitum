package org.ebur.debitum.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialContainerTransform;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.PersonFilterViewModel;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionListFragment
        extends AbstractBaseListFragment <
            TransactionListViewModel,
            TransactionListAdapter,
            TransactionListViewHolder,
            TransactionWithPerson> {

    public static final String ARG_FILTER_PERSON = "filterPerson";
    protected PersonFilterViewModel personFilterViewModel;
    private Toolbar filterBar;

    @Override
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
        sharedElementTransition.setDuration(500);
        sharedElementTransition.setDrawingViewId(R.id.nav_host_fragment);
        sharedElementTransition.setScrimColor(Color.TRANSPARENT);
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
            if(transactions.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
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
    protected void onActionModeReturned(int selectedId) {}

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
}