package org.ebur.debitum.ui.list;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.ui.EditPersonFragment;
import org.ebur.debitum.ui.edit_transaction.EditTransactionFragment;
import org.ebur.debitum.util.ColorUtils;
import org.ebur.debitum.util.Utilities;
import org.ebur.debitum.viewModel.ContactsHelper;
import org.ebur.debitum.viewModel.ListOrderViewModel;
import org.ebur.debitum.viewModel.PersonSumListViewModel;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PersonSumListFragment
        extends AbstractBaseListFragment <
            PersonSumListViewModel,
            PersonSumListAdapter,
            PersonSumListViewHolder,
            PersonSumListAdapter.PersonWithAvatar> {


    private ListOrderViewModel orderViewModel;
    private ContactsHelper contactsHelper;
    boolean contactLinkingEnabled;
    private Menu menu;

    @Override
    @LayoutRes
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contactsHelper = new ViewModelProvider(requireActivity()).get(ContactsHelper.class);
        // scoped to activity to make setting persistent across screens
        orderViewModel = new ViewModelProvider(requireActivity()).get(ListOrderViewModel.class);
        checkReadContactsPermission();
        return super.onCreateView(inflater, container, savedInstanceState);
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
        contactsHelper.isContactLinkingEnabled().observe(getViewLifecycleOwner(), enabled -> {
            contactLinkingEnabled = enabled;
            updateRecyclerView(viewModel.getPersonsWithTransactions().getValue());
        });

        viewModel.getPersonsWithTransactions().observe(getViewLifecycleOwner(), this::updateRecyclerView);
        orderViewModel.getOrder().observe(getViewLifecycleOwner(), order -> {
            setOrderRadioButtonsCheckedStatus(order);
            updateRecyclerView(viewModel.getPersonsWithTransactions().getValue());
        });
    }

    private void updateRecyclerView(@Nullable List<PersonWithTransactions> pwtList) {
        if(pwtList == null) return;

        updateTotalHeader(PersonWithTransactions.getSum(pwtList));
        @ColorInt int secondaryColorRGB = ColorUtils.getAttributeColor(requireContext(), R.attr.colorSecondary);

        // prepare sorting
            int by = orderViewModel.getOrderBy();
        boolean asc = orderViewModel.isOrderAscending();
        Comparator<PersonWithTransactions> comparator;
        switch (by) {
            case ListOrderViewModel.ORDER_NAME:
                comparator = Comparator.comparing(PersonWithTransactions::getName);
                break;
            case ListOrderViewModel.ORDER_DATE:
                comparator = Comparator.comparing(PersonWithTransactions::getLastTxnTimestamp);
                break;
            case ListOrderViewModel.ORDER_AMOUNT:
                comparator = Comparator.comparing(PersonWithTransactions::getSum);
                break;
            default:
                comparator = Comparator.comparing(PersonWithTransactions::getLastTxnTimestamp);
        }

        // create PersonWithAvatar instance for every PersonWithTransactions
        // and apply ordering
        // TODO would be great to determine, which avatars need to be recalculated and only submit those
        adapter.submitList(
                pwtList.stream()
                        .sorted(asc ? comparator : comparator.reversed())
                        .map(pwt -> new PersonSumListAdapter.PersonWithAvatar(
                        pwt,
                        contactsHelper.makeAvatarDrawable(
                                contactLinkingEnabled ? contactsHelper.getContactImage(pwt.person.linkedContactUri) : null,
                                pwt.person.getColor(secondaryColorRGB)
                        )
                )).collect(Collectors.toList()));


        // show or hide empty-screen
        boolean empty = pwtList.isEmpty();
        recyclerView.setVisibility(empty?View.GONE:View.VISIBLE);
        emptyView.setVisibility(empty?View.VISIBLE:View.GONE);
    }

    // ---------------------------
    // Toolbar Menu event handling
    // ---------------------------

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_person_sum_list, menu);
        this.menu = menu;
        Integer order = orderViewModel.getOrder().getValue();
        setOrderRadioButtonsCheckedStatus(order != null ? order : 0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.miAddPerson) {
            addPerson();
            return true;
        } else if (id==R.id.miOrderByNameAsc) {
            orderViewModel.setOrder(ListOrderViewModel.ORDER_NAME, true);
            return true;
        } else if (id==R.id.miOrderByNameDesc) {
            orderViewModel.setOrder(ListOrderViewModel.ORDER_NAME, false);
            return true;
        } else if (id==R.id.miOrderByDateAsc) {
            orderViewModel.setOrder(ListOrderViewModel.ORDER_DATE, true);
            return true;
        } else if (id==R.id.miOrderByDateDesc) {
            orderViewModel.setOrder(ListOrderViewModel.ORDER_DATE, false);
            return true;
        } else if (id==R.id.miOrderByAmntAsc) {
            orderViewModel.setOrder(ListOrderViewModel.ORDER_AMOUNT, true);
            return true;
        } else if (id==R.id.miOrderByAmntDesc) {
            orderViewModel.setOrder(ListOrderViewModel.ORDER_AMOUNT, false);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setOrderRadioButtonsCheckedStatus(int order) {
        if (menu != null) {
            // CAUTION: if android:checkableBehavior="single"
            // (MenuItemImpl.mFlags & MenuItemImpl.EXCLUSIVE != 0), setChecked does not care about
            // the value passed but sets the item for that setChecked is called as checked and the
            // others in the group unchecked!
            @IdRes int menuItemResId;
            boolean asc = orderViewModel.isOrderAscending();
            int by = orderViewModel.getOrderBy();
            switch (by) {
                case ListOrderViewModel.ORDER_NAME:
                    menuItemResId = asc ? R.id.miOrderByNameAsc : R.id.miOrderByNameDesc;
                    break;
                case ListOrderViewModel.ORDER_DATE:
                    menuItemResId = asc ? R.id.miOrderByDateAsc : R.id.miOrderByDateDesc;
                    break;
                case ListOrderViewModel.ORDER_AMOUNT:
                    menuItemResId = asc ? R.id.miOrderByAmntAsc : R.id.miOrderByAmntDesc;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown order by value: "+by);
            }
            // could also pass false here, as setChecked does not care in exclusive mode!
            MenuItem item = menu.findItem(menuItemResId);
            // checking for null, because when coming back to this activity by pressing the system
            // back button, onCreateOptionsMenu will be called only after the first call of
            // setOrderRadioButtonsCheckedStatus and thus item is still null then (would cause crash)
            if (item!=null) item.setChecked(true);
        }
    }

    // -----------
    // Action Mode
    // ----------


    @Override
    protected boolean prepareActionMode(ActionMode mode, Menu menu) {
        super.prepareActionMode(mode, menu);

        // show sum in subtitle if more than one person is selected
        if(selectionTracker.getSelection().size() > 1) {
            int sum = adapter.getCurrentList()
                    .stream()
                    .filter(personWithAvatar ->
                            selectionTracker.getSelection().contains((long) personWithAvatar.pwt.person.idPerson)
                    )
                    .mapToInt(
                            personWithAvatar -> Transaction.getSum(personWithAvatar.pwt.transactions))
                    .sum();
            mode.setSubtitle(getResources().getString(
                    R.string.actionmode_sum,
                    Transaction.formatMonetaryAmount(sum, Utilities.getNrOfDecimals(requireContext()))
            ));
        } else {
            mode.setSubtitle(null);
        }
        return true;
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
        NavHostFragment.findNavController(this).navigate(R.id.action_global_editPerson, args);
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

    @Override
    protected void onActionModeReturned(int selectedId) {}

    public void addPerson() {
        NavHostFragment.findNavController(this).navigate(R.id.action_addPerson);//, args);
    }

    private void checkReadContactsPermission() {
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher, as an instance variable.
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> contactsHelper.setContactLinkingEnabled(isGranted));

        // check and ask for permission
        contactsHelper.checkReadContactsPermission(requestPermissionLauncher);
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
            Person selectedPerson;
            try {
                selectedPerson = viewModel.getPersonById(selectedId);
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, String.format("person with id %d could not be found in the database", selectedId));
                return null;
            }
            presets.putString(EditTransactionFragment.ARG_PRESET_NAME, selectedPerson.name);
            return presets;
        }
    }
}