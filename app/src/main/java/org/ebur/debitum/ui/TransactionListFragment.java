package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.ebur.debitum.R;
import org.ebur.debitum.model.Transaction;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import static android.app.Activity.RESULT_OK;


public class TransactionListFragment extends Fragment {

    public static final int NEW_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;

    private TransactionListViewModel transactionListViewModel;

    public static TransactionListFragment newInstance() {
        return new TransactionListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);
        final TransactionListAdapter adapter = new TransactionListAdapter(new TransactionListAdapter.TransactionDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), AddTransactionActivity.class);
                startActivityForResult(intent, TransactionListFragment.NEW_TRANSACTION_ACTIVITY_REQUEST_CODE);
            }
        });

        // attach to LiveData
        transactionListViewModel = new ViewModelProvider(this).get(TransactionListViewModel.class);
        transactionListViewModel.getTransactions().observe(requireActivity(), transactions -> {
            // Update the cached copy of the words in the adapter.
            adapter.submitList(transactions);
        });


        return root;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_TRANSACTION_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Transaction transaction = new Transaction(extras.getString("NAME"),
                    extras.getInt("AMOUNT"),
                    extras.getString("DESC"),
                    extras.getString("TIMESTAMP"));
            transactionListViewModel.insert(transaction);
        } else {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    "missing data, not saved",
                    Toast.LENGTH_LONG).show();
        }
    }

 /*   private ArrayList<TransactionListItem> getListData() {
        ArrayList<TransactionListItem> results = new ArrayList<>();
        TransactionListItem txn1 = new TransactionListItem();
        txn1.setName("Haushaltskasse");
        txn1.setDescription("Gebana");
        txn1.setAmount(5674);
        txn1.setTimestamp("2020-10-02T10:03:01");
        results.add(txn1);
        TransactionListItem txn2 = new TransactionListItem();
        txn2.setName("Natalie");
        txn2.setDescription("Lowa-Schuhe");
        txn2.setAmount(16000);
        txn2.setTimestamp("2021-01-02T16:23:00");
        results.add(txn2);
        TransactionListItem txn3 = new TransactionListItem();
        txn3.setName("Ingo");
        txn3.setDescription("Fahrschein");
        txn3.setAmount(-1000);
        txn3.setTimestamp("2020-12-17T10:13:01");
        results.add(txn3);
        return results;
    }*/
}