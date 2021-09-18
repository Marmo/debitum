package org.ebur.debitum.database;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public abstract class ImageDao {

    @Query("SELECT filename FROM image where id_transaction = :idTransaction")
    abstract List<String> getImageFilenames(int idTransaction);

    @Query("SELECT distinct filename FROM image")
    abstract List<String> getAllImageFilenames();

    @Insert
    abstract void insert(Image... images);

    // returns a boolean so that ImageRepository can use a Future to enforce that the image links
    // are updated before deleteOrphanedImages is called in EditTransactionViewModel
    @Transaction
    boolean update(int idTransaction, @Nullable List<String> filenames) {
        // first delete all images of the transaction
        deleteAllImagesOfTransaction(idTransaction);
        // then add all images from the passed list
        if (filenames != null) {
            for (String filename : filenames) {
                insert(new Image(filename, idTransaction));
            }
        }
        return true;
    }

    @Query("delete from image where id_transaction = :idTransaction")
    abstract void deleteAllImagesOfTransaction(int idTransaction);

    @Query("delete from image where filename not in (:existingFiles)")
    public abstract void deleteBrokenImageLinks(List<String> existingFiles);
}
