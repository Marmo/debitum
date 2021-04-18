package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.TransactionWithPerson;

class TransactionListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView txnNameView;
    private final TextView txnDescriptionView;
    private final TextView txnAmountView;
    private final TextView txnGaveReceivedView;
    private final TextView txnTimestampView;
    private TransactionWithPerson transactionWithPerson;

    private TransactionListViewHolder(View itemView) {
        super(itemView);
        txnNameView = itemView.findViewById(R.id.list_item_name);
        txnDescriptionView = itemView.findViewById(R.id.list_item_description);
        txnAmountView = itemView.findViewById(R.id.list_item_amount);
        txnGaveReceivedView = itemView.findViewById(R.id.list_item_gave_received);
        txnTimestampView = itemView.findViewById(R.id.list_item_timestamp);

        itemView.setOnClickListener(this);
    }

    public void bind(TransactionWithPerson twp, boolean isSelected) {
        this.transactionWithPerson = twp;
        txnNameView.setText(twp.person.name);
        txnDescriptionView.setText(twp.transaction.description);
        txnAmountView.setText(twp.transaction.getFormattedAmount(false));
        String dateFormat = this.itemView.getContext().getResources().getString(R.string.date_format);
        txnTimestampView.setText(Utilities.formatDate(twp.transaction.timestamp, dateFormat));

        int gaveReceivedString, amountColor;
        int sign = Integer.compare(twp.transaction.amount, 0);
        switch (sign) {
            case -1:
                gaveReceivedString = R.string.transaction_list_gave;
                amountColor = R.color.owe_green;
                break;
            case 1:
                gaveReceivedString = R.string.transaction_list_received;
                amountColor = R.color.lent_red;
                break;
            default:
                gaveReceivedString = -1;
                amountColor = -1;
        }
        txnGaveReceivedView.setText(gaveReceivedString);
        txnAmountView.setTextColor(txnAmountView.getResources().getColor(amountColor, null));

        // selection state
        itemView.setActivated(isSelected);
    }

    static TransactionListViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_list, parent, false);
        return new TransactionListViewHolder(view);
    }

    @Override
    public void onClick(View v) {
        NavController nav = Navigation.findNavController(v);
        Bundle args = new Bundle();
        args.putInt(EditTransactionFragment.ARG_ID_TRANSACTION, transactionWithPerson.transaction.idTransaction);
        nav.navigate(R.id.action_transactionListFragment_to_editTransactionFragment, args);
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

