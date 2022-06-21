package org.ebur.debitum.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialFadeThrough;

import org.ebur.debitum.BuildConfig;
import org.ebur.debitum.R;
import org.ebur.debitum.database.AppDatabase;
import org.ebur.debitum.ui.edit_transaction.EditTransactionFragment;
import org.ebur.debitum.util.FileUtils;
import org.ebur.debitum.util.Utilities;
import org.ebur.debitum.viewModel.SettingsViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


public class SettingsFragment extends PreferenceFragmentCompat {

    @SuppressWarnings("unused")
    private static final String TAG = "SettingsFragment";

    public final static String PREF_KEY_DISMISS_FILTER_BEHAVIOUR = "dismiss_filter_behaviour";
    public final static String PREF_KEY_ITEM_RETURNED_STANDARD_FILTER = "item_returned_standard_filter";
    public final static String PREF_KEY_DATE_FORMAT = "date_format";
    public final static String PREF_KEY_DECIMALS = "decimals";
    public final static String PREF_KEY_INVERT_COLORS = "invert_colors";
    public final static String PREF_KEY_CHANGELOG = "changelogSeenVersion";

    public final static String FILENAME_DB = "debitum.db";
    public final static String FILENAME_PREFS = "debitum-preferences.xml";

    private SettingsViewModel viewModel;

