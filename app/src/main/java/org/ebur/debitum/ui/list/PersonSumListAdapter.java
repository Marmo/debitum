package org.ebur.debitum.ui.list;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.ebur.debitum.database.PersonWithTransactions;

public class PersonSumListAdapter
        extends ListAdapter<PersonSumListAdapter.PersonWithAvatar, PersonSumListViewHolder>
        implements AbstractBaseListFragment.Adapter {

    private SelectionTracker<Long> selectionTracker = null;

    public PersonSumListAdapter(@NonNull DiffUtil.ItemCallback<PersonWithAvatar> diffCallback) {
        super(diffCallback);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public PersonSumListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PersonSumListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonSumListViewHolder holder, int position) {
        PersonWithAvatar current = getItem(position);
        holder.bind(current.pwt, current.avatar, selectionTracker.isSelected(getItemId(position)));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).pwt.person.idPerson;
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) { this.selectionTracker = selectionTracker; }

    static class PersonSumDiff extends DiffUtil.ItemCallback<PersonWithAvatar> {

        @Override
        public boolean areItemsTheSame(@NonNull PersonWithAvatar oldItem, @NonNull PersonWithAvatar newItem) {
            return oldItem.pwt.person.idPerson == newItem.pwt.person.idPerson;
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull PersonWithAvatar oldItem, @NonNull PersonWithAvatar newItem) {
            // changes in avatar are ONLY relevant if the contacts permission is granted while the
            // personSumList is shown, so comparing classes is enough
            return oldItem.pwt.equals(newItem.pwt)
                    && oldItem.avatar.getClass().equals(newItem.avatar.getClass());
        }
    }

    public static class PersonWithAvatar {
        public PersonWithTransactions pwt;
        public Drawable avatar;

        public PersonWithAvatar(PersonWithTransactions pwt, Drawable avatar) {
            this.pwt = pwt;
            this.avatar = avatar;
        }
    }
}
