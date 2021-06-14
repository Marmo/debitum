package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialFadeThrough;

import org.ebur.debitum.BuildConfig;
import org.ebur.debitum.R;
import org.ebur.debitum.database.AppDatabase;

import java.io.File;


public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    private final String BACKUP_FILENAME = "debitum_backup.db";
    private final String BACKUP_SUBDIR = "backup";

    public final static String PREF_KEY_DISMISS_FILTER_BEHAVIOUR = "dismiss_filter_behaviour";
    public final static String PREF_KEY_ITEM_RETURNED_STANDARD_FILTER = "item_returned_standard_filter";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setEnterTransition(new MaterialFadeThrough().setDuration(400));
        setExitTransition(new MaterialFadeThrough().setDuration(400));

        final String PREF_KEY_BACKUP = "backup";
        final String PREF_KEY_RESTORE = "restore";
        final String PREF_KEY_GUIDE = "guide";
        final String PREF_KEY_LICENSES = "licenses";
        final String PREF_KEY_VERSION = "version";

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SwitchPreferenceCompat dismissFilterPref = findPreference(PREF_KEY_DISMISS_FILTER_BEHAVIOUR);
        if(dismissFilterPref != null) {
            dismissFilterPref.setSummaryOff(R.string.pref_dismiss_filter_summary_false);
            dismissFilterPref.setSummaryOn(R.string.pref_dismiss_filter_summary_true);
        }

        ListPreference ItemReturnedFilterPref = findPreference(PREF_KEY_ITEM_RETURNED_STANDARD_FILTER);
        if(ItemReturnedFilterPref != null) {
            ItemReturnedFilterPref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        }

        Preference backupPref = findPreference(PREF_KEY_BACKUP);
        if (backupPref!=null) {
            backupPref.setSummary(getString(R.string.pref_backup_summary, requireContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR));
            backupPref.setOnPreferenceClickListener(preference -> {
                backup();
                return true;
            });
        }
        Preference restorePref = findPreference(PREF_KEY_RESTORE);
        if(restorePref!=null) {
            restorePref.setSummary(getString(R.string.pref_restore_summary, BACKUP_FILENAME));
            restorePref.setOnPreferenceClickListener(preference -> {
                restore();
                return true;
            });
        }
        Preference guidePref = findPreference(PREF_KEY_GUIDE);
        if (guidePref!=null) {
            guidePref.setOnPreferenceClickListener(preference -> {
                NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_guide);
                return true;
            });
        }

        Preference licensesPref = findPreference(PREF_KEY_LICENSES);
        if(licensesPref!=null) {
            licensesPref.setOnPreferenceClickListener(preference -> {
                NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_licenses);
                return true;
            });
        }

        Preference versionPref = findPreference(PREF_KEY_VERSION);
        if(versionPref!=null) {
            versionPref.setSummary(getString(R.string.pref_version, BuildConfig.VERSION_NAME));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // add padding so that nothing is hidden behind the bottom navigation
        getListView().setPadding(0,0,0, getResources().getDimensionPixelSize(R.dimen.bottom_nav_height)+50);
        getListView().setClipToPadding(false);
    }

    // ---------------------
    // Backup and restore DB
    // ---------------------

    private void backup() {
        String path = requireContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR;
        AppDatabase.backupDatabase(BACKUP_FILENAME, path, (success, message) -> {
            String info;
            if (success) {info = getString(R.string.backup_successful);}
            else info = getString(R.string.backup_failed, message);
            Snackbar.make(requireActivity().findViewById(R.id.nav_host_fragment),
                    info,
                    7000)
                    .show();
        });
    }

    private void restore() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setPositiveButton(R.string.restore_confirm, (dialog, id) -> {
            String path = requireContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR;
            AppDatabase.restoreDatabase(BACKUP_FILENAME, path, (success, message) -> {
                if (!success) {
                    Snackbar.make(requireActivity().findViewById(R.id.nav_host_fragment),
                            getString(R.string.restore_failed, message),
                            7000)
                            .show();
                } else {
                    restartApp();
                }

            });
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> dialog.cancel());

        builder.setMessage(getString(R.string.restore_confirm_text))
                .setTitle(R.string.restore_confirm_title);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void restartApp() {
        NavUtils.navigateUpTo(requireActivity(), new Intent(getContext(), MainActivity.class));
        startActivity(requireActivity().getIntent());
    }
}