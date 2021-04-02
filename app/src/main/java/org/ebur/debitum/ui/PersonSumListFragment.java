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
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
        viewModel.getPersonsWithTransactions().observe(getViewLifecycleOwner(), personsWithTransactions -> {
            // the list returned by TransactionDao.getAllPersonsWithTransactions) contains one [PersonWithTransactions]
            // for every monetary? transaction, with a list of all transactions of the person, thus
            // having each transaction multiple times. Thus we have to filter it, leaving only one
            // [PersonWithTransactions} for every distinct person having all of their transactions
            /*List<Integer> foundIds = new ArrayList<>();
            List<PersonWithTransactions> correctedPWT = new ArrayList<>();
            for (PersonWithTransactions pwt : personsWithTransactions) {
                if(!foundIds.contains(pwt.person.idPerson)) {
                    correctedPWT.add(new PersonWithTransactions(pwt.person, pwt.transactions));
                    foundIds.add(pwt.person.idPerson);
                }
            }
            // Update the transactions in the [recyclerView] via [adapter].
            adapter.submitList(correctedPWT);*/
            adapter.submitList(personsWithTransactions);
        });

        return root;
    }
}