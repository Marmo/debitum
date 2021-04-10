package org.ebur.debitum.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
//import androidx.room.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    // get all transactions
    @androidx.room.Transaction
    @Query("select txn.* from txn order by timestamp desc")
    LiveData<List<TransactionWithPerson>> getAllTransactions();


    // get all persons with at least one transaction and a list of all of their transactions
    @androidx.room.Transaction
    @Query("select * from person where id_person in (select distinct id_person from txn)")
    LiveData<List<PersonWithTransactions>> getAllPersonsWithTransactions();

    // get a single transaction by id
    @androidx.room.Transaction
    @Query("select txn.* from txn where id_transaction = :idTransaction")
    TransactionWithPerson getTransaction(int idTransaction);

    @Update
    void update(Transaction transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Transaction transaction);

    @Delete
    int delete(Transaction transaction);

    @Query("delete from txn")
    void deleteAll();
}

