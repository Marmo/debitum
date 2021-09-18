package org.ebur.debitum.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "image", primaryKeys = {"id_transaction", "filename"})
public class Image {
    public Image (@NonNull String filename, int idTransaction) {
        this.filename = filename;
        this.idTransaction = idTransaction;
    }
    @ColumnInfo(name = "id_transaction") public int idTransaction;
    @ColumnInfo(name = "filename") @NonNull public String filename;
}
