package org.ebur.debitum.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;

import java.util.Locale;

class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView totalView;

    private HeaderViewHolder(View itemView) {
        super(itemView);
        totalView = itemView.findViewById(R.id.header_total);
    }

    public void bind(int total) {
        totalView.setText(String.format(Locale.getDefault(), "%d", total));

        int amountColor = total<0?R.color.owe_green:R.color.lent_red;

        totalView.setTextColor(totalView.getResources().getColor(amountColor, null));
    }

    static HeaderViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_total, parent, false);
        return new HeaderViewHolder(view);
    }
}

