package org.ebur.debitum.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Transaction;

import java.util.Locale;

class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView totalView, descView;

    private HeaderViewHolder(View itemView) {
        super(itemView);
        totalView = itemView.findViewById(R.id.header_total);
        descView = itemView.findViewById(R.id.header_description);
    }

    public void bind(int total, boolean isMonetary) {
        if (isMonetary) {
            totalView.setText(Transaction.formatMonetaryAmount(total));
            descView.setVisibility(View.INVISIBLE);
            int totalColor = total>0 ? R.color.owe_green : R.color.lent_red;
            totalView.setTextColor(totalView.getResources().getColor(totalColor, null));
        }
        else {
            totalView.setText(String.format(Locale.getDefault(), "%d", total));
            descView.setVisibility(View.VISIBLE);
        }
    }

    static HeaderViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_total, parent, false);
        return new HeaderViewHolder(view);
    }
}

