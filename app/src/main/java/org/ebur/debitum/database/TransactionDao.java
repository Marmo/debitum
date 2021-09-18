package org.ebur.debitum.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class TransactionDao {

    // get all transactions
    @androidx.room.Transaction
    @Query("select txn.* from txn where is_monetary = :isMonetary order by timestamp desc")
    abstract LiveData<List<TransactionWithPerson>> getAllTransactions(boolean isMonetary);

    // get all persons with at least one transaction and a list of all of their transactions
    @androidx.room.Transaction
    //@Query("select * from person where id_person in (select distinct id_person from txn)")
    @Query("select person.* " +
            "from person join txn on person.id_person = txn.id_person " +
            "group by person.id_person, person.name, person.note " +
            "order by max(txn.timestamp) desc")
    abstract LiveData<List<PersonWithTransactions>> getAllPersonsWithTransactions();

    // get a single transaction by id
    @androidx.room.Transaction
    @Query("select txn.* from txn where id_transaction = :idTransaction")
    abstract TransactionWithPerson getTransaction(int idTransaction);

    @Update
    abstract void update(Transaction transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insert(Transaction transaction);

    @Delete
    abstract int deleteTransaction(Transaction transaction);

    @Query("delete from image where id_transaction = :idTransaction")
    abstract void deleteTransactionsImages(int idTransaction);

    // delete a transaction and all of its image links
    // note: there is no need to delete the image files here, as they are deleted upon the next
    // dismissed or saved transaction, when EditTransactionViewModel::deleteOrphanedImageFiles
    @androidx.room.Transaction
    int delete(Transaction transaction) {
        deleteTransactionsImages(transaction.idTransaction);
        return deleteTransaction(transaction);
    }
}

