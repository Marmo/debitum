package org.ebur.debitum.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
//import androidx.room.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    //get all transactions
    @androidx.room.Transaction
    @Query("select txn.* from txn inner join person on txn.id_person = person.id_person order by timestamp desc")
    LiveData<List<TransactionWithPerson>> getAllTransactions();

    //get all transactions of one person
//    @androidx.room.Transaction
//    @Query("select txn.* from txn " +
//            "inner join person on txn.id_person = person.id_person " +
//            "where name = :name")
//    List<TransactionWithPerson> getTransactionsByName(String name);

    //get sum of all transactions of one person
//    @Query("select sum(txn.amount) from txn " +
//            "join person on txn.id_person = person.id_person " +
//            "where person.name = :name and txn.is_monetary " +
//            "group by person.name")
//    int getSumByName(String name);

    //get all persons and a list of all of their transactions
    @androidx.room.Transaction
    @Query("select * from person where id_person in (select distinct id_person from txn)")
    LiveData<List<PersonWithTransactions>> getAllPersonsWithTransactions();

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insert(Transaction... transactions);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("delete from txn")
    void deleteAll();
}

