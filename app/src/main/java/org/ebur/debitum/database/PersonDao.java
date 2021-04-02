package org.ebur.debitum.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PersonDao {

    @Query("SELECT * FROM person order by name")
    LiveData<List<Person>> getAllPersons();

    @Query("select id_person from person where name = :name")
    int getId(String name);

    @Insert
    void insert(Person... persons);

    @Delete
    void delete(Person person);

    @Query("delete from person")
    void deleteAll();
}
