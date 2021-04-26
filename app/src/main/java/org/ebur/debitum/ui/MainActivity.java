
package org.ebur.debitum.ui;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.DialogFragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ebur.debitum.R;
import org.ebur.debitum.viewModel.PersonFilterViewModel;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private final Set<Integer> DESTINATIONS_WITH_FAB_AND_BOTTOMNAV = Stream.of(R.id.personSumListFragment, R.id.transactionListFragment, R.id.itemTransactionListFragment)
            .collect(Collectors.toCollection(HashSet::new));
    private final Set<Integer> DESTINATIONS_WITH_PERSON_FILTER = Stream.of(R.id.transactionListFragment, R.id.itemTransactionListFragment)
            .collect(Collectors.toCollection(HashSet::new));

    private NavController nav;
    private PersonFilterViewModel personFilterViewModel;
    private Toolbar toolbar, filterBar;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        personFilterViewModel = new ViewModelProvider(this).get(PersonFilterViewModel.class);

        // setup toolbar
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        nav = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.personSumListFragment,
                R.id.transactionListFragment,
                R.id.itemTransactionListFragment)
                .build();
        toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, nav, appBarConfiguration);
        setSupportActionBar(toolbar);

        //setup filter bar
        filterBar = findViewById(R.id.filter_bar);
        filterBar.getMenu().findItem(R.id.miDismiss_filter).setOnMenuItemClickListener(item -> {
            onDismissPersonFilterAction();
            return true;
        });
        // observe filterPerson to set filterBar title
        personFilterViewModel.getFilterPersonLive().observe(this, filterPerson -> {
            if(filterPerson != null) filterBar.setTitle(filterPerson.name);
        });

        // control filter bar visibility (only show in certain screens)
        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(DESTINATIONS_WITH_PERSON_FILTER.contains(destination.getId())
                    && personFilterViewModel.getFilterPerson() != null) filterBar.setVisibility(View.VISIBLE);
            else filterBar.setVisibility(View.GONE);
        });

        // setup fab
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> nav.navigate(R.id.editTransactionFragment));

        // control FAB visibility
        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(DESTINATIONS_WITH_FAB_AND_BOTTOMNAV.contains(destination.getId())) {
                fab.show();
            }
            else {
                fab.hide();
            }
        });

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
        bottomNav.setItemOnTouchListener(R.id.btm_settings, (view, event) -> {
            view.performClick();
            nav.navigate(R.id.settingsActivity);
            return true;
        });

        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.personSumListFragment) bottomNav.setSelectedItemId(R.id.btm_people);
            else if(id == R.id.transactionListFragment) bottomNav.setSelectedItemId(R.id.btm_money);
            else if (id == R.id.itemTransactionListFragment) bottomNav.setSelectedItemId(R.id.btm_items);
        });
    }

    public void setToolbarTitle(int titleResId) {
        toolbar.setTitle(titleResId);
    }

    public void showFilterBar() {
        if(personFilterViewModel.getFilterPerson() != null) {
            filterBar.setTitle(personFilterViewModel.getFilterPerson().name);
            filterBar.setVisibility(View.VISIBLE);
        }
    }

    private void onDismissPersonFilterAction() {
        personFilterViewModel.setFilterPerson(null);
        filterBar.setTitle("");
        filterBar.setVisibility(View.GONE);
        // replace curremt framgent with a new one of the same class
        // (then unfiltered, as the viewModel's filterPerson was nulled)
        NavDestination current = nav.getCurrentDestination();
        if (current != null) nav.navigate(current.getId());
    }
}