package org.ebur.debitum.ui;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.viewModel.PersonSumListViewModel;

import java.util.concurrent.ExecutionException;

public class PersonSumListFragment
        extends AbstractBaseListFragment <
            PersonSumListViewModel,
            PersonSumListAdapter,
            PersonSumListViewHolder,
            PersonWithTransactions> {

    @Override
    int getLayout() {
        return R.layout.fragment_person_sum_list;
    }
    @Override
    Class<PersonSumListViewModel> getViewModelClass() {
        return PersonSumListViewModel.class;
    }
    @Override
    PersonSumListAdapter getAdapter() {
        return new PersonSumListAdapter(new PersonSumListAdapter.PersonSumDiff());
    }

    @Override
    protected void addRecyclerViewDecorations() {
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        InsetDrawable divider = (InsetDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.divider_inset_start, null);
        assert divider!=null;
        divider.setAlpha(33);
        decoration.setDrawable(divider);
        recyclerView.addItemDecoration(decoration);
    }

    @Override
    protected void subscribeToViewModel() {
        viewModel.getPersonsWithTransactions().observe(getViewLifecycleOwner(), pwtList -> {
            updateTotalHeader(
                    PersonWithTransactions.getSum(pwtList)
            );
            adapter.submitList(pwtList);
            if(pwtList.isEmpty()) {
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_person_sum_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miAddPerson) {
            addPerson();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void onActionModeEdit(int idPerson) {
        editPerson(idPerson);
    }
    public void editPerson(int idPerson) {
        Person selectedPerson;
        try {
            selectedPerson = viewModel.getPersonById(idPerson);
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, String.format("person with id %d could not be found in the database", idPerson));
            return;
        }
        // navigate to EditTransactionFragment
        Bundle args = new Bundle();
        args.putParcelable(EditPersonFragment.ARG_EDITED_PERSON, selectedPerson);
        NavHostFragment.findNavController(this).navigate(R.id.action_editPerson, args);
    }

    protected void onActionModeDelete(Selection<Long> selection) {
        deletePersons(selection);
    }
    public void deletePersons(Selection<Long> selection) {
        int deleteCount = selection.size();

        // ask for confirmation
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity());
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

    public void addPerson() {
        NavHostFragment.findNavController(this).navigate(R.id.action_addPerson);//, args);
    }
}