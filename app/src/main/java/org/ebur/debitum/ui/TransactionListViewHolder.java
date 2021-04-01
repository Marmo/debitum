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
    private final TextView txnTimestampView;

    private TransactionListViewHolder(View itemView) {
        super(itemView);
        txnNameView = itemView.findViewById(R.id.list_item_name);
        txnDescriptionView = itemView.findViewById(R.id.list_item_description);
        txnAmountView = itemView.findViewById(R.id.list_item_amount);
        txnTimestampView = itemView.findViewById(R.id.list_item_timestamp);
    }

    public void bind(String name, String description, String amount, Date timestamp) {
        txnNameView.setText(name);
        txnDescriptionView.setText(description);
        txnAmountView.setText(amount);
        String dateFormat = this.itemView.getContext().getResources().getString(R.string.add_transaction_date_format);
        txnTimestampView.setText(Utilities.formatDate(timestamp, dateFormat));
    }

    static TransactionListViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionListViewHolder(view);
    }
}

