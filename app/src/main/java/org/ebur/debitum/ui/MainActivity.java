
package org.ebur.debitum.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ebur.debitum.R;
import org.ebur.debitum.viewModel.PersonFilterViewModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<Integer> DESTINATIONS_WITH_FAB =
            Stream.of(R.id.people_dest, R.id.money_dest, R.id.item_dest)
                .collect(Collectors.toCollection(ArrayList::new));
    private final ArrayList<Integer> DESTINATIONS_WITH_PERSON_FILTER = Stream.of(R.id.money_dest, R.id.item_dest)
            .collect(Collectors.toCollection(ArrayList::new));

    private NavController nav;
    private PersonFilterViewModel personFilterViewModel;
    private Toolbar filterBar;
    BottomNavigationView bottomNav;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        personFilterViewModel = new ViewModelProvider(this).get(PersonFilterViewModel.class);

        setupToolbar();
        setupFilterBar();
        setupBottomNavigation();
        setupFAB();
    }

    private void setupToolbar() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        nav = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.people_dest,
                R.id.money_dest,
                R.id.item_dest,
                R.id.settings_dest)
                .build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, nav, appBarConfiguration);
        setSupportActionBar(toolbar);
    }

    private void setupFilterBar() {
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
    }

    private void setupFAB() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> onAddTransactionAction(view));

        // control FAB visibility
        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(DESTINATIONS_WITH_FAB.contains(destination.getId())) {
                fab.show();
                bottomNav.getMenu().findItem(R.id.btm_placeholder).setVisible(true);
            }
            else {
                fab.hide();
                bottomNav.getMenu().findItem(R.id.btm_placeholder).setVisible(false);
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, nav);
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

    private void onAddTransactionAction(View fab) {
        boolean isItemList = false;
        NavDestination dest = nav.getCurrentDestination();
        if(dest != null)
            isItemList = dest.getId() == R.id.item_dest;
        Bundle args = new Bundle();
        args.putBoolean(EditTransactionFragment.ARG_ID_NEW_ITEM, isItemList);
        nav.navigate(R.id.editTransaction_dest, args);
    }
}