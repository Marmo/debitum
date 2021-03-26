package org.ebur.debitum.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDAO {

    //get all transactions
    @Query("select * from txn ")
    LiveData<List<Transaction>> getAllTransactions();

    //get all transactions of one person
    @Query("select txn.* from txn " +
            "inner join person on txn.id_person = person.id_person " +
            "where name = :name")
    List<Transaction> getTransactionsByName(String name);

    //get sum of all transactions of one person
    @Query("select sum(txn.amount) from txn " +
            "join person on txn.id_person = person.id_person " +
            "where person.name = :name and txn.is_monetary " +
            "group by person.name")
    int getSumByName(String name);

    //get sum of all transactions grouped by person
    @Query("select person.name, sum(txn.amount) as sum from txn " +
            "join person on txn.id_person = person.id_person " +
            "where txn.is_monetary " +
            "group by person.name")
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

