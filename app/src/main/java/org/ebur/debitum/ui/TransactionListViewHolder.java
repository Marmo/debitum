package org.ebur.debitum.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    public void bind(String name, String description, String amount, int sign, Date timestamp) {
        txnNameView.setText(name);
        txnDescriptionView.setText(description);
        txnAmountView.setText(amount);
        String dateFormat = this.itemView.getContext().getResources().getString(R.string.add_transaction_date_format);
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
    }

    static TransactionListViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionListViewHolder(view);
    }
}

