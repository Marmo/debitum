package org.ebur.debitum.ui;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.TransactionWithPerson;

class TransactionListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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

        itemView.setOnClickListener(this);
    }

    public void bind(TransactionWithPerson twp, boolean isSelected) {
        txnNameView.setText(twp.person.name);
        txnDescriptionView.setText(twp.transaction.description);
        txnAmountView.setText(twp.transaction.getFormattedAmount(false));
        if (twp.transaction.isReturned()) {
            // set date text and background/text color
            Resources res = itemView.getResources();
            txnTimestampView.setText(res.getString(R.string.transaction_list_date_given_returned,
                    Utilities.formatDate(twp.transaction.timestamp, itemView.getContext()),
                    Utilities.formatDate(twp.transaction.timestampReturned, itemView.getContext())));
            itemView.setBackgroundColor(ResourcesCompat.getColor(res,
                    R.color.returned_item_background,
                    null));
            @ColorInt int textColor =  ResourcesCompat.getColor(res,
                    R.color.returned_item_text,
                    null);
            txnNameView.setTextColor(textColor);
            txnGaveReceivedView.setTextColor(textColor);
        } else {
            txnTimestampView.setText(Utilities.formatDate(twp.transaction.timestamp, itemView.getContext()));
        }

        int gaveReceivedString, amountColor;
        int sign = Integer.compare(twp.transaction.amount, 0);
        switch (sign) {
            case -1:
                gaveReceivedString = R.string.transaction_list_received;
                amountColor = R.color.lent_red;
                break;
            case 0:
            case 1:
                gaveReceivedString = R.string.transaction_list_gave;
                amountColor = R.color.owe_green;
                break;
            default:
                throw new IllegalStateException("Unexpected value (sign): " + sign);
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

    // without having an onClickListener the view will not get the pressed state when clicked
    // and thus won't reflect the backgroundTint change from list_item_bg_selector.xml
    @Override
    public void onClick(View v) {
    }
}

