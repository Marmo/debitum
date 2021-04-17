
package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ebur.debitum.R;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        setSupportActionBar(toolbar);

        // setup fab
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditTransactionFragment.class);
            startActivity(intent);
        });
    }

    // TODO move to PersonSumList
    // TODO use safe args https://developer.android.com/guide/navigation/navigation-getting-started#ensure_type-safety_by_using_safe_args
    public void onSearchTransactionAction(MenuItem item) {
        navController.navigate(R.id.action_personSumListFragment_to_transactionListFragment);
    }

    public void setToolbarTitle(int titleResId) {
        toolbar.setTitle(titleResId);
    }
}