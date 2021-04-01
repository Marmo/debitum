package org.ebur.debitum.ui;

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

import org.ebur.debitum.R;
import org.ebur.debitum.viewModel.PersonSumListViewModel;
import org.ebur.debitum.viewModel.TransactionListViewModel;


public class PersonSumListFragment extends Fragment {

    public static final int NEW_TRANSACTION_ACTIVITY_REQUEST_CODE = 1;

    private TransactionListViewModel viewModel;

    public static PersonSumListFragment newInstance() {
        return new PersonSumListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_person_sum_list, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.person_sum_list_recyclerview);
        final PersonSumListAdapter adapter = new PersonSumListAdapter(new PersonSumListAdapter.PersonSumDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionListViewModel.class);
        viewModel.getPersonSums().observe(getViewLifecycleOwner(), personSums -> {
            // Update the transactions in the [recyclerView] via [adapter].
            adapter.submitList(personSums);
        });

        return root;
    }
}