
package org.ebur.debitum.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.ebur.debitum.BuildConfig;
import org.ebur.debitum.R;
import org.ebur.debitum.ui.list.AbstractBaseListFragment;

public class MainActivity extends AppCompatActivity {

    private NavController nav;
    BottomNavigationView bottomNav;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        setupToolbar();
        setupBottomNavigation();
        setupFAB();

        showWhatsNewPopup();
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

    private void setupFAB() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::onAddTransactionAction);

        // control FAB visibility
        nav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() != R.id.settings_dest) {
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

    private void showWhatsNewPopup() {
        // get current app version
        int currentVersion = BuildConfig.VERSION_CODE;
        // get version, the popup was already shown for
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int seenVersion = pref.getInt(SettingsFragment.PREF_KEY_CHANGELOG, 0);

        if (currentVersion > seenVersion) {
            NavHostFragment
                    .findNavController(getCurrentNavigationFragment())
                    .navigate(R.id.action_global_changelog);

            // save current version in preferences
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(SettingsFragment.PREF_KEY_CHANGELOG, currentVersion);
            editor.apply();
        }
    }

    private void onAddTransactionAction(View fab) {
        @IdRes int action;
        NavDestination dest = nav.getCurrentDestination();
        assert dest!= null;
        if (dest.getId() == R.id.item_dest) {
            action = R.id.action_add_item_transaction;
        } else {
            action = R.id.action_global_add_money_transaction;
        }

        Bundle presets;
        Fragment current = getCurrentNavigationFragment();
        if (current instanceof AbstractBaseListFragment) {
            presets = ((AbstractBaseListFragment<?,?,?,?>) current).getPresetsFromSelection();
        } else {
            presets = null;
        }
        nav.navigate(action, presets);
    }

    // https://github.com/material-components/material-components-android-examples/blob/develop/Reply/app/src/main/java/com/materialstudies/reply/ui/MainActivity.kt
    @Nullable
    Fragment getCurrentNavigationFragment() {
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if(navHostFragment != null) {
            return navHostFragment.getChildFragmentManager().getFragments().get(0);
        } else {
            return null;
        }
    }
}