    private final ActivityResultLauncher<String[]> restoreLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            restore(uri);
                        }
                    });

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        int duration = getResources().getInteger(R.integer.duration_bottom_nav_transition);
        setEnterTransition(new MaterialFadeThrough().setDuration(duration));
        setExitTransition(new MaterialFadeThrough().setDuration(duration));

        final String PREF_KEY_BACKUP = "backup";
        final String PREF_KEY_RESTORE = "restore";
        final String PREF_KEY_LICENSES = "licenses";
        final String PREF_KEY_VERSION = "version";

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SwitchPreferenceCompat dismissFilterPref = findPreference(PREF_KEY_DISMISS_FILTER_BEHAVIOUR);
        if (dismissFilterPref != null) {
            dismissFilterPref.setSummaryOff(R.string.pref_dismiss_filter_summary_false);
            dismissFilterPref.setSummaryOn(R.string.pref_dismiss_filter_summary_true);
        }

        SwitchPreferenceCompat invertColorsPref = findPreference(PREF_KEY_INVERT_COLORS);
        if (invertColorsPref != null) {
            invertColorsPref.setSummaryOff(R.string.pref_invert_colors_summary_off);
            invertColorsPref.setSummaryOn(R.string.pref_invert_colors_summary_on);
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

        ListPreference decimalsPref = findPreference(PREF_KEY_DECIMALS);
        if (decimalsPref != null) {
            decimalsPref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            decimalsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                // generally the strategy is to keep the existing amounts
                // example: old setting: 2 decimals -> new setting: 1 decimal
                // |    old amount      |      new amount    |
                // | internal | display | internal | display |
                // |----------|---------|----------|---------|
                // |  1000    | 10.00   |   100    |  10.0   |
                // |  12345   | 123.45  |  1235    | 123.5   |
                int newInt = Integer.parseInt((String)newValue);
                int oldInt = Integer.parseInt(((ListPreference)preference).getValue());
                if (newInt < oldInt) {
                    // we need to warn the user about possible data loss ONLY if we *decrease* the
                    // number of decimals
                    showChangeDecimalsDialog(newInt, oldInt);
                } else if (oldInt < newInt) {
                    viewModel.changeTransactionDecimals(newInt-oldInt); // old=2, new=1 -> shift = -1
                }
                return true;
            });
        }

        Preference backupPref = findPreference(PREF_KEY_BACKUP);
        if (backupPref!=null) {
            backupPref.setOnPreferenceClickListener(preference -> {
                startBackup();
                return true;
            });
        }
        Preference restorePref = findPreference(PREF_KEY_RESTORE);
        if (restorePref!=null) {
            restorePref.setSummary(getString(R.string.pref_restore_summary));
            restorePref.setOnPreferenceClickListener(preference -> {
                startRestore();
                return true;
            });
        }

        Preference licensesPref = findPreference(PREF_KEY_LICENSES);
        if (licensesPref!=null) {
            licensesPref.setOnPreferenceClickListener(preference -> {
                NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_licenses);
                return true;
            });
        }

        Preference changelogPref = findPreference(PREF_KEY_CHANGELOG);
        if (changelogPref != null) {
            changelogPref.setOnPreferenceClickListener(preference -> {
                NavHostFragment.findNavController(this).navigate(R.id.action_global_changelog);
                return true;
            });
        }

        Preference versionPref = findPreference(PREF_KEY_VERSION);
        if (versionPref!=null) {
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
        getListView().setTransitionName("not needed but transition group is only respected if name is set");
    }

    private void showChangeDecimalsDialog(int newNrOfDecimals, int oldNrOfDecimals) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setPositiveButton(R.string.decrease_decimals_dialog_confirm, (dialog, id) -> {
            viewModel.changeTransactionDecimals(newNrOfDecimals-oldNrOfDecimals); // old=2, new=1 -> shift = -1
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> {
            // reset setting to old value
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
            pref.edit().putString(PREF_KEY_DECIMALS, String.valueOf(oldNrOfDecimals)).apply();
            dialog.cancel();
            // reopen settings fragment, as else the setting will visually remain at the new value,
            // even though the change was cancelled.
            // Somehow this also affects the oldValue when subsequently changing the setting again, see
            // https://github.com/Marmo/debitum/issues/80, LEADING TO WRONG AMOUNTS!
            NavHostFragment.findNavController(this).navigate(R.id.settings_dest);
        });

        builder.setMessage(getString(R.string.decrease_decimals_dialog_text))
                .setTitle(R.string.decrease_decimals_dialog_title);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    // ---------------------
    // Backup and restore DB
    // ---------------------

    // handle SAF file picker for backup
    private void startBackup() {
        // assemble filename preset
        Calendar today = Calendar.getInstance();
        String filenameZip = String.format(Locale.getDefault(),
                "debitum-backup-%04d-%02d-%02dT%02d_%02d_%02d.zip",
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH),
                today.get(Calendar.HOUR_OF_DAY),
                today.get(Calendar.MINUTE),
                today.get(Calendar.SECOND)
        );

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, filenameZip);
        backupARL.launch(intent);
    }

    ActivityResultLauncher<Intent> backupARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri destUri = data.getData();
                    backup(destUri);
                }
            }
    );

    // perform actual backup
    private void backup(Uri destUri) {

        String BACKUP_SUBDIR = "backup";
        String path = requireContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + BACKUP_SUBDIR;

        AppDatabase.backupDatabase(FILENAME_DB, path, (successDb, messageDb) -> {
            File dbFile = new File(path, FILENAME_DB);
            File prefsFile = new File(path, FILENAME_PREFS);
            File imagesDir = EditTransactionFragment.getImageDir(requireContext());
            String info;

            List<File> filesToZip = new ArrayList<>();
            filesToZip.add(dbFile);
            filesToZip.add(prefsFile);
            if (imagesDir.listFiles() != null) { filesToZip.addAll(Arrays.asList(imagesDir.listFiles()));}

            if (successDb) {
                info = getString(R.string.backup_successful);
                try {
                    exportPreferences(prefsFile);
                    FileUtils.zip(filesToZip, destUri, requireContext());
                } catch (IOException e) {
                    e.printStackTrace();
                    info = getString(R.string.backup_failed, e.getMessage());
                }
            } else {
                info = getString(R.string.backup_failed, messageDb);
            }
            finishBackup(info, dbFile, prefsFile);
        });
    }

    // stores the app's preferences as a java properties file
    private void exportPreferences(@NonNull File prefsFile) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Properties props = new Properties();
        props.putAll(prefs.getAll());
        FileOutputStream out = new FileOutputStream(prefsFile);
        props.storeToXML(out, "");
        out.close();
    }

    private void finishBackup(@NonNull String info, @NonNull File dbFile, @NonNull File prefsFile) {
        // show snackbar
        Snackbar.make(requireActivity().findViewById(R.id.nav_host_fragment),
                info,
                7000)
                .show();
        // cleanup temporary db file and prefs file
        dbFile.delete();
        prefsFile.delete();
    }

    private void startRestore() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setPositiveButton(R.string.restore_confirm, (dialog, id) -> {
            String[] mimetypes = {"application/zip"};
            restoreLauncher.launch(mimetypes);
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> dialog.cancel());

        builder.setMessage(getString(R.string.restore_confirm_text))
                .setTitle(R.string.restore_confirm_title);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void restore(Uri uriZip) {
        File imageDir = EditTransactionFragment.getImageDir(requireContext());
        File tmpDir = new File(imageDir, "restore/");
        File dbFile = new File(tmpDir, FILENAME_DB);
        File prefsFile = new File(tmpDir, FILENAME_PREFS);
        try {
            // create tmpDir
            if (!tmpDir.mkdirs() || !tmpDir.canWrite()) {
                // abort with error message
                String info = getString(R.string.restore_failed_tmpdir, tmpDir.getAbsolutePath());
                showSnackbar(getString(R.string.restore_failed, info));
                FileUtils.deleteDir(tmpDir);
                return;
            }
            // unzip file to tmpDir
            FileUtils.unzip(uriZip, tmpDir, requireContext());

            // note: only the db file is checked here. Any orphaned files will
            // be deleted when the EditTransaction Dialog is closed the next time
            if (!dbFile.exists() | !dbFile.canRead()) {
                // abort with error message
                String info = getString(R.string.restore_failed_dbFileMissing);
                showSnackbar(getString(R.string.restore_failed, info));
                FileUtils.deleteDir(tmpDir);
                return;
            }

            // restore database
            AppDatabase.restoreDatabase(Uri.fromFile(dbFile), (success, message) -> {
                if (success) {
                    // delete dbFile so that it is not copied to imagesDir afterwards
                    dbFile.delete();

                    //import preferences
                    importPreferences(prefsFile);
                    // delete prefsFile so that it is not copied to imagesDir afterwards
                    prefsFile.delete();

                    // copy images to imageDir
                    // Note: there is no (urgent) need for cleaning the image directory before
                    // copying the restored files there, because any excess images will be deleted
                    // when the EditTransaction dialog is closed (save/dismiss) the next time
                    for (File file:tmpDir.listFiles()) {
                        try {
                            FileUtils.copyFile(file, new File(imageDir, file.getName()));
                        } catch (IOException e) {
                            // show warning and continue with next file
                            e.printStackTrace();
                            showSnackbar(getString(R.string.restore_not_all_images_restored, e.getMessage()));
                        }
                    }
                    FileUtils.deleteDir(tmpDir);
                    restartApp();
                } else {
                    FileUtils.deleteDir(tmpDir);
                    showSnackbar(getString(R.string.restore_failed, message));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            FileUtils.deleteDir(tmpDir);
            showSnackbar(getString(R.string.restore_failed, e.getMessage()));
        }
    }

    // loads the app's preferences from a java properties file
    private void importPreferences(@NonNull File prefsFile) {
        Properties props = new Properties();

        if (!prefsFile.exists()) {
            // assume we are restoring from old backup without preferences. Then the amounts have
            // 2 decimals (there was not setting for decimals when backups did not contain preferences)
            // this prevents importing from old backups while having decimals set to something other
            // than 2 and ending up with wrong amounts
            props.put(PREF_KEY_DECIMALS, "2");
        } else {
            // get preferences from props xml
            try {
                FileInputStream in = new FileInputStream(prefsFile);
                props.loadFromXML(in);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                showSnackbar(getString(R.string.restore_preferences_not_restored, e.getMessage()));
            }
        }
        // update app preferences from props
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .edit();

        for(Object keyObj:props.keySet()) {
            String key = keyObj.toString();
            String value = props.getProperty(key);
            editor.putString(key, value);
        }
        editor.apply();
    }

    private void restartApp() {
        Context context = requireContext();
        String packageName = context.getPackageName();
        Intent restartIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(restartIntent);
        NavUtils.navigateUpTo(requireActivity(), new Intent(context, MainActivity.class));
    }

    private void showSnackbar(String message) {
        Snackbar.make(requireActivity().findViewById(R.id.nav_host_fragment),
                message,
                7000)
                .show();
    }
}