package org.ebur.debitum.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.ebur.debitum.model.Transaction;

import java.util.List;
import java.util.Map;

@Dao
public interface TransactionDAO {

    // TODO reintroduce separate person table/entity,
    //  https://stackoverflow.com/questions/47511750/how-to-use-foreign-key-in-room-persistence-library
    //  https://developer.android.com/training/data-storage/room/relationships#java
    //get all transactions
    @Query("select * from txn ")
    LiveData<List<Transaction>> getAllTransactions();

    //get all transactions of one person
    @Query("select * from txn " +
            "where name = :name")
    List<Transaction> getTransactionsByName(String name);

    //get sum of all transactions of one person
    @Query("select sum(amount) from txn " +
            "where name = :name " +
            "group by name")
    int getSumByName(String name);

    //get sum of all transactions grouped by person
    @Query("select name, sum(txn.amount) as sum from txn " +
            "where is_monetary " +
            "group by name")
    List<PersonWithSum> getSumByName();

    static class PersonWithSum {
        public String name;
        public int sum;
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Transaction... transactions);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("delete from txn")
    void deleteAll();
}

