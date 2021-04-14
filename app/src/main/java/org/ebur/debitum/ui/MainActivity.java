
package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    private TransactionListViewModel viewModel;
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

        viewModel = new ViewModelProvider(this).get(TransactionListViewModel.class);
        //viewModel.getToolbarMenuItems().observe(this, this::updateToolbarMenuItemVisibility);

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        updateToolbarMenuItemVisibility(viewModel.getToolbarMenuItems().getValue());
        return true;
    }*/

    /*private void updateToolbarMenuItemVisibility(ArrayList<Integer> visibleItems) {
        // Update toolbar menu (check which of the menu's items is listed in visibleItems-List
        // and set visibility accordingly)
            for (int i = 0; i < menu.size(); i++) {
                MenuItem mi = menu.getItem(i);
                int resId = mi.getItemId();
                mi.setVisible(visibleItems.contains(resId));
            }
        }
    }*/

    // TODO use safe args https://developer.android.com/guide/navigation/navigation-getting-started#ensure_type-safety_by_using_safe_args
    public void onSearchTransactionAction(MenuItem item) {
        navController.navigate(R.id.action_personSumListFragment_to_transactionListFragment);
    }
}