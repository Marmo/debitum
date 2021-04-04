package org.ebur.debitum.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.ebur.debitum.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MainTabPagerAdapter extends FragmentStateAdapter {

    private final int TAB_PEOPLE = 0, TAB_TRANSACTIONS = 1, TAB_COUNT = 2;

    public MainTabPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // getItem is called to instantiate the fragment for the given page.
        switch (position) {
            case TAB_PEOPLE: return PersonSumListFragment.newInstance();
            case TAB_TRANSACTIONS: return TransactionListFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}