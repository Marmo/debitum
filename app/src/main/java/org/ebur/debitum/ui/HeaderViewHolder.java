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
            // factor -1: we want to see negative numbers if we owe money to others, while the
            // amount in Transaction objects is positive in case someone gave us money
            // TODO maybe the logic in Transaction should be also; negative amount if someone lends me money??
            totalView.setText(Transaction.formatMonetaryAmount(-1 * total));
            descView.setVisibility(View.INVISIBLE);
        }
        else {
            totalView.setText(total);
            descView.setVisibility(View.VISIBLE);
        }



        int amountColor = total<0 ? R.color.owe_green : R.color.lent_red;
        totalView.setTextColor(totalView.getResources().getColor(amountColor, null));
    }

    static HeaderViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_total, parent, false);
        return new HeaderViewHolder(view);
    }
}

