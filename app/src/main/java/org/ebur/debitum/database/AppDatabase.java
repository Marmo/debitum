package org.ebur.debitum.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.ebur.debitum.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {Transaction.class, Person.class},
        version = 2,
        exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
    public abstract PersonDao personDao();

    // define a singleton AppDatabase to prevent having multiple instances of the database opened at the same time
    private static volatile AppDatabase INSTANCE;

    private static File dbFile;
    private static String backupFileNotFoundMessage;

    // create an ExecutorService with a fixed thread pool that will be used to run database operations asynchronously on a background thread
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseTaskExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE person "
                    + " ADD COLUMN note TEXT");
        }
    };

    /* returns the singleton. It'll create the database the first time it's accessed, using Room's
     * database builder to create a RoomDatabase object
     */
    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "transaction_database")
                            .setJournalMode(JournalMode.TRUNCATE) // to make export easier, might have negative implications on write performance
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        dbFile = context.getDatabasePath("transaction_database");
        backupFileNotFoundMessage = context.getString(R.string.pref_restore_file_not_found);

        return INSTANCE;
    }

    // ---------------------
    // Backup and restore DB
    // ---------------------
    private static void backupOrRestoreDatabase(boolean backup, String filename, String path, OnBackupRestoreFinishListener onBackupRestoreFinishListener) {
            boolean success = false;
            String message = "";
            File backupFile = new File(path, filename);

            try {
                if(backup) {
                    // create backup dir if it not yet exists
                    if(backupFile.getParentFile() != null
                            && (backupFile.getParentFile().exists()
                                || backupFile.getParentFile().mkdirs())) {
                        copyFile(dbFile, backupFile);
                        success = true;
                    }
                }
                else {
                    if(backupFile.exists()) {
                        copyFile(backupFile, dbFile);
                        success = true;
                    } else {
                        message = backupFileNotFoundMessage;
                    }
                }
            } catch (IOException e) {
                message = e.getMessage();
            } finally {
                if(onBackupRestoreFinishListener != null)
                    onBackupRestoreFinishListener.onFinished(success, message);
            }
    }

    public static void backupDatabase(String filename, String path, OnBackupRestoreFinishListener onBackupRestoreFinishListener){
        backupOrRestoreDatabase(true, filename, path, onBackupRestoreFinishListener);
    }

    public static void restoreDatabase(String filename, String path, OnBackupRestoreFinishListener onBackupRestoreFinishListener) {
        backupOrRestoreDatabase(false, filename, path, onBackupRestoreFinishListener);
    }

    private static void copyFile(File source, File dest) throws IOException {
        // try-with-resources
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    public interface OnBackupRestoreFinishListener {
        void onFinished(boolean success, String message);
    }
}
