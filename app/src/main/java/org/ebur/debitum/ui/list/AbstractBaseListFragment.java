package org.ebur.debitum.ui.list;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;

import com.google.android.material.transition.MaterialFadeThrough;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.util.ColorUtils;
import org.ebur.debitum.util.Utilities;


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
     * @return the Resource-Id of the root layout that should be used to inflate that fragment
     */
    @LayoutRes
    abstract int getLayout();

    /**
     * @return the class of the ViewModel to be used
     */
    abstract Class<TViewModel> getViewModelClass(); // needed, because TViewModel.class does not work when getting the ViewModel

    /**
     * @return the RecyclerView's adapter (as we cannot directly instantiate TAdapter here)
     */
    abstract TAdapter getAdapter();

    /**
     * @return a bundle of preset arguments to be passed to EditTransactionFragment when creating a
     * new transaction. If none or more than one rows are selected, this method should generally
     * return null (but probably also other solutions could make sense). The arguments should be one
     * or more of the constants defined and consumed in EditTransactionFragment:
     * EditTransactionFragment.ARG_PRESET_TYPE
     * EditTransactionFragment.ARG_PRESET_NAME
     * EditTransactionFragment.ARG_PRESET_AMOUNT
     * EditTransactionFragment.ARG_PRESET_DESCRIPTION
     * EditTransactionFragment.ARG_PRESET_DATE
     * EditTransactionFragment.ARG_PRESET_RETURNDATE
     */
    @Nullable public abstract Bundle getPresetsFromSelection();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transitions
        int duration = getResources().getInteger(R.integer.duration_bottom_nav_transition);
        Transition transition = new MaterialFadeThrough().setDuration(duration);
        setEnterTransition(transition);
        setExitTransition(transition);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(getViewModelClass());

        View root = inflater.inflate(getLayout(), container, false);

        emptyView = root.findViewById(R.id.emptyDbView);

        setupTotalHeader(root);
        setupRecyclerView(root);
        buildSelectionTracker();
        subscribeToViewModel();
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postponeEnterTransition();
        view.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                }
        );
    }

    protected void setupTotalHeader(@NonNull View root) {
        TextView descView = root.findViewById(R.id.header_description);
        descView.setVisibility(View.INVISIBLE);
    }
    protected void updateTotalHeader(int total) {
        TextView totalView = requireView().findViewById(R.id.header_total);
        totalView.setText(Transaction.formatMonetaryAmount(total, Utilities.getNrOfDecimals(requireContext())));
        @ColorInt int totalColor = total>0
                ? ColorUtils.getOweColor(requireContext())
                : ColorUtils.getLentColor(requireContext());
        totalView.setTextColor(totalColor);
    }

    protected void setupRecyclerView(@NonNull View root) {
        recyclerView = root.findViewById(R.id.recyclerview);
        adapter = getAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        addRecyclerViewDecorations();
    }

    protected void addRecyclerViewDecorations() {
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
            return createActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return prepareActionMode(mode, menu);
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
            } else if(id==R.id.miReturned) {
                int selectedId = selectionTracker.getSelection().iterator().next().intValue();
                selectionTracker.clearSelection();
                onActionModeReturned(selectedId);
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

    protected boolean createActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_list_action_mode, menu);
        menu.findItem(R.id.miReturned).setVisible(false);
        actionMode = mode;
        return true;
    }
    protected boolean prepareActionMode(ActionMode mode, Menu menu) {
        int nRowsSelected = selectionTracker.getSelection().size();
        // only show edit transaction menu item if exactly one transaction is selected
        menu.findItem(R.id.miEdit).setVisible(nRowsSelected == 1);
        // only show delete transaction menu item if one or more items are selected
        menu.findItem(R.id.miDelete).setVisible(nRowsSelected >= 1);
        // hide show mark-returned menu item (feel free to override this prepareActionMode to change
        // this behaviour ...)
        menu.findItem(R.id.miReturned).setVisible(false);

        CharSequence title = getResources().getQuantityString(R.plurals.actionmode_selected, nRowsSelected, nRowsSelected);
        mode.setTitle(title);
        return true;
    }

    protected abstract void onActionModeEdit(int selectedId);
    protected abstract void onActionModeDelete(Selection<Long> selection);
    protected abstract void onActionModeReturned(int selectedId);


    // this seems to be the best place to finish the action mode, as it is called quickly after
    // changing the fragment, yet it is _not_ called on screen-off.
    // NOTE: Until now I did not find a place that matches the above requirements and is _not_
    // called upon changing from light to dark mode (which would be desirable)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // finish action mode to prevent it being carried over to another list (causes crash, see issue #15)
        if (actionMode!=null) {
            actionMode.finish();
            actionMode=null;
        }
    }

    interface Adapter {
        void setSelectionTracker(SelectionTracker<Long> selectionTracker);
    }
}

