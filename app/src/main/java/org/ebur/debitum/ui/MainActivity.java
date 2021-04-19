
package org.ebur.debitum.ui;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ebur.debitum.R;
import org.ebur.debitum.viewModel.PersonFilterViewModel;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private final Set<Integer> DESTINATIONS_WITH_FAB = Stream.of(R.id.transactionListFragment, R.id.personSumListFragment)
            .collect(Collectors.toCollection(HashSet::new));

    private NavController nav;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        nav = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(nav.getGraph()).build();
        toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, nav, appBarConfiguration);
        setSupportActionBar(toolbar);

        // setup fab
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            nav.navigate(R.id.editTransactionFragment);
        });

        // control FAB visibility
        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(DESTINATIONS_WITH_FAB.contains(destination.getId())) fab.show();
            else fab.hide();
        });

        // setup Bottom Navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setItemOnTouchListener(R.id.btm_people, (view, event) -> {
            view.performClick();
            nav.navigate(R.id.personSumListFragment);
            return true;
        });
        bottomNav.setItemOnTouchListener(R.id.btm_money, (view, event) -> {
            view.performClick();
            nav.navigate(R.id.transactionListFragment);
            return true;
        });
        bottomNav.setItemOnTouchListener(R.id.btm_items, (view, event) -> {
            view.performClick();
            nav.navigate(R.id.itemTransactionListFragment);
            return true;
        });
    }

    public void setToolbarTitle(int titleResId) {
        toolbar.setTitle(titleResId);
    }
}