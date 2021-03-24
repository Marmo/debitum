
package org.ebur.debitum.ui;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import org.ebur.debitum.R;

public class MainActivity extends AppCompatActivity {

    // TODO can't I set these in xml and that's it?
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_people_title, R.string.tab_txn_title};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainTabPagerAdapter adapter = new MainTabPagerAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        // setup and TabLayout and attach it to the ViewPager
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

    }
}