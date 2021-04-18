
package org.ebur.debitum.ui;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ebur.debitum.R;

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
    }

    // TODO move to PersonSumList
    // TODO use safe args https://developer.android.com/guide/navigation/navigation-getting-started#ensure_type-safety_by_using_safe_args
    public void onSearchTransactionAction(MenuItem item) {
        nav.navigate(R.id.action_personSumListFragment_to_transactionListFragment);
    }

    public void setToolbarTitle(int titleResId) {
        toolbar.setTitle(titleResId);
    }
}