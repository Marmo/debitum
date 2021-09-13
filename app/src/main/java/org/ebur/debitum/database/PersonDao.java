package org.ebur.debitum.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class PersonDao {

    @Query("SELECT * FROM person order by name")
    abstract LiveData<List<Person>> getAllPersons();

    @Query("SELECT * FROM person order by name")
    abstract List<Person> getAllPersonsNonLive();

    @Query("select id_person from person where name = :name limit 1")
    abstract int getPersonId(String name);

    @Query("select * from person where id_person = :id limit 1")
    abstract Person getPersonById(int id);

    @Transaction
    @Query("select exists (select 1 from person where name=:name limit 1)")
    abstract Boolean exists(String name);

    @Insert
    abstract void insert(Person... persons);

    @Update
    abstract void update(Person person);

    @Query("delete from image " +
            "where id_transaction in (select id_transaction from txn where id_person = :idPerson)")
    abstract void deleteImagesOfPerson(int idPerson);

    @Query("delete from txn where id_person = :idPerson")
    abstract void deleteTransactionsOfPerson(int idPerson);

    @Query("delete from person where id_person = :idPerson;")
    abstract void deletePerson(int idPerson);

    // delete a Person and all of their transactions and image links
    // note: there is no need to delete the image files here, as they are deleted upon the next
    // dismissed or saved transaction, when EditTransactionViewModel::deleteOrphanedImageFiles
    @Transaction
    void delete(Person person) {
        int id = person.idPerson;
        deleteImagesOfPerson(id);
        deleteTransactionsOfPerson(id);
        deletePerson(id);
    }
}
