package org.ebur.debitum.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialFadeThrough;

import org.ebur.debitum.R;
import org.ebur.debitum.database.AppDatabase;

import java.io.File;


public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    private final String BACKUP_FILENAME = "debitum_backup.db";
    private final String BACKUP_SUBDIR = "backup";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setEnterTransition(new MaterialFadeThrough()); setExitTransition(new MaterialFadeThrough());

        final String PREF_KEY_BACKUP = "backup";
        final String PREF_KEY_RESTORE = "restore";
        final String PREF_KEY_GUIDE = "guide";
        final String PREF_KEY_GITHUB = "github";

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

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
        if (guidePref!=null) {guidePref.setOnPreferenceClickListener(preference -> {
            showGuide();
            return true;
        });
        }
        Preference githubPref = findPreference(PREF_KEY_GITHUB);
        if (githubPref!=null) {githubPref.setOnPreferenceClickListener(preference -> {
                openGithub();
                return true;
            });
        }
    }

    private void showGuide() {
        WebView webView = new WebView(getContext());
        webView.loadData(getString(R.string.guide_text),"text/html", "utf-8");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.pref_guide_title)
                .setView(webView)
                .setPositiveButton(getString(R.string.pref_guide_dialog_close), (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void openGithub() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://github.com/Marmo/debitum"));
        startActivity(i);
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
                    Snackbar.LENGTH_LONG)
                    .show();
        });
    }

    private void restore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setPositiveButton(R.string.restore_confirm, (dialog, id) -> {
            String path = requireContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR;
            AppDatabase.restoreDatabase(BACKUP_FILENAME, path, (success, message) -> {
                if (!success) {
                    Snackbar.make(requireActivity().findViewById(R.id.nav_host_fragment),
                            getString(R.string.restore_failed, message),
                            Snackbar.LENGTH_LONG)
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
        /*Intent restartIntent = new Intent(getContext(), MainActivity.class);
        int requestCode = 1;
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), requestCode, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)requireContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);
        System.exit(0);*/
        NavUtils.navigateUpTo(requireActivity(), new Intent(getContext(), MainActivity.class));
        startActivity(requireActivity().getIntent());
    }
}