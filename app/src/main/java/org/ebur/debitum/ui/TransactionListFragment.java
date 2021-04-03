package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.Date;

import static android.app.Activity.RESULT_OK;

// TODO make list items selectable to delete them (via ActionBar-Button)
// TODO make list items selactabke to edit them (fab or ActionBar-Button)

public class TransactionListFragment extends Fragment {

    public static final int NEW_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;

    private TransactionListViewModel viewModel;

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

        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionListViewModel.class);
        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            // Update the transactions in the [recyclerView] via [adapter].
            adapter.submitList(transactions);
        });

        return root;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_TRANSACTION_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Transaction transaction = new Transaction(extras.getInt("PERSON_ID"),
                                                      extras.getInt("AMOUNT"),
                                                      extras.getBoolean("ISMONETARY"),
                                                      extras.getString("DESC"),
                                                      new Date(extras.getLong("TIMESTAMP")));
            viewModel.insert(transaction);
        }
    }
}