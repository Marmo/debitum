
package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import org.ebur.debitum.R;
import org.ebur.debitum.viewModel.TransactionListViewModel;

import java.util.HashMap;

// TODO put fab in MainActivity instead of TransactionListFragment
// TODO make fab briefly disappear when changing tabs
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_NEW_PERSON = "org.ebur.debitum.NEW_PERSON";
    public static final String EXTRA_NEW_TRANSACTION = "org.ebur.debitum.NEW_TRANSACTION";

    private TransactionListViewModel viewModel;
    private Menu menu;

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_people_title, R.string.tab_txn_title};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainTabPagerAdapter adapter = new MainTabPagerAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        // setup and TabLayout and attach it to the ViewPager
        // TODO remove naming of tabs from here
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        viewModel = new ViewModelProvider(this).get(TransactionListViewModel.class);
        viewModel.getToolbarMenuItems().observe(this, this::updateToolbarMenu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        updateToolbarMenu(viewModel.getToolbarMenuItems().getValue());
        return true;
    }

    public void onAddPersonAction(MenuItem item) {
        Intent intent = new Intent(this, EditPersonActivity.class);
        intent.putExtra(EXTRA_NEW_PERSON, true);
        startActivity(intent, null);
    }

    private void updateToolbarMenu(HashMap<Integer, Boolean> menuItems) {
        // Update toolbar menu (reset to all visible, then remove unwanted items)
        if (menu != null) {
            menu.clear();
            getMenuInflater().inflate(R.menu.menu_main, menu);
            menuItems.forEach( (menuItem, visible) -> { if(!visible) menu.removeItem(menuItem); });
        }
    }
}