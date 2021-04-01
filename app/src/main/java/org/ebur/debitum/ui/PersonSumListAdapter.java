package org.ebur.debitum.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.ebur.debitum.database.PersonWithSum;

public class PersonSumListAdapter extends ListAdapter<PersonWithSum, PersonSumListViewHolder> {

    public PersonSumListAdapter(@NonNull DiffUtil.ItemCallback<PersonWithSum> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public PersonSumListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PersonSumListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(PersonSumListViewHolder holder, int position) {
        PersonWithSum current = getItem(position);
        holder.bind(current.person.name, current.getFormattedSum(), current.getSign());
    }

    static class PersonSumDiff extends DiffUtil.ItemCallback<PersonWithSum> {

        @Override
        public boolean areItemsTheSame(@NonNull PersonWithSum oldItem, @NonNull PersonWithSum newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull PersonWithSum oldItem, @NonNull PersonWithSum newItem) {
            return oldItem.equals(newItem);
        }
    }

}
