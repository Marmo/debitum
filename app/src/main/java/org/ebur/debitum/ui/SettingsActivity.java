package org.ebur.debitum.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import org.ebur.debitum.R;
import org.ebur.debitum.database.AppDatabase;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        // TODO we need a toolbar here
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final String BACKUP_FILENAME = "debitum_backup.db";
        private final String BACKUP_SUBDIR = "backup";
        private final String PREF_KEY_BACKUP = "backup";
        private final String PREF_KEY_RESTORE = "restore";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference backupPref = findPreference(PREF_KEY_BACKUP);
            backupPref.setSummary(getString(R.string.pref_backup_summary, getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR));
            backupPref.setOnPreferenceClickListener(preference -> {backup(); return true;});
            Preference restorePref = findPreference(PREF_KEY_RESTORE);
            restorePref.setSummary(getString(R.string.pref_restore_summary, BACKUP_FILENAME));
            restorePref.setOnPreferenceClickListener(preference -> {restore(); return true;});
        }

        // ---------------------
        // Backup and restore DB
        // ---------------------

        private void backup() {
            String path = getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR;
            AppDatabase.backupDatabase(BACKUP_FILENAME, path, (success, message) -> {
                String info;
                if (success) {info = getString(R.string.backup_successful);}
                else info = getString(R.string.backup_failed, message);
                Snackbar.make(requireActivity().findViewById(R.id.settings),
                        info,
                        Snackbar.LENGTH_SHORT)
                        .show();
            });
        }

        private void restore() {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setPositiveButton(R.string.restore_confirm, (dialog, id) -> {
                String path = getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR;
                AppDatabase.restoreDatabase(BACKUP_FILENAME, path, (success, message) -> {
                    if (!success) {
                        Snackbar.make(requireActivity().findViewById(R.id.settings),
                                getString(R.string.restore_failed, message),
                                Snackbar.LENGTH_SHORT)
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
            Intent restartIntent = new Intent(getContext(), MainActivity.class);
            int requestCode = 1;
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), requestCode, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
            System.exit(0);
        }
    }
}