package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ebur.debitum.R;
import org.ebur.debitum.viewModel.PersonSumListViewModel;
import org.ebur.debitum.viewModel.TransactionListViewModel;

// TODO add Activity to show all transactions of one person that is launched when clicking on one row
// TODO in PersonTransactionListActivity add ActionBar options to edit/delete person
public class PersonSumListFragment extends Fragment {
    public static final String EXTRA_EDITED_PERSON = "org.ebur.debitum.EDITED_PERSON";

    private PersonSumListViewModel viewModel;

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
        viewModel = new ViewModelProvider(requireActivity()).get(PersonSumListViewModel.class);
        viewModel.getPersonsWithTransactions().observe(getViewLifecycleOwner(), adapter::submitList);

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // add fragment's menu items to activity's toolbar
        inflater.inflate(R.menu.menu_person_sum_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miAddPerson) {
            onAddPersonAction(item);
            return true;
        } else {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
        }
    }

    public void onAddPersonAction(MenuItem item) {
        Intent intent = new Intent(requireActivity(), EditPersonActivity.class);
        startActivity(intent, null);
    }
}