package org.ebur.debitum.ui;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.MaterialFadeThrough;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Transaction;


/**
 * Generic version of the Box class.
 * @param <TViewModel>> the type of the main viewModel
 * @param <TAdapter> the type of the recycler view's adapter
 * @param <TViewHolder> the type of the adapter's ViewHolder
 * @param <TListItem> the type containing the data for one row (=the "Type" in the observed
 *                   LiveData<List<Type>>)
 */
public abstract class AbstractBaseListFragment
        <TViewModel extends AndroidViewModel,
                TAdapter extends ListAdapter<TListItem, TViewHolder> & AbstractBaseListFragment.Adapter,
                TViewHolder extends RecyclerView.ViewHolder,
                TListItem>
        extends Fragment {

    protected static final String TAG = "You should have set this yourselves";

    protected TViewModel viewModel;
    protected RecyclerView recyclerView;
    protected TAdapter adapter;
    protected SelectionTracker<Long> selectionTracker = null;
    protected View emptyView;

    protected ActionMode actionMode;

    /**
     * returns the root layout that should be used to inflate that fragment
     */
    abstract int getLayout();
    abstract Class<TViewModel> getViewModelClass();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transitions
        setEnterTransition(new MaterialFadeThrough().setDuration(400));
        setExitTransition(new MaterialFadeThrough().setDuration(400));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(getViewModelClass());

        View root = inflater.inflate(getLayout(), container, false);

        emptyView = root.findViewById(R.id.emptyDbView);

        setupTotalHeader(root);
        setupRecyclerView(root);
        buildSelectionTracker();
        subscribeToViewModel();
        setHasOptionsMenu(true);
        return root;
    }

    protected void setupTotalHeader(@NonNull View root) {
        TextView descView = root.findViewById(R.id.header_description);
        descView.setVisibility(View.INVISIBLE);
    }
    protected void updateTotalHeader(int total) {
        TextView totalView = requireView().findViewById(R.id.header_total);
        totalView.setText(Transaction.formatMonetaryAmount(total));
        int totalColor = total>0 ? R.color.owe_green : R.color.lent_red;
        totalView.setTextColor(totalView.getResources().getColor(totalColor, null));
    }

    protected void setupRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.recyclerview);
        adapter = new TAdapter(new TAdapter.Diff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    protected void buildSelectionTracker() {
        selectionTracker = new SelectionTracker.Builder<>(
                TAG,
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

        // start action mode & change visible menu items depending on item selection
        this.selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                if(actionMode == null) {
                    actionMode = requireActivity().startActionMode(actionModeCallback);
                } else if(!selectionTracker.hasSelection()) {
                    actionMode.finish();
                } else {
                    actionMode.invalidate(); // refresh visible menu items
                }
            }
        });
        adapter.setSelectionTracker(this.selectionTracker);
    }

    protected abstract void subscribeToViewModel();

    // ----------------------
    // Contextual action mode
    // https://developer.android.com/guide/topics/ui/menus#CAB
    // ----------------------

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_list_action_mode, menu);
            actionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int nRowsSelected = selectionTracker.getSelection().size();
            // only show edit transaction menu item if exactly one transaction is selected
            menu.findItem(R.id.miEdit).setVisible(nRowsSelected == 1);
            // only show delete transaction menu item if one or more items are selected
            menu.findItem(R.id.miDelete).setVisible(nRowsSelected >= 1);
            CharSequence title = getResources().getQuantityString(R.plurals.actionmode_selected, nRowsSelected, nRowsSelected);
            mode.setTitle(title);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            int id = menuItem.getItemId();
            if(id==R.id.miEdit) {
                int selectedId = selectionTracker.getSelection().iterator().next().intValue();
                selectionTracker.clearSelection();
                onActionModeEdit(selectedId);
                mode.finish();
                return true;
            } else if(id==R.id.miDelete) {
                // make copy of selection so we have a constant list of selected items, even during
                // iteratively deleting items
                MutableSelection<Long> selectionCopy = new MutableSelection<>();
                selectionTracker.copySelection(selectionCopy);
                selectionTracker.clearSelection();
                onActionModeDelete(selectionCopy);
                mode.finish();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectionTracker.clearSelection();
            actionMode = null;
        }
    };

    protected abstract void onActionModeEdit(int selectedId);
    protected abstract void onActionModeDelete(Selection<Long> selection);

    interface Adapter {
        void setSelectionTracker(SelectionTracker<Long> selectionTracker);
    }
}

