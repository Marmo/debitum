package org.ebur.debitum.database;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.ebur.debitum.R;
import org.ebur.debitum.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {Transaction.class, Person.class, Image.class},
        version = 6,
        exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
    public abstract PersonDao personDao();
    public abstract ImageDao imageDao();

    // define a singleton AppDatabase to prevent having multiple instances of the database opened at the same time
    private static volatile AppDatabase INSTANCE;

    private static File dbFile;
    private static String backupFileNotFoundMessage;
    private Context context;

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

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE txn "
                    + " ADD COLUMN timestamp_returned INTEGER");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE person "
                    + " ADD COLUMN linked_contact_uri TEXT");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE image (id_transaction INTEGER NOT NULL, filename TEXT NOT NULL, PRIMARY KEY (id_transaction, filename) ON CONFLICT IGNORE)");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE txn "
                    + " ADD COLUMN has_images INTEGER NOT NULL DEFAULT(0)");
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
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_5_6
                            )
                            .build();
                    INSTANCE.context = context;
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
    public static void backupDatabase(@NonNull String filename, @NonNull String path, @Nullable OnBackupRestoreFinishListener onBackupRestoreFinishListener){
        boolean success = false;
        String message = "";
        File backupFile = new File(path, filename);

        try {
            // create backup dir if it not yet exists
            if(backupFile.getParentFile() != null
                    && (backupFile.getParentFile().exists()
                    || backupFile.getParentFile().mkdirs())) {
                FileUtils.copyFile(dbFile, backupFile);
                success = true;
            }
        } catch (IOException e) {
            message = e.getMessage();
        } finally {
            if(onBackupRestoreFinishListener != null)
                onBackupRestoreFinishListener.onFinished(success, message);
        }
    }

    public static void restoreDatabase(@NonNull String filename, @NonNull String path, @Nullable OnBackupRestoreFinishListener onBackupRestoreFinishListener) {
        // TODO check if uri points to valid debitum database
        boolean success = false;
        String message = "";
        File backupFile = new File(path, filename);

        try {
            if(backupFile.exists()) {
                FileUtils.copyFile(backupFile, dbFile);
                success = true;
            } else {
                message = backupFileNotFoundMessage;
            }
        } catch (IOException e) {
            message = e.getMessage();
        } finally {
            if(onBackupRestoreFinishListener != null)
                onBackupRestoreFinishListener.onFinished(success, message);
        }
    }

    public static void restoreDatabase(@NonNull Uri uri, @Nullable OnBackupRestoreFinishListener onBackupRestoreFinishListener) {
        // TODO check if uri points to valid debitum database
        boolean success = false;
        String message = "";

        try {
            if(true) { // TODO check if uri is valid
                FileUtils.copyFile(uri, dbFile, INSTANCE.context.getContentResolver());
                success = true;
            } else {
                message = backupFileNotFoundMessage;
            }
        } catch (IOException e) {
            message = e.getMessage();
        } finally {
            if(onBackupRestoreFinishListener != null)
                onBackupRestoreFinishListener.onFinished(success, message);
        }
    }

    public interface OnBackupRestoreFinishListener {
        void onFinished(boolean success, String message);
    }
}
