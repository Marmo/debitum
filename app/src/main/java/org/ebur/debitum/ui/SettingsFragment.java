package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.AppDatabase;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    //private final String BACKUP_FILENAME = "debitum_backup.db";
    private final String BACKUP_SUBDIR = "backup";

    public final static String PREF_KEY_DISMISS_FILTER_BEHAVIOUR = "dismiss_filter_behaviour";
    public final static String PREF_KEY_ITEM_RETURNED_STANDARD_FILTER = "item_returned_standard_filter";
    public final static String PREF_KEY_DATE_FORMAT = "date_format";

    private final ActivityResultLauncher<String[]> restoreLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
        AppDatabase.restoreDatabase(uri, (success, message) -> {
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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        int duration = getResources().getInteger(R.integer.duration_bottom_nav_transition);
        setEnterTransition(new MaterialFadeThrough().setDuration(duration));
        setExitTransition(new MaterialFadeThrough().setDuration(duration));

        final String PREF_KEY_BACKUP = "backup";
        final String PREF_KEY_RESTORE = "restore";
        final String PREF_KEY_GUIDE = "guide";
        final String PREF_KEY_LICENSES = "licenses";
        final String PREF_KEY_VERSION = "version";

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SwitchPreferenceCompat dismissFilterPref = findPreference(PREF_KEY_DISMISS_FILTER_BEHAVIOUR);
        if (dismissFilterPref != null) {
            dismissFilterPref.setSummaryOff(R.string.pref_dismiss_filter_summary_false);
            dismissFilterPref.setSummaryOn(R.string.pref_dismiss_filter_summary_true);
        }

        ListPreference itemReturnedFilterPref = findPreference(PREF_KEY_ITEM_RETURNED_STANDARD_FILTER);
        if (itemReturnedFilterPref != null) {
            itemReturnedFilterPref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        }

        ListPreference dateFormatPref = findPreference(PREF_KEY_DATE_FORMAT);
        if (dateFormatPref != null) {
            CharSequence[] values = dateFormatPref.getEntryValues();
            CharSequence[] entries = new CharSequence[values.length];

            CharSequence systemDefaultSValue = getResources().getString(R.string.pref_date_format_systemdefault_short_value);
            CharSequence systemDefaultSEntry = getResources().getString(R.string.pref_date_format_systemdefault_short);
            CharSequence systemDefaultMValue = getResources().getString(R.string.pref_date_format_systemdefault_medium_value);
            CharSequence systemDefaultMEntry = getResources().getString(R.string.pref_date_format_systemdefault_medium);
            CharSequence systemDefaultLValue = getResources().getString(R.string.pref_date_format_systemdefault_long_value);
            CharSequence systemDefaultLEntry = getResources().getString(R.string.pref_date_format_systemdefault_long);

            Date today = new Date();

            for (int i = 0;i< entries.length;i++) {
                CharSequence value = values[i];
                if (value.equals(systemDefaultSValue)) {
                    entries[i] = systemDefaultSEntry;
                } else if (value.equals(systemDefaultMValue)) {
                    entries[i] = systemDefaultMEntry;
                } else if (value.equals(systemDefaultLValue)) {
                    entries[i] = systemDefaultLEntry;
                } else {
                    entries[i] = Utilities.formatDate(today, values[i].toString());
                }
            }
            dateFormatPref.setEntries(entries);

            dateFormatPref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
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
            restorePref.setSummary(getString(R.string.pref_restore_summary));
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

        getListView().setTransitionGroup(true);
        getListView().setTransitionName("not needed but transition group is only respected if name set");
    }

    // ---------------------
    // Backup and restore DB
    // ---------------------

    private void backup() {
        // assemble filename
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);
        int second = today.get(Calendar.SECOND);
        String filename = String.format(Locale.getDefault(), "debitum-backup-%04d-%02d-%02dT%02d_%02d_%02d.db", year, month, day, hour, minute, second);

        String path = requireContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR;

        AppDatabase.backupDatabase(filename, path, (success, message) -> {
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
            String[] mimetypes = {"application/x-sqlite3", "application/octet-stream"};
            restoreLauncher.launch(mimetypes);
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