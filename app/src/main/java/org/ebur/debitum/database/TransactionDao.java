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

    // changes the number of decimals of each monetary transaction, while trying to keep the amount
    // shown to the user as constant as possible (if precision is lost, the amount will be rounded)
    //
    // since the amount is an integer internally, when adding (removing) a decimal the amount has to
    // be multiplied (divided) by 10 to keep the value it represents constant
    //
    // Examples:
    // |    old amount      |      new amount    |       new amount    |
    // |                    |  one decimal added | one decimal removed |
    // | internal | display | internal | display |  internal | display |
    // |----------|---------|----------|---------|-----------|---------|
    // |   1000   |  10.00  |   10000  |  10.000 |    100    |  10.0   |
    // |  12345   | 123.45  |  123450  | 123.450 |   1235    | 123.5   |
    void changeDecimals(int shift) {
        // since sqlite does not have a power function available here, we need to do the
        // exponentiation here in java
        changeDecimalsQuery(Math.pow(10.0, shift));
    }
    @Query("update txn set amount = round(amount*:factor, 0) where txn.is_monetary")
    abstract void changeDecimalsQuery(double factor);
}

