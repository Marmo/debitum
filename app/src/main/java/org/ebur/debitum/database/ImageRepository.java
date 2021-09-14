package org.ebur.debitum.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ImageRepository {

    private final ImageDao imageDao;

    // Note that in order to unit test the Repository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public ImageRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        imageDao = db.imageDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public List<String> getAllImageFilenames() {
        Future<List<String>> future = AppDatabase.databaseTaskExecutor.submit(imageDao::getAllImageFilenames);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public List<String> getImageFilenames(int idTransaction) {
        Future<List<String>> future = AppDatabase.databaseTaskExecutor.submit(() -> imageDao.getImageFilenames(idTransaction));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insert(String filename, int idTransaction) {
        AppDatabase.databaseTaskExecutor.execute(() -> {
            imageDao.insert(new Image(filename, idTransaction));
        });
    }

    public void deleteBrokenImageLinks(@NonNull List<File> existingFiles) {
        List<String> existingFilenames = existingFiles
                .stream()
                .map(File::getName)
                .collect(Collectors.toList());
        AppDatabase.databaseTaskExecutor.execute(() -> {
            imageDao.deleteBrokenImageLinks(existingFilenames);
        });
    }

    public boolean update(int idTransaction, @Nullable List<String> filenames) {
        Future<Boolean> future = AppDatabase.databaseTaskExecutor.submit(() -> imageDao.update(idTransaction, filenames));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
