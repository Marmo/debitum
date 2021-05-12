package org.ebur.debitum.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;

class PersonSumListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View itemView;
    private final TextView nameView;
    private final TextView oweLentLabelView;
    private final TextView sumView;

    private Person person;

    private PersonSumListViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        nameView = itemView.findViewById(R.id.list_item_name);
        oweLentLabelView = itemView.findViewById(R.id.list_item_owe_lent);
        sumView = itemView.findViewById(R.id.list_item_sum);

        itemView.setOnClickListener(this);
    }

    static PersonSumListViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_person_sum_list, parent, false);
        return new PersonSumListViewHolder(view);
    }

    public void bind(PersonWithTransactions pwt, boolean isSelected) {
        nameView.setText(pwt.person.name);
        sumView.setText(Transaction.getFormattedSum(pwt.transactions, false));

        person = pwt.person;

        int sign = Transaction.getSumSign(pwt.transactions);
        switch(sign) {
            case 1:
                oweLentLabelView.setText(R.string.person_sum_list_you_owe);
                sumView.setTextColor(sumView.getResources().getColor(R.color.owe_green, null));
                break;
            case 0:
                oweLentLabelView.setText(R.string.person_sum_list_no_debt);
                sumView.setVisibility(View.GONE);
                break;
            case -1:
                oweLentLabelView.setText(R.string.person_sum_list_you_lent);
                sumView.setTextColor(sumView.getResources().getColor(R.color.lent_red, null));
        }

        ViewCompat.setTransitionName(itemView, person.name);

        // selection state
        itemView.setActivated(isSelected);
    }

    @Override
    public void onClick(View v) {
        NavController navController = NavHostFragment.findNavController(FragmentManager.findFragment(v));
        Bundle args = new Bundle();
        args.putParcelable(TransactionListFragment.ARG_FILTER_PERSON, person);

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(v, person.name)
                .build();

        navController.navigate(R.id.action_personSumList_to_transactionList_on_filter, args, null, extras);
    }

    // anonymous implementation of androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
    //     https://proandroiddev.com/a-guide-to-recyclerview-selection-3ed9f2381504?gi=ee4affe1b9d3
    //     https://developer.android.com/reference/androidx/recyclerview/selection/package-summary
    ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<Long>() {
            @Override
            public int getPosition() { return getAdapterPosition(); }

            @Override
            public Long getSelectionKey() { return getItemId(); }
        };
    }
}

