package org.ebur.debitum.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;

import java.util.Date;

class TransactionListViewHolder extends RecyclerView.ViewHolder {
    private final TextView txnNameView;
    private final TextView txnDescriptionView;
    private final TextView txnAmountView;
    private final TextView txnGaveReceivedView;
    private final TextView txnTimestampView;

    private TransactionListViewHolder(View itemView) {
        super(itemView);
        txnNameView = itemView.findViewById(R.id.list_item_name);
        txnDescriptionView = itemView.findViewById(R.id.list_item_description);
        txnAmountView = itemView.findViewById(R.id.list_item_amount);
        txnGaveReceivedView = itemView.findViewById(R.id.list_item_gave_received);
        txnTimestampView = itemView.findViewById(R.id.list_item_timestamp);
    }

    public void bind(String name, String description, String amount, int sign, Date timestamp, boolean isSelected) {
        txnNameView.setText(name);
        txnDescriptionView.setText(description);
        txnAmountView.setText(amount);
        String dateFormat = this.itemView.getContext().getResources().getString(R.string.date_format);
        txnTimestampView.setText(Utilities.formatDate(timestamp, dateFormat));

        int gaveReceivedString, amountColor;
        if(sign == -1) {
            gaveReceivedString = R.string.transaction_list_gave;
            amountColor = R.color.owe_green;
        }
        else { // sign == 1 (or 0??)
            gaveReceivedString = R.string.transaction_list_received;
            amountColor = R.color.lent_red;
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

    // anonymouns implementation of androidx.recyclerview.selection.ItemDetailsLookup.ItemDetails
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

