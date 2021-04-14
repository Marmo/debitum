
package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.ebur.debitum.R;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_NEW_TRANSACTION = "org.ebur.debitum.NEW_TRANSACTION";

    private Menu menu;
    private FloatingActionButton fab;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        setSupportActionBar(toolbar);

        // setup fab
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditTransactionActivity.class);
            intent.putExtra(MainActivity.EXTRA_NEW_TRANSACTION, true);
            startActivity(intent);
        });
    }

    // TODO use safe args https://developer.android.com/guide/navigation/navigation-getting-started#ensure_type-safety_by_using_safe_args
    public void onSearchTransactionAction(MenuItem item) {
        navController.navigate(R.id.action_personSumListFragment_to_transactionListFragment);
    }
}