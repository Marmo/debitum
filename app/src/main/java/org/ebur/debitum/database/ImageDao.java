package org.ebur.debitum.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class ImageDao {

    @Query("SELECT filename FROM image where id_transaction = :idTransaction")
    abstract List<String> getImageFilenames(int idTransaction);

    @Query("SELECT distinct filename FROM image")
    abstract List<String> getAllImageFilenames();

    @Insert
    abstract void insert(Image... images);

    @Update
    abstract void update(Image image);

    @Query("delete from image where id_transaction = :idTransaction")
    abstract void deleteAllImagesOfTransaction(int idTransaction);

    @Query("delete from image where filename = :filename AND id_transaction = :idTransaction;")
    abstract void deleteImage(String filename, int idTransaction);
}
