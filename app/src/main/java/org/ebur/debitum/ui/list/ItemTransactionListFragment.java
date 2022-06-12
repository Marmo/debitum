package org.ebur.debitum.ui.list;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.Selection;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.ui.SettingsFragment;
import org.ebur.debitum.viewModel.ItemReturnedFilterViewModel;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// like TransactionListFragment but shows only non-monetary items
public class ItemTransactionListFragment extends TransactionListFragment {

    private final static String TAG = "ItemTransactionListFragment";

    private ItemReturnedFilterViewModel returnedFilterViewModel;
    private TextView descView;
    private Menu menu;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // view model scoped to activity to make setting persistent across screens
        returnedFilterViewModel = new ViewModelProvider(requireActivity()).get(ItemReturnedFilterViewModel.class);

        // set standard item returned state filter mode if filter mode is yet undefined
        if (returnedFilterViewModel.getFilterMode().getValue() == ItemReturnedFilterViewModel.FILTER_UNDEF) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            int standardFilterMode =
                    Integer.parseInt(
                            pref.getString(SettingsFragment.PREF_KEY_ITEM_RETURNED_STANDARD_FILTER,
                                    Integer.toString(ItemReturnedFilterViewModel.FILTER_ALL)
                            ));
            returnedFilterViewModel.setFilterMode(standardFilterMode);
        }

        View root = super.onCreateView(inflater, container, savedInstanceState);
        assert root != null;
        descView = root.findViewById(R.id.header_description);
        return root;
    }

    @Override
    protected void subscribeToViewModel() {
        viewModel.getItemTransactions().observe(getViewLifecycleOwner(), this::updateAdapter);
        returnedFilterViewModel.getFilterMode().observe(getViewLifecycleOwner(), filterMode -> {
            String[] descs = getResources().getStringArray(R.array.header_desc_items);
            descView.setText(descs[filterMode-1]);
            setFilterRadioButtonsCheckedStatus(filterMode);
            updateAdapter(viewModel.getItemTransactions().getValue());
        });
    }

    private void updateAdapter(List<TransactionWithPerson> transactions) {
        if (transactions != null) {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            List<TransactionWithPerson> listForAdapter = filter(transactions, filterPerson);
            Integer filterMode = returnedFilterViewModel.getFilterMode().getValue();
            listForAdapter = filter(listForAdapter, filterMode != null ? filterMode : 0);
            updateTotalHeader(TransactionWithPerson.getNumberOfItems(listForAdapter));
            adapter.submitList(listForAdapter);

            // show or hide empty screen
            boolean empty = transactions.isEmpty();
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void setupTotalHeader(@NonNull View root) {
        TextView descView = root.findViewById(R.id.header_description);
        descView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void updateTotalHeader(int total) {
        TextView totalView = requireView().findViewById(R.id.header_total);
        totalView.setText(String.format(Locale.getDefault(), "%d", total));
    }

    // ---------------------------
    // Toolbar Menu event handling
    // ---------------------------

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_transaction_list, menu);
        this.menu = menu;
        Integer filterMode = returnedFilterViewModel.getFilterMode().getValue();
        setFilterRadioButtonsCheckedStatus(filterMode != null ? filterMode : 0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miFilterReturned) {
            returnedFilterViewModel.setFilterMode(ItemReturnedFilterViewModel.FILTER_RETURNED);
            return true;
        } else if (id==R.id.miFilterUnreturned) {
            returnedFilterViewModel.setFilterMode(ItemReturnedFilterViewModel.FILTER_UNRETURNED);
            return true;
        } else if (id==R.id.miFilterAll) {
            returnedFilterViewModel.setFilterMode(ItemReturnedFilterViewModel.FILTER_ALL);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setFilterRadioButtonsCheckedStatus(int filterMode) {
        if (menu != null) {
            // CAUTION: if android:checkableBehavior="single"
            // (MenuItemImpl.mFlags & MenuItemImpl.EXCLUSIVE != 0) setChecked does not care about
            // the value passed but sets the item for that setChecked is called as checked and the
            // others in the group unchecked!
            @IdRes int menuItemResId;
            switch (filterMode) {
                case ItemReturnedFilterViewModel.FILTER_ALL:
                    menuItemResId = R.id.miFilterAll;
                    break;
                case ItemReturnedFilterViewModel.FILTER_UNRETURNED:
                    menuItemResId = R.id.miFilterUnreturned;
                    break;
                case ItemReturnedFilterViewModel.FILTER_RETURNED:
                    menuItemResId = R.id.miFilterReturned;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown filter mode: "+filterMode);
            }
            // could also pass false here, as setChecked does not care in exclusive mode!
            menu.findItem(menuItemResId).setChecked(true);
        }
    }

    // -----------
    // Action Mode
    // -----------

    @Override
    protected boolean createActionMode(ActionMode mode, Menu menu) {
        super.createActionMode(mode, menu);
        MenuItem returnedItem = menu.findItem(R.id.miReturned);
        returnedItem.setIcon(R.drawable.ic_item_returned_24);
        returnedItem.setTitle(R.string.actionmode_return_item);
        return true;
    }

    @Override
    protected boolean prepareActionMode(ActionMode mode, Menu menu) {
        super.prepareActionMode(mode, menu); // shows mark-returned item!
        Selection<Long> selection = selectionTracker.getSelection();
        boolean returned = false;
        if(selection.size() == 1 ) {
            try {
                returned = viewModel.isTransactionReturned(selectionTracker.getSelection().iterator().next().intValue());
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error upon checking if selected transaction is already returned: "+e.getMessage());
            }
            // hide mark-returned-item if selected txn is already returned
            if (returned) {
                menu.findItem(R.id.miReturned).setVisible(false);
            }
        }

        // we do not want to show a (nonsense) sum here (which is set by super.perpareActionMode()!)
        mode.setSubtitle(null);

        return true;
    }

    @Override
    protected void onActionModeReturned(int selectedId) {
        // get Transaction from viewModel/repository
        Transaction txn;
        try {
            txn = viewModel.getTransactionFromDatabase(selectedId);
        } catch (ExecutionException |InterruptedException e) {
            //TODO notify with toast
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Log.e(TAG, errorMessage);
            return;
        }
        // set returned
        txn.setReturned();
        // update via viewModel/repository
        viewModel.update(txn);
    }

    protected List<TransactionWithPerson> filter(List<TransactionWithPerson> transactions, int filterMode) {
        if (transactions == null) return null;
        else return transactions.stream()
                .filter(twp -> !twp.transaction.isReturned() && (filterMode & ItemReturnedFilterViewModel.FILTER_UNRETURNED) > 0
                        || twp.transaction.isReturned() && (filterMode & ItemReturnedFilterViewModel.FILTER_RETURNED) > 0)
                .collect(Collectors.toList());
    }
}